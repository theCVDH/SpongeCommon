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

import com.google.common.base.Preconditions;
import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameters.flags.Flags;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.command.parameters.CommandExecutionContext;
import org.spongepowered.api.command.parameters.Parameter;
import org.spongepowered.api.command.parameters.ParameterParseException;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

import javax.annotation.Nullable;

public class SpongeParameterSequenceBuilder implements Parameter.SequenceBuilder {

    private final List<Parameter> parameters = new ArrayList<>();

    private boolean requireAll = true;

    @Override
    public Parameter.SequenceBuilder requireAll(boolean requireAll) {
        this.requireAll = requireAll;
        return this;
    }

    @Override
    public Parameter.SequenceBuilder add(Iterable<Parameter> parameters) {
        for (Parameter parameter : parameters) {
            this.parameters.add(parameter);
        }
        return this;
    }

    @Override
    public Parameter.SequenceBuilder add(Parameter... parameters) {
        this.parameters.addAll(Arrays.asList(parameters));
        return this;
    }

    @Override
    public Parameter build() {
        Preconditions.checkState(!this.parameters.isEmpty(), "There must be at least one parameter.");
        if (this.parameters.size() == 1) {
            return this.parameters.get(0);
        }

        if (this.requireAll) {
            return new SpongeSequenceParameter(this.parameters);
        } else {
            return new SpongeFirstOfParameter(this.parameters);
        }
    }

    @Override
    public Parameter.SequenceBuilder from(Parameter value) {
        // TODO: From
        throw new UnsupportedOperationException();
    }

    @Override
    public Parameter.SequenceBuilder reset() {
        this.parameters.clear();
        return this;
    }

    private static class SpongeFirstOfParameter implements Parameter {

        private final List<Parameter> parameters;

        SpongeFirstOfParameter(List<Parameter> parameters) {
            this.parameters = parameters;
        }

        @Override
        public Text getKey() {
            return Text.EMPTY;
        }

        @Override
        public void parse(CommandSource source, TokenizedArgs args, CommandExecutionContext context) throws ParameterParseException {
            List<ParameterParseException> exceptions = new ArrayList<>();
            for (Parameter parameter : this.parameters) {
                Object state = context.getState();
                try {
                    parameter.parse(source, args, context);
                    return;
                } catch (ParameterParseException ex) {
                    context.setState(state);
                }
            }

            // If we get here, nothing parsed, so we throw an exception.
            // TODO: Exception
            throw args.createError(t("Could not parse the parameter.", exceptions));
        }

        @Override
        public List<String> complete(CommandSource source, TokenizedArgs args, CommandExecutionContext context) throws ParameterParseException {
            List<String> completions = new ArrayList<>();
            for (Parameter parameter : this.parameters) {
                Object state = context.getState();
                try {
                    completions.addAll(parameter.complete(source, args, context));
                } finally {
                    context.setState(state);
                }
            }

            return completions;
        }

        @Override
        public Text getUsage(CommandSource source) {
            Text.Builder builder = CommandMessageFormatting.LEFT_PARENTHESIS.toBuilder();
            boolean isFirst = true;

            for (Parameter parameter : this.parameters) {
                Text usage = parameter.getUsage(source);
                if (!usage.isEmpty()) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        builder.append(CommandMessageFormatting.PIPE_TEXT);
                    }
                    builder.append(usage);
                }
            }
            return builder.append(CommandMessageFormatting.RIGHT_PARENTHESIS).build();
        }
    }

}
