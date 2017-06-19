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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.command.parameters.CommandExecutionContext;
import org.spongepowered.api.command.parameters.Parameter;
import org.spongepowered.api.command.parameters.ParameterParseException;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;
import org.spongepowered.common.command.specification.SpongeCommandExecutionContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class SpongeSequenceParameter implements Parameter {

    private final List<Parameter> parameters;

    public SpongeSequenceParameter(Parameter... parameters) {
        this.parameters = Arrays.asList(parameters);
    }

    public SpongeSequenceParameter(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public Text getKey() {
        return Text.EMPTY;
    }

    @Override
    public void parse(CommandSource source, TokenizedArgs args, CommandExecutionContext context) throws ParameterParseException {
        boolean parseFlags = context instanceof SpongeCommandExecutionContext && !((SpongeCommandExecutionContext) context).getFlags().isAnchored();
        for (Parameter parameter : this.parameters) {
            parameter.parse(source, args, context);
            if (parseFlags) {
                ((SpongeCommandExecutionContext) context).getFlags().parse(source, args, context);
            }
        }
    }

    @Override
    public List<String> complete(CommandSource source, TokenizedArgs args, CommandExecutionContext context) throws ParameterParseException {
        for (Iterator<Parameter> it = this.parameters.iterator(); it.hasNext(); ) {
            Parameter element = it.next();
            Object startState = args.getState();
            try {
                element.parse(source, args, context);
                Object endState = args.getState();
                if (!args.hasNext()) {
                    args.setState(startState);
                    List<String> inputs = element.complete(source, args, context);
                    args.previous();
                    if (!inputs.contains(args.next())) { // Tabcomplete returns results to complete the last word in an argument.
                        // If the last word is one of the completions, the command is most likely complete
                        return inputs;
                    }

                    args.setState(endState);
                }
            } catch (ParameterParseException e) {
                args.setState(startState);
                return element.complete(source, args, context);
            }

            if (!it.hasNext()) {
                args.setState(startState);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.joinWith(Text.of(" "), this.parameters.stream()
                .map(x -> x.getUsage(source))
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toList()));
    }
}
