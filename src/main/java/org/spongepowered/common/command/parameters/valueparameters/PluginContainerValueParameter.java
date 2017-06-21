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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.command.parameters.specification.CatalogedValueParameter;
import org.spongepowered.api.command.parameters.specification.impl.PatternMatchingValueParameter;

import java.util.Optional;
import java.util.stream.Collectors;

public class PluginContainerValueParameter extends PatternMatchingValueParameter implements CatalogedValueParameter {

    @Override
    protected Iterable<String> getChoices(CommandSource source) {
        return Sponge.getPluginManager().getPlugins().stream().map(PluginContainer::getId).collect(Collectors.toList());
    }

    @Override
    protected Object getValue(String choice) throws IllegalArgumentException {
        Optional<PluginContainer> plugin = Sponge.getPluginManager().getPlugin(choice);
        return plugin.orElseThrow(() -> new IllegalArgumentException("Plugin " + choice + " was not found"));
    }

    @Override
    public String getId() {
        return "sponge:plugin";
    }

    @Override
    public String getName() {
        return "Plugin Container parameter";
    }
}
