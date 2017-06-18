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

import org.spongepowered.api.command.parameters.Parameter;
import org.spongepowered.api.command.parameters.ParameterParseException;
import org.spongepowered.api.command.parameters.specification.ValueParameter;
import org.spongepowered.api.command.parameters.specification.ValueParameterModifier;

import java.util.List;

class SpongeParameter implements Parameter {

    private final Text key;
    private final List<ValueParameterModifier> modifiers;
    private final ValueParameter valueParameter;

    public SpongeParameter(Text key, List<ValueParameterModifier> modifiers, ValueParameter valueParameter) {
        this.key = key;
        this.modifiers = modifiers;
        this.valueParameter = valueParameter;
    }

    @Override
    public Text getKey() {
        return this.key;
    }

    @Override
    public void parse(CommandSource source, TokenizedArgs args, CommandExecutionContext context) throws ParameterParseException {
        new SpongeParsingContext(this.key, source, args, context, this.modifiers.listIterator(), this.valueParameter).next();
    }

    @Override
    public List<String> complete(CommandSource source, TokenizedArgs args, CommandExecutionContext context) throws ParameterParseException {
        List<String> completions = this.valueParameter.complete(source, args, context);
        for (ValueParameterModifier modifier : this.modifiers) {
            completions = modifier.complete(source, args, context, completions);
        }

        return completions;
    }

    @Override
    public Text getUsage(CommandSource source) {
        Text usage = this.valueParameter.getUsage(this.key, source);
        for (ValueParameterModifier modifier : this.modifiers) {
            usage = modifier.getUsage(this.key, source, usage);
        }
        return usage;
    }

}
