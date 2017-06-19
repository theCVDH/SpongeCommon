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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.parameters.CommandExecutionContext;
import org.spongepowered.api.command.parameters.ParameterParseException;
import org.spongepowered.api.command.parameters.tokens.InputTokenizer;
import org.spongepowered.api.command.specification.ChildExceptionBehavior;
import org.spongepowered.api.command.specification.CommandExecutor;
import org.spongepowered.api.command.specification.CommandSpecification;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.command.parameters.Parameter;
import org.spongepowered.api.command.parameters.flags.Flags;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;
import org.spongepowered.common.command.parameters.flags.NoFlags;
import org.spongepowered.common.command.parameters.tokenized.SpongeTokenizedArgs;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SpongeCommandSpecification implements CommandSpecification {

    private final Parameter parameters;
    private final Map<String, CommandCallable> children;
    private final ChildExceptionBehavior childExceptionBehavior;
    private final InputTokenizer inputTokenizer;
    private final Flags flags;
    private final CommandExecutor executor;
    @Nullable private final String permission;
    @Nullable private final Text shortDescription;
    @Nullable private final Text extendedDescription;
    private final boolean requirePermissionForChildren;

    SpongeCommandSpecification(Iterable<Parameter> parameters,
            Map<String, CommandCallable> children,
            ChildExceptionBehavior childExceptionBehavior,
            InputTokenizer inputTokenizer,
            Flags flags,
            CommandExecutor executor, @Nullable String permission,
            @Nullable Text shortDescription,
            @Nullable Text extendedDescription,
            boolean requirePermissionForChildren) {
        this.parameters = Parameter.seq(parameters);
        this.children = children;
        this.childExceptionBehavior = childExceptionBehavior;
        this.inputTokenizer = inputTokenizer;
        this.flags = flags;
        this.executor = executor;
        this.permission = permission;
        this.shortDescription = shortDescription;
        this.extendedDescription = extendedDescription;
        this.requirePermissionForChildren = requirePermissionForChildren;
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        if (this.requirePermissionForChildren) {
            checkPermission(source);
        }

        // Step one, create the TokenizedArgs and CommandExecutionContext
        TokenizedArgs args = new SpongeTokenizedArgs(this.inputTokenizer.tokenize(arguments, true), arguments);
        SpongeCommandExecutionContext context = new SpongeCommandExecutionContext();
        CommandException childException = null;

        // Step two, children. If we have any, we parse them now.
        // TODO: Integrate dispatcher?

        // Step three, this command
        if (!this.requirePermissionForChildren) {
            checkPermission(source);
        }

        populateContext(source, args, context);
        return null;
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition)
            throws CommandException {
        checkNotNull(source, "source");
        SpongeTokenizedArgs args = new SpongeTokenizedArgs(this.inputTokenizer.tokenize(arguments, true), arguments);
        CommandExecutionContext context = new SpongeCommandExecutionContext(null, true, targetPosition);
        return ImmutableList.copyOf(this.parameters.complete(source, args, context));
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return this.permission == null || source.hasPermission(this.permission);
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.ofNullable(this.shortDescription);
    }

    @Override
    public Optional<Text> getExtendedDescription(CommandSource source) {
        return Optional.ofNullable(this.extendedDescription);
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        // TODO: This needs improving with subcommands etc.
        checkNotNull(source, "source");
        Text.Builder builder = Text.builder();
        this.getShortDescription(source).ifPresent((a) -> builder.append(a, Text.NEW_LINE));
        builder.append(getUsage(source));
        this.getExtendedDescription(source).ifPresent((a) -> builder.append(Text.NEW_LINE, a));
        return Optional.of(builder.build());
    }

    @Override
    public Text getUsage(CommandSource source) {
        if (this.flags instanceof NoFlags) {
            return this.parameters.getUsage(source);
        }

        return Text.joinWith(CommandMessageFormatting.SPACE_TEXT, this.flags.getUsage(source), this.parameters.getUsage(source));
    }

    private void checkPermission(CommandSource source) throws CommandPermissionException {
        if (!testPermission(source)) {
            throw new CommandPermissionException();
        }
    }

    /**
     * Process this command with existing arguments and context objects.
     *
     * @param source The source to populate the context with
     * @param args The arguments to process with
     * @param context The context to put data in
     * @throws ParameterParseException if an invalid argument is provided
     */
    private void populateContext(CommandSource source, TokenizedArgs args, CommandExecutionContext context) throws ParameterParseException {
        // First, the flags.
        this.flags.parse(source, args, context);
        this.parameters.parse(source, args, context);
    }

}
