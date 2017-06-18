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
package org.spongepowered.common.command.parameters.flags.behaviors;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameters.CommandExecutionContext;
import org.spongepowered.api.command.parameters.ParameterParseException;
import org.spongepowered.api.command.parameters.flags.UnknownFlagBehavior;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

public class AcceptNonValueBehavior implements UnknownFlagBehavior {

    @Override
    public void parse(CommandSource source, TokenizedArgs args, CommandExecutionContext context, Object tokenizedArgsPreviousState,
                      Object contextPreviousState) throws ParameterParseException {
        // Rewind, parse it.
        args.setState(tokenizedArgsPreviousState);
        String arg = args.next();
        if (arg.startsWith("--")) {
            context.putEntry(arg.substring(2), true);
            return;
        }

        if (arg.length() != 2) {
            // The length of the argument is incorrect.
            throw args.createError(t("The unknown short flag %s cannot have a value (%s).", arg.substring(0, 2), arg.substring(2)));
        }

        context.putEntry(String.valueOf(arg.charAt(1)), true);
    }

    @Override
    public String getId() {
        return "sponge:acceptnonvalue";
    }

    @Override
    public String getName() {
        return "Accept flag (no value)";
    }
}
