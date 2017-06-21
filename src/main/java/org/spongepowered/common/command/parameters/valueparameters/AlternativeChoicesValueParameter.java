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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.command.parameters.CommandExecutionContext;
import org.spongepowered.api.command.parameters.ParameterParseException;
import org.spongepowered.api.command.parameters.specification.ValueParameter;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

public class AlternativeChoicesValueParameter implements ValueParameter {

    private static final int CUTOFF = 5;

    private final Supplier<Collection<String>> choicesSupplier;
    private final Function<String, ?> valuesFunction;
    private final Tristate includeChoicesInUsage;

    public AlternativeChoicesValueParameter(Supplier<Collection<String>> choicesSupplier, Function<String, ?> valuesFunction,
            Tristate includeChoicesInUsage) {
        this.choicesSupplier = choicesSupplier;
        this.valuesFunction = valuesFunction;
        this.includeChoicesInUsage = includeChoicesInUsage;
    }

    @Override
    public Optional<Object> getValue(CommandSource source, TokenizedArgs args, CommandExecutionContext context) throws ParameterParseException {
        final String nextArg = args.next();
        return Optional.ofNullable(getValue(nextArg, args));
    }

    @Nullable
    public Object getValue(String nextArg, TokenizedArgs args) throws ParameterParseException {
        Collection<String> choices = this.choicesSupplier.get();
        String result = choices.stream().filter(k -> k.equalsIgnoreCase(nextArg)).findFirst()
                .orElseThrow(() -> args.createError(t("Argument was not a valid choice. Valid choices: %s",
                        choices.stream().map(x -> x.toLowerCase(Locale.ENGLISH))
                                .sorted().collect(Collectors.joining(", ")))));
        return this.valuesFunction.apply(result);
    }

    @Override
    public List<String> complete(CommandSource source, TokenizedArgs args, CommandExecutionContext context) throws ParameterParseException {
        final String nextArg = args.peek();
        return this.choicesSupplier.get().stream().filter(x -> x.toLowerCase(Locale.ENGLISH)
                .startsWith(nextArg.toLowerCase(Locale.ENGLISH))).sorted().collect(Collectors.toList());
    }

    @Override
    public Text getUsage(Text key, CommandSource source) {
        if (this.includeChoicesInUsage != Tristate.FALSE) {
            List<String> choices = this.choicesSupplier.get().stream().sorted().collect(Collectors.toList());
            if (this.includeChoicesInUsage.asBoolean() || choices.size() < CUTOFF) {
                return Text.of("<", String.join("|", choices), ">");
            }
        }
        return key;
    }
}
