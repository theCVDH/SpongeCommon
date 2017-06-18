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

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameters.CommandExecutionContext;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.command.parameters.ParameterParseException;
import org.spongepowered.api.command.parameters.specification.impl.SelectorValueParameter;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class ReturnSourceParameter extends SelectorValueParameter {

    @Nullable private final Class<? extends Entity> sourceOnFailType;

    public ReturnSourceParameter(Class<? extends Entity> selectorTarget, @Nullable Class<? extends Entity> sourceOnFailType) {
        super(selectorTarget);
        this.sourceOnFailType = sourceOnFailType;
    }

    @Override
    public Optional<Object> getValue(CommandSource source, TokenizedArgs args, CommandExecutionContext context)
            throws ParameterParseException {
        try {
            return super.getValue(source, args, context);
        } catch (ParameterParseException e) {
            if (this.sourceOnFailType != null &&this.sourceOnFailType.isInstance(source)) {
                return Optional.of(source);
            }

            throw e;
        }
    }

    @Override public Text getUsage(Text key, CommandSource source) {
        return this.sourceOnFailType != null && this.sourceOnFailType.isInstance(source) ? Text.of("[", key, "]") : key;
    }
}
