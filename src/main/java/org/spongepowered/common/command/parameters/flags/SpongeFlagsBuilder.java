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
package org.spongepowered.common.command.parameters.flags;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.spongepowered.api.command.parameters.Parameter;
import org.spongepowered.api.command.parameters.flags.Flags;
import org.spongepowered.api.command.parameters.flags.UnknownFlagBehavior;
import org.spongepowered.api.command.parameters.flags.UnknownFlagBehaviors;

import java.util.Locale;
import java.util.Map;

public class SpongeFlagsBuilder implements Flags.Builder {

    private final Map<String, String> flagAliasToFlag = Maps.newHashMap();
    private final Map<String, Parameter> valueFlags = Maps.newHashMap();
    private final Map<String, String> permissionFlags = Maps.newHashMap();
    private UnknownFlagBehavior shortUnknown = UnknownFlagBehaviors.ERROR;
    private UnknownFlagBehavior longUnknown = UnknownFlagBehaviors.ERROR;
    private boolean anchorFlags = false;

    @Override
    public Flags.Builder flag(String... specs) {
        Preconditions.checkArgument(specs.length > 0, "Specs must have at least one entry.");
        storeAliases(specs);
        return this;
    }

    @Override
    public Flags.Builder permissionFlag(String flagPermission, String... specs) {
        String first = storeAliases(specs);
        this.permissionFlags.put(first, flagPermission);
        return this;
    }

    @Override
    public Flags.Builder valueFlag(Parameter value, String... specs) {
        String first = storeAliases(specs);
        this.valueFlags.put(first, value);
        return this;
    }

    @Override
    public Flags.Builder setUnknownLongFlagBehavior(UnknownFlagBehavior behavior) {
        Preconditions.checkNotNull(behavior);
        this.shortUnknown = behavior;
        return this;
    }

    @Override
    public Flags.Builder setUnknownShortFlagBehavior(UnknownFlagBehavior behavior) {
        Preconditions.checkNotNull(behavior);
        this.longUnknown = behavior;
        return this;
    }

    @Override
    public Flags.Builder setAnchorFlags(boolean anchorFlags) {
        this.anchorFlags = anchorFlags;
        return this;
    }

    @Override
    public Flags build() {
        return new SpongeFlags(
                ImmutableMap.copyOf(this.flagAliasToFlag),
                ImmutableMap.copyOf(this.valueFlags),
                ImmutableMap.copyOf(this.permissionFlags),
                this.shortUnknown,
                this.longUnknown,
                this.anchorFlags
        );
    }

    @Override
    public Flags.Builder from(Flags value) {
        // TODO: From SpongeFlags
        return this;
    }

    @Override
    public Flags.Builder reset() {
        this.flagAliasToFlag.clear();
        this.valueFlags.clear();
        this.permissionFlags.clear();
        this.shortUnknown = UnknownFlagBehaviors.ERROR;
        this.longUnknown = UnknownFlagBehaviors.ERROR;
        this.anchorFlags = false;
        return this;
    }

    private String storeAliases(String[] specs) {
        Preconditions.checkNotNull(specs);
        Preconditions.checkArgument(specs.length > 0);

        // First spec
        String first = null;

        // Put the flag aliases in
        for (String spec : specs) {
            if (spec.length() > 2 && spec.startsWith("-")) {
                // Long flag.
                if (first == null) {
                    first = spec.toLowerCase(Locale.ENGLISH).substring(1);
                }

                this.flagAliasToFlag.put(spec.substring(1), first);
            } else {
                // Short flags - remove "-" if necessary.
                for (char c : spec.toLowerCase(Locale.ENGLISH).replaceAll("-", "").toCharArray()) {
                    if (first == null) {
                        first = String.valueOf(c).toLowerCase(Locale.ENGLISH);
                    }

                    this.flagAliasToFlag.put(String.valueOf(c).toLowerCase(Locale.ENGLISH), first);
                }
            }
        }

        //noinspection ConstantConditions
        return first;
    }
}
