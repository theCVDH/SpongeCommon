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
package org.spongepowered.common.command.parameters;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameters.CommandExecutionContext;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.command.parameters.ParameterParseException;
import org.spongepowered.api.command.parameters.specification.ParsingContext;
import org.spongepowered.api.command.parameters.specification.ValueParameter;
import org.spongepowered.api.command.parameters.specification.ValueParameterModifier;

import java.util.ListIterator;

public class SpongeParsingContext implements ParsingContext {

    private final Text key;
    private final CommandSource source;
    private final TokenizedArgs args;
    private final CommandExecutionContext context;
    private final ListIterator<ValueParameterModifier> modifierListIterator;
    private final ValueParameter valueParameter;

    public SpongeParsingContext(Text key, CommandSource source, TokenizedArgs args, CommandExecutionContext context,
                                ListIterator<ValueParameterModifier> modifierListIterator, ValueParameter valueParameter) {
        this.key = key;
        this.source = source;
        this.args = args;
        this.context = context;
        this.modifierListIterator = modifierListIterator;
        this.valueParameter = valueParameter;
    }

    @Override public void next() throws ParameterParseException {
        if (this.modifierListIterator.hasNext()) {
            try {
                this.modifierListIterator.next().onParse(this.key, this.source, this.args, this.context, this);
            } finally {
                this.modifierListIterator.previous();
            }
        } else {
            this.context.putEntry(this.key, this.valueParameter.getValue(this.source, this.args, this.context).orElseThrow(() ->
                    args.createError(Text.of(TextColors.RED, "Could not parse result for ", this.key))));
        }
    }
}
