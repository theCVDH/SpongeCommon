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
package org.spongepowered.common.command.parameters.flags;

import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.command.parameters.CommandExecutionContext;
import org.spongepowered.api.command.parameters.Parameter;
import org.spongepowered.api.command.parameters.flags.Flags;
import org.spongepowered.api.command.parameters.flags.UnknownFlagBehavior;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;
import org.spongepowered.common.util.TextsJoiningCollector;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpongeFlags implements Flags {

    private final List<String> primaryFlags;
    private final Map<String, Parameter> flags;
    private final UnknownFlagBehavior shortUnknown;
    private final UnknownFlagBehavior longUnknown;
    private final boolean anchorFlags;

    SpongeFlags(List<String> primaryFlags, Map<String, Parameter> flags,
            UnknownFlagBehavior shortUnknown, UnknownFlagBehavior longUnknown, boolean anchorFlags) {
        this.primaryFlags = primaryFlags;
        this.flags = flags;
        this.shortUnknown = shortUnknown;
        this.longUnknown = longUnknown;
        this.anchorFlags = anchorFlags;
    }

    @Override
    public void parse(CommandSource source, TokenizedArgs args, CommandExecutionContext context) {

    }

    @Override
    public Text getUsage(CommandSource src) {
        return this.primaryFlags.stream().map(this.flags::get).map(x -> x.getUsage(src)).filter(x -> !x.isEmpty())
                .collect(new TextsJoiningCollector(CommandMessageFormatting.SPACE_TEXT));
    }

    void populateBuilder(SpongeFlagsBuilder builder) {
        builder.updateFlags(this.primaryFlags, this.flags)
                .setUnknownShortFlagBehavior(this.shortUnknown)
                .setUnknownLongFlagBehavior(this.longUnknown)
                .setAnchorFlags(this.anchorFlags);
    }
}
