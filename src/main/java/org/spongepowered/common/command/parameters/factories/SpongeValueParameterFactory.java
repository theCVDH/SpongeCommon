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
package org.spongepowered.common.command.parameters.factories;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.command.parameters.specification.ValueParameter;
import org.spongepowered.api.command.parameters.specification.factories.ValueParameterFactory;
import org.spongepowered.common.command.parameters.valueparameters.CatalogTypeValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.ChoicesValueParameter;
import org.spongepowered.common.command.parameters.valueparameters.LiteralValueParameter;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SpongeValueParameterFactory implements ValueParameterFactory {

    @Override
    public <T extends CatalogType> ValueParameter catalogedElement(Class<T> catalogType, String... prefixes) {
        return new CatalogTypeValueParameter<>(catalogType, prefixes);
    }

    @Override public ValueParameter choices(boolean showUsage, Supplier<Map<String, ?>> choices) {
        return new ChoicesValueParameter(choices, showUsage ? Tristate.UNDEFINED : Tristate.FALSE);
    }

    @Override public ValueParameter choices(boolean showUsage, Supplier<Collection<String>> choices, Function<String, ?> valueFunction) {
        return null;
    }

    @Override
    public <T extends Enum<T>> ValueParameter enumValue(Class<T> enumClass) {
        // Enums shouldn't change, so we generate it out of the supplier.
        Map<String, T> map =
                EnumSet.allOf(enumClass).stream().collect(Collectors.toMap(x -> x.name().toLowerCase(Locale.ENGLISH), Function.identity()));
        return new ChoicesValueParameter(() -> map, Tristate.TRUE);
    }

    @Override
    public ValueParameter literal(@Nullable Object returnedValue, Supplier<Iterable<String>> literalSupplier) {
        return new LiteralValueParameter(returnedValue, literalSupplier);
    }
}
