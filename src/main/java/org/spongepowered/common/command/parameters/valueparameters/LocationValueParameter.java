/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.command.parameters.valueparameters;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.command.parameters.CommandExecutionContext;
import org.spongepowered.api.command.parameters.ParameterParseException;
import org.spongepowered.api.command.parameters.specification.CatalogedValueParameter;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

public class LocationValueParameter implements CatalogedValueParameter {

    private final WorldPropertiesValueParameter worldParser;
    private final Vector3dValueParameter vectorParser;

    public LocationValueParameter(WorldPropertiesValueParameter worldParser, Vector3dValueParameter vectorParser) {
        this.worldParser = worldParser;
        this.vectorParser = vectorParser;
    }

    @Override
    public String getId() {
        return "sponge:location";
    }

    @Override
    public String getName() {
        return "Location parameter";
    }

    @Override
    public List<String> complete(CommandSource source, TokenizedArgs args, CommandExecutionContext context) throws ParameterParseException {
        Object state = args.getState();
        Optional<String> nextPossibility = args.nextIfPresent();
        if (nextPossibility.isPresent() && nextPossibility.get().startsWith("@")) {
            return Selector.complete(nextPossibility.get());
        }
        args.setState(state);
        List<String> ret;
        if ((ret = this.worldParser.complete(source, args, context)).isEmpty()) {
            args.setState(state);
            ret = this.vectorParser.complete(source, args, context);
        }
        return ret;
    }

    @Override
    public Optional<Object> getValue(CommandSource source, TokenizedArgs args, CommandExecutionContext context)
            throws ParameterParseException {
        Object state = args.getState();
        if (args.peek().startsWith("@")) { // We are a selector
            return Optional.of(Selector.parse(args.next()).resolve(source).stream()
                    .map(Entity::getLocation)
                    .collect(ImmutableSet.toImmutableSet()));
        }

        Object world;
        Object vec = null;
        try {
            world = checkNotNull(this.worldParser.getValue(args.next()), "worldVal");
        } catch (ParameterParseException ex) {
            args.setState(state);
            if (!(source instanceof Locatable)) {
                throw args.createError(t("Source must have a location in order to have a fallback world"));
            }
            world = ((Locatable) source).getWorld().getProperties();
            try {
                vec = checkNotNull(this.vectorParser.getValue(source, args, context), "vectorVal");
            } catch (ParameterParseException ex2) {
                args.setState(state);
                throw ex;
            }
        }
        if (vec == null) {
            vec = checkNotNull(this.vectorParser.getValue(source, args, context), "vectorVal");
        }

        if (world instanceof Collection<?>) {
            // multiple values
            if (((Collection<?>) world).size() != 1) {
                throw args.createError(t("A location must be specified in only one world!"));
            }
            world = ((Collection<?>) world).iterator().next();
        }
        WorldProperties targetWorldProps = ((WorldProperties) world);
        Optional<World> targetWorld = Sponge.getServer().getWorld(targetWorldProps.getUniqueId());

        @SuppressWarnings("ConstantConditions")
        Vector3d vector = (Vector3d) vec;
        return Optional.of(new Location<>(targetWorld.get(), vector));
    }

}
