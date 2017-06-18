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

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameters.tokens.InputTokenizer;
import org.spongepowered.api.command.specification.ChildExceptionBehavior;
import org.spongepowered.api.command.specification.CommandSpecification;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.command.parameters.Parameter;
import org.spongepowered.api.command.parameters.flags.Flags;
import org.spongepowered.api.command.parameters.tokens.TokenizedArgs;
import org.spongepowered.common.command.specification.SpongeCommandExecutionContext;
import org.spongepowered.common.command.parameters.tokenized.SpongeTokenizedArgs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SpongeCommandSpecification implements CommandSpecification {

    private final Iterable<Parameter> parameters;
    private final Map<String, CommandCallable> children;
    private final ChildExceptionBehavior behavior;
    private final InputTokenizer inputTokenizer;
    @Nullable private final Flags flags;
    @Nullable private final String permission;
    @Nullable private final Text shortDescription;
    @Nullable private final Text extendedDescription;
    private final boolean requirePermissionForChildren;

    SpongeCommandSpecification(Iterable<Parameter> parameters,
                               Map<String, CommandCallable> children,
                               ChildExceptionBehavior behavior,
                               InputTokenizer inputTokenizer,
                               @Nullable Flags flags,
                               @Nullable String permission,
                               @Nullable Text shortDescription,
                               @Nullable Text extendedDescription,
                               boolean requirePermissionForChildren) {
        this.parameters = parameters;
        this.children = children;
        this.behavior = behavior;
        this.inputTokenizer = inputTokenizer;
        this.flags = flags;
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

        return null;
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition)
            throws CommandException {
        return null;
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
    public Optional<Text> getHelp(CommandSource source) {
        return null;
    }

    @Override
    public Text getUsage(CommandSource source) {
        return null;
    }

    private void checkPermission(CommandSource source) throws CommandPermissionException {
        if (!testPermission(source)) {
            throw new CommandPermissionException();
        }
    }

}
