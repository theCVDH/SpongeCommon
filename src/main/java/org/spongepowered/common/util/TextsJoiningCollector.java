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
package org.spongepowered.common.util;

import com.google.common.collect.Sets;
import org.spongepowered.api.text.Text;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * A {@link Collector} that allows {@link Text}s to be joined together in a
 * {@link Stream}
 */
public class TextsJoiningCollector implements Collector<Text, Text.Builder, Text> {

    private final Text joiningDelimiter;
    private final BiConsumer<Text.Builder, Text> accumulator;
    private final BinaryOperator<Text.Builder> combiner;

    /**
     * Joins texts together with no delimiting character.
     */
    public TextsJoiningCollector() {
        this(Text.EMPTY);
    }

    /**
     * Joins texts together with no delimiting character.
     */
    public TextsJoiningCollector(Text joiningDelimiter) {
        this.joiningDelimiter = joiningDelimiter;
        this.accumulator = ((builder, text) -> {
            if (!this.joiningDelimiter.isEmpty() && !builder.toString().isEmpty()) {
                builder.append(this.joiningDelimiter);
            }

            builder.append(text);
        });

        this.combiner = (b1, b2) -> {
            if (!this.joiningDelimiter.isEmpty()) {
                b1.append(this.joiningDelimiter);
            }

            return b1.append(b2.build());
        };
    }

    @Override
    public Supplier<Text.Builder> supplier() {
        return Text::builder;
    }

    @Override
    public BiConsumer<Text.Builder, Text> accumulator() {
        return this.accumulator;
    }

    @Override
    public BinaryOperator<Text.Builder> combiner() {
        return this.combiner;
    }

    @Override
    public Function<Text.Builder, Text> finisher() {
        return Text.Builder::build;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Sets.newHashSet();
    }

}
