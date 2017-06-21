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
package org.spongepowered.common.command.parameters.modifiers;

import com.google.common.base.Preconditions;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameters.CommandExecutionContext;
import org.spongepowered.api.command.parameters.ParameterParseException;
import org.spongepowered.api.command.parameters.specification.ParsingContext;
import org.spongepowered.api.command.parameters.specification.ValueParameterModifier;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;
import org.spongepowered.api.text.Text;

public class DefaultValueModifier implements ValueParameterModifier {

    private final Object defaultValue;

    public DefaultValueModifier(Object defaultValue) {
        Preconditions.checkNotNull(defaultValue, "The default cannot be null.");
        this.defaultValue = defaultValue;
    }

    @Override
    public void onParse(Text key, CommandSource source, TokenizedArgs args, CommandExecutionContext context, ParsingContext parsingContext)
            throws ParameterParseException {
        parsingContext.next();

        if (!context.hasAny(key)) {
            context.putEntry(key, this.defaultValue);
        }
    }

}
