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
package org.spongepowered.common.command.parameters.tokenized;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.command.parameters.ParameterParseException;
import org.spongepowered.api.command.parameters.tokens.SingleArg;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

public class SpongeTokenizedArgs implements TokenizedArgs {

    private final UUID internalIdentifier = UUID.randomUUID();

    private final List<SingleArg> args;
    private final String raw;
    private ListIterator<SingleArg> iterator;

    public SpongeTokenizedArgs(List<SingleArg> args, String raw) {
        this.args = args;
        this.iterator = args.listIterator();
        this.raw = raw;
    }

    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    @Override
    public String next() throws ParameterParseException {
        if (hasNext()) {
            return this.iterator.next().getArg();
        }

        throw createError(t("Not enough arguments"));
    }

    @Override
    public Optional<String> nextIfPresent() {
        try {
            return Optional.of(next());
        } catch (ParameterParseException e) {
            return Optional.empty();
        }
    }

    @Override
    public String peek() throws ParameterParseException {
        if (hasNext()) {
            this.iterator.next();
            return this.iterator.previous().getArg();
        }

        throw createError(t("Not enough arguments"));
    }

    @Override
    public boolean hasPrevious() {
        return this.iterator.hasPrevious();
    }

    @Override
    public String previous() throws ParameterParseException {
        if (hasPrevious()) {
            return this.iterator.previous().getArg();
        }

        throw createError(t("Already at the beginning of the argument list."));
    }

    @Override
    public List<String> getAll() {
        return ImmutableList.copyOf(this.args.stream().map(SingleArg::getArg).collect(Collectors.toList()));
    }

    @Override
    public int getCurrentRawPosition() {
        if (hasNext()) {
            SingleArg next = this.iterator.next();
            this.iterator.previous();

            return next.getStartIndex();
        }

        return this.raw.length();
    }

    @Override
    public String getRaw() {
        return this.raw;
    }

    @Override
    public Object getState() {
        return new State(this.iterator.previousIndex(), this.internalIdentifier);
    }

    @Override
    public void setState(Object state) {
        Preconditions.checkArgument(state instanceof State, "This is not a state obtained from getState");

        State toRestore = (State) state;
        Preconditions.checkArgument(toRestore.internalIdentifier.equals(this.internalIdentifier), "This is not a state from this object");

        this.iterator = this.args.listIterator(toRestore.index);
    }

    // TODO
    @Override
    public ParameterParseException createError(Text message) {
        return null;
    }

    @Override
    public ParameterParseException createError(Text message, Throwable inner) {
        return null;
    }

    static class State {
        private final int index;
        private final UUID internalIdentifier;

        State(int index, UUID internalIdentifier) {
            this.index = index;
            this.internalIdentifier = internalIdentifier;
        }
    }
}
