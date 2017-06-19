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

import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameters.CommandExecutionContext;
import org.spongepowered.api.command.parameters.Parameter;
import org.spongepowered.api.command.parameters.ParameterParseException;
import org.spongepowered.api.command.parameters.flags.Flags;
import org.spongepowered.api.command.parameters.flags.UnknownFlagBehavior;
import org.spongepowered.api.command.parameters.flags.UnknownFlagBehaviors;
import org.spongepowered.api.command.parameters.specification.ParsingContext;
import org.spongepowered.api.command.parameters.specification.ValueParameterModifier;
import org.spongepowered.api.command.parameters.specification.ValueParser;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class SpongeFlagsBuilder implements Flags.Builder {

    private final static ValueParser MARK_TRUE = (source, args, context) -> Optional.of(true);

    private final List<String> primaryFlags = Lists.newArrayList();
    private final Map<String, Parameter> flags = Maps.newHashMap();
    private UnknownFlagBehavior shortUnknown = UnknownFlagBehaviors.ERROR;
    private UnknownFlagBehavior longUnknown = UnknownFlagBehaviors.ERROR;
    private boolean anchorFlags = false;

    @Override
    public Flags.Builder flag(String... specs) {
        Preconditions.checkArgument(specs.length > 0, "Specs must have at least one entry.");
        List<String> aliases = storeAliases(specs);
        Text usage = Text.of("[" + getFlag(aliases.get(0)) + "]");
        Parameter markTrue = Parameter.builder().key(aliases.get(0)).parser(MARK_TRUE)
                .usage((source, current) -> usage).build();
        this.primaryFlags.add(aliases.get(0));
        for (String alias : aliases) {
            this.flags.put(alias, markTrue);
        }
        return this;
    }

    @Override
    public Flags.Builder permissionFlag(String flagPermission, String... specs) {
        Preconditions.checkArgument(specs.length > 0, "Specs must have at least one entry.");
        List<String> aliases = storeAliases(specs);
        final String first = getFlag(aliases.get(0));
        Text usage = Text.of(CommandMessageFormatting.LEFT_SQUARE, getFlag(aliases.get(0)), CommandMessageFormatting.RIGHT_SQUARE);
        Parameter perm = Parameter.builder().key(aliases.get(0)).parser(MARK_TRUE)
                .addModifiers(new PermissionModifier(first, flagPermission, usage)).build();
        this.primaryFlags.add(aliases.get(0));
        for (String alias : aliases) {
            this.flags.put(alias, perm);
        }
        return this;
    }

    @Override
    public Flags.Builder valueFlag(Parameter value, String... specs) {
        Preconditions.checkArgument(specs.length > 0, "Specs must have at least one entry.");
        List<String> aliases = storeAliases(specs);
        this.primaryFlags.add(aliases.get(0));
        for (String alias : aliases) {
            this.flags.put(alias, value);
        }
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
                ImmutableList.copyOf(this.primaryFlags),
                ImmutableMap.copyOf(this.flags),
                this.shortUnknown,
                this.longUnknown,
                this.anchorFlags
        );
    }

    @Override
    public Flags.Builder from(Flags value) {
        if (!(value instanceof SpongeFlags)) {
            throw new IllegalArgumentException("value must be a SpongeFlags object");
        }

        ((SpongeFlags) value).populateBuilder(this);

        return this;
    }

    // For use in from
    SpongeFlagsBuilder updateFlags(List<String> primaryFlags, Map<String, Parameter> flags) {
        this.primaryFlags.clear();
        this.primaryFlags.addAll(primaryFlags);
        this.flags.clear();
        this.flags.putAll(flags);
        return this;
    }

    @Override
    public Flags.Builder reset() {
        this.flags.clear();
        this.shortUnknown = UnknownFlagBehaviors.ERROR;
        this.longUnknown = UnknownFlagBehaviors.ERROR;
        this.anchorFlags = false;
        return this;
    }

    private String getFlag(String flag) {
        if (flag.length() == 1) {
            return "-" + flag;
        } else {
            return "--" + flag;
        }
    }

    private List<String> storeAliases(String[] specs) {
        Preconditions.checkNotNull(specs);
        Preconditions.checkArgument(specs.length > 0);

        List<String> aliases = Lists.newArrayList();

        // Put the flag aliases in
        for (String spec : specs) {
            if (spec.length() > 2 && spec.startsWith("-")) {
                aliases.add(spec.substring(1));
            } else {
                // Short flags - remove "-" if necessary.
                for (char c : spec.toLowerCase(Locale.ENGLISH).replaceAll("-", "").toCharArray()) {
                    aliases.add(String.valueOf(c).toLowerCase(Locale.ENGLISH));
                }
            }
        }

        return aliases;
    }

    private static class PermissionModifier implements ValueParameterModifier {

        private final String flag;
        private final String flagPermission;
        private final Text usage;

        private PermissionModifier(String flag, String flagPermission, Text usage) {
            this.flag = flag;
            this.flagPermission = flagPermission;
            this.usage = usage;
        }

        @Override
        public void onParse(Text key, CommandSource source, TokenizedArgs args, CommandExecutionContext context, ParsingContext parsingContext)
                throws ParameterParseException {
            if (source.hasPermission(this.flagPermission)) {
                parsingContext.next();
            } else {
                throw args.createError(t("You do not have permission to use the flag %s", this.flag));
            }
        }

        @Override
        public Text getUsage(Text key, CommandSource source, Text currentUsage) {
            if (!source.hasPermission(this.flagPermission)) {
                return Text.EMPTY;
            }

            return usage;
        }
    }

    private static class UsageModifier implements ValueParameterModifier {

        private final Text flag;

        private UsageModifier(Text flag) {
            this.flag = flag;
        }

        @Override
        public void onParse(Text key, CommandSource source, TokenizedArgs args, CommandExecutionContext context, ParsingContext parsingContext)
                throws ParameterParseException {
            parsingContext.next(); // Not changing anything here.
        }

        @Override
        public Text getUsage(Text key, CommandSource source, Text currentUsage) {
            if (currentUsage.isEmpty()) {
                return currentUsage;
            }

            return Text.of(CommandMessageFormatting.LEFT_SQUARE, this.flag, CommandMessageFormatting.SPACE_TEXT, currentUsage,
                    CommandMessageFormatting.RIGHT_SQUARE);
        }
    }
}
