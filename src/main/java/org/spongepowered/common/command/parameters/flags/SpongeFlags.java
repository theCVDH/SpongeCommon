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

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.command.parameters.CommandExecutionContext;
import org.spongepowered.api.command.parameters.Parameter;
import org.spongepowered.api.command.parameters.flags.Flags;
import org.spongepowered.api.command.parameters.flags.UnknownFlagBehavior;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;

import java.util.Map;

public class SpongeFlags implements Flags {

    private final Map<String, String> flagAliasToFlag;
    private final Map<String, Parameter> valueFlags;
    private final Map<String, String> permissionFlags;
    private final UnknownFlagBehavior shortUnknown;
    private final UnknownFlagBehavior longUnknown;
    private final boolean anchorFlags;

    SpongeFlags(Map<String, String> flagAliasToFlag,
            Map<String, Parameter> valueFlags, Map<String, String> permissionFlags,
            UnknownFlagBehavior shortUnknown, UnknownFlagBehavior longUnknown, boolean anchorFlags) {
        this.flagAliasToFlag = flagAliasToFlag;
        this.valueFlags = valueFlags;
        this.permissionFlags = permissionFlags;
        this.shortUnknown = shortUnknown;
        this.longUnknown = longUnknown;
        this.anchorFlags = anchorFlags;
    }

    @Override
    public void parse(CommandSource source, TokenizedArgs args, CommandExecutionContext context) {

    }

    @Override
    public Text getUsage(CommandSource src) {
        return null;
    }
}
