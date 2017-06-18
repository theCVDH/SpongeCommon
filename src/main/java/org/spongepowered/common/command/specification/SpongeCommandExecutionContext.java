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
package org.spongepowered.common.command.specification;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.command.parameters.CommandExecutionContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpongeCommandExecutionContext implements CommandExecutionContext {

    private final UUID internalIdentifier = UUID.randomUUID();
    private final boolean isCompletion;
    @Nullable private final Location<World> targetBlock;

    private static String textToArgKey(Text key) {
        if (key instanceof TranslatableText) { // Use translation key
            return ((TranslatableText) key).getTranslation().getId();
        }

        return key.toPlain();
    }

    private final ArrayListMultimap<String, Object> parsedArgs;

    public SpongeCommandExecutionContext() {
        this(ArrayListMultimap.create(), false, null);
    }

    public SpongeCommandExecutionContext(ArrayListMultimap<String, Object> parsedArgs) {
        this(parsedArgs, false, null);
    }

    public SpongeCommandExecutionContext(ArrayListMultimap<String, Object> parsedArgs, boolean isCompletion, @Nullable Location<World> targetBlock) {
        this.targetBlock = targetBlock;
        this.isCompletion = isCompletion;
        this.parsedArgs = parsedArgs;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOneUnchecked(Text key) {
        return (T) getOne(key).get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOneUnchecked(String key) {
        return (T) getOne(key).get();
    }

    @Override
    public Optional<Location<World>> getTargetBlock() {
        return Optional.ofNullable(this.targetBlock);
    }

    @Override
    public boolean isCompletion() {
        return this.isCompletion;
    }

    @Override
    public boolean hasAny(Text key) {
        return hasAny(textToArgKey(key));
    }

    @Override
    public boolean hasAny(String key) {
        return this.parsedArgs.containsKey(key);
    }

    @Override
    public <T> Optional<T> getOne(Text key) {
        return getOne(textToArgKey(key));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOne(String key) {
        Collection<Object> values = this.parsedArgs.get(key);
        if (values.size() != 1) {
            return Optional.empty();
        }
        return Optional.ofNullable((T) values.iterator().next());
    }

    @Override
    public <T> Collection<T> getAll(Text key) {
        return getAll(textToArgKey(key));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getAll(String key) {
        return Collections.unmodifiableCollection((Collection<T>) this.parsedArgs.get(key));
    }

    @Override public void putEntry(Text key, Object value) {
        putEntry(textToArgKey(key), value);
    }

    @Override
    public void putEntry(String key, Object value) {
        checkNotNull(value, "value");
        this.parsedArgs.put(key, value);
    }

    @Override
    public Object getState() {
        return new State(this.internalIdentifier, ImmutableMultimap.copyOf(this.parsedArgs));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setState(Object state) {
        Preconditions.checkArgument(state instanceof State, "This is not a state obtained from getState");
        State toRestore = (State) state;
        Preconditions.checkArgument(toRestore.internalIdentifier.equals(this.internalIdentifier), "This is not a state from this object");

        this.parsedArgs.clear();
        this.parsedArgs.putAll(((State) state).contextState);

    }

    private static class State {
        private final UUID internalIdentifier;
        private final Multimap<String, Object> contextState;

        private State(UUID internalIdentifier, Multimap<String, Object> contextState) {
            this.internalIdentifier = internalIdentifier;
            this.contextState = contextState;
        }
    }

}
