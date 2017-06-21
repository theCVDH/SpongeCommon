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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.util.StartsWithPredicate;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;
import org.spongepowered.api.command.parameters.CommandExecutionContext;
import org.spongepowered.api.command.parameters.ParameterParseException;
import org.spongepowered.api.command.parameters.specification.CatalogedValueParameter;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

public class Vector3dValueParameter implements CatalogedValueParameter {

    private static final ImmutableSet<String> SPECIAL_TOKENS = ImmutableSet.of("#target", "#me");

    @Override
    public String getId() {
        return "sponge:vector3d";
    }

    @Override
    public String getName() {
        return "Vector3d parameter";
    }

    @Override
    public Optional<Object> getValue(CommandSource source, TokenizedArgs args, CommandExecutionContext context)
            throws ParameterParseException {
        String xStr;
        String yStr;
        String zStr;
        xStr = args.next();
        if (xStr.contains(",")) {
            String[] split = xStr.split(",");
            if (split.length != 3) {
                throw args.createError(t("Comma-separated location must have 3 elements, not %s", split.length));
            }
            xStr = split[0];
            yStr = split[1];
            zStr = split[2];
        } else if (xStr.equals("#target") && source instanceof Entity) {
            Optional<BlockRayHit<World>> hit = BlockRay
                    .from(((Entity) source))
                    .stopFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1))
                    .build()
                    .end();
            if (!hit.isPresent()) {
                throw args.createError(t("No target block is available! Stop stargazing!"));
            }
            return hit.map(BlockRayHit::getPosition);
        } else if (xStr.equalsIgnoreCase("#me") && source instanceof Locatable) {
            return Optional.of(((Locatable) source).getLocation().getPosition());
        } else {
            yStr = args.next();
            zStr = args.next();
        }
        final double x = parseRelativeDouble(args, xStr, source instanceof Locatable ? ((Locatable) source).getLocation().getX() : null);
        final double y = parseRelativeDouble(args, yStr, source instanceof Locatable ? ((Locatable) source).getLocation().getY() : null);
        final double z = parseRelativeDouble(args, zStr, source instanceof Locatable ? ((Locatable) source).getLocation().getZ() : null);

        return Optional.of(new Vector3d(x, y, z));
    }

    @Override
    public List<String> complete(CommandSource src, TokenizedArgs args, CommandExecutionContext context) throws ParameterParseException {
        Optional<String> arg = args.nextIfPresent();
        // Traverse through the possible arguments. We can't really complete arbitrary integers
        if (arg.isPresent()) {
            if (arg.get().startsWith("#")) {
                return SPECIAL_TOKENS.stream().filter(new StartsWithPredicate(arg.get())).collect(ImmutableList.toImmutableList());
            } else if (arg.get().contains(",") || !args.hasNext()) {
                return ImmutableList.of(arg.get());
            } else {
                arg = args.nextIfPresent();
                if (args.hasNext()) {
                    return ImmutableList.of(args.nextIfPresent().get());
                }
                return ImmutableList.of(arg.get());
            }
        }
        return ImmutableList.of();
    }

    private double parseRelativeDouble(TokenizedArgs args, String arg, @Nullable Double relativeTo) throws ParameterParseException {
        boolean relative = arg.startsWith("~");
        if (relative) {
            if (relativeTo == null) {
                throw args.createError(t("Relative position specified but source does not have a position"));
            }
            arg = arg.substring(1);
            if (arg.isEmpty()) {
                return relativeTo;
            }
        }
        try {
            double ret = Double.parseDouble(arg);
            return relative ? ret + relativeTo : ret;
        } catch (NumberFormatException e) {
            throw args.createError(t("Expected input %s to be a double, but was not", arg));
        }
    }
}
