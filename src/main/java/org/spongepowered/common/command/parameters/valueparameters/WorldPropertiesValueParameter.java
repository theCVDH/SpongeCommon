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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameters.CommandExecutionContext;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.command.parameters.ParameterParseException;
import org.spongepowered.api.command.parameters.specification.CatalogedValueParameter;
import org.spongepowered.api.command.parameters.specification.impl.PatternMatchingValueParameter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class WorldPropertiesValueParameter extends PatternMatchingValueParameter implements CatalogedValueParameter {

    private final CatalogTypeValueParameter<DimensionType> dimensionParameter;

    public WorldPropertiesValueParameter(CatalogTypeValueParameter<DimensionType> dimensionParameter) {
        this.dimensionParameter = dimensionParameter;
    }

    @Override
    public String getId() {
        return "sponge:world_properties";
    }

    @Override
    public String getName() {
        return "World Properties parameter";
    }

    @Override
    public Optional<Object> getValue(CommandSource source, TokenizedArgs args, CommandExecutionContext context)
            throws ParameterParseException {
        final String next = args.peek();
        if (next.startsWith("#")) {
            String specifier = next.substring(1);
            if (specifier.equalsIgnoreCase("first")) {
                args.next();
                return Sponge.getServer().getAllWorldProperties().stream().filter(input -> input != null && input.isEnabled())
                        .findFirst().map(x -> (Object) x);
            } else if (specifier.equalsIgnoreCase("me") && source instanceof Locatable) {
                args.next();
                return Optional.of(((Locatable) source).getWorld().getProperties());
            } else {
                boolean firstOnly = false;
                if (specifier.endsWith(":first")) {
                    firstOnly = true;
                    specifier = specifier.substring(0, specifier.length() - 6);
                }
                args.next();

                @SuppressWarnings("unchecked")
                final DimensionType type = (DimensionType) (this.dimensionParameter.getValue(source, args, specifier).iterator().next());
                Iterable<WorldProperties> ret = Sponge.getGame().getServer().getAllWorldProperties().stream().filter(input -> input != null
                        && input.isEnabled() && input.getDimensionType().equals(type)).collect(Collectors.toList());
                return Optional.of(firstOnly ? ret.iterator().next() : ret);
            }
        }

        return super.getValue(source, args, context);
    }

    @Override
    public List<String> complete(CommandSource src, TokenizedArgs args, CommandExecutionContext context) {
        Iterable<String> choices = getCompletionChoices(src);
        final Optional<String> nextArg = args.nextIfPresent();
        if (nextArg.isPresent()) {
            choices = StreamSupport.stream(choices.spliterator(), false).filter(input -> getFormattedPattern(nextArg.get()).matcher(input).find())
                    .collect(Collectors.toList());
        }
        return ImmutableList.copyOf(choices);
    }

    private Iterable<String> getCompletionChoices(CommandSource source) {
        return Iterables.concat(getChoices(source), ImmutableSet.of("#first", "#me"),
                Sponge.getRegistry().getAllOf(DimensionType.class).stream().map(input2 -> "#" + input2.getId()).collect(Collectors.toList()));
    }

    @Override
    protected Iterable<String> getChoices(CommandSource source) {
        return Sponge.getGame().getServer().getAllWorldProperties().stream()
                .map(WorldProperties::getWorldName)
                .collect(Collectors.toList());
    }

    @Override
    protected Object getValue(String choice) throws IllegalArgumentException {
        Optional<WorldProperties> ret = Sponge.getGame().getServer().getWorldProperties(choice);
        if (!ret.isPresent()) {
            throw new IllegalArgumentException("Provided argument " + choice + " did not match a WorldProperties");
        }
        return ret.get();
    }
}
