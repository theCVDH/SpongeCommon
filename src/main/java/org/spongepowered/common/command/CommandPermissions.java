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
package org.spongepowered.common.command;

import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.SpongeImpl;

import java.util.Optional;
import java.util.function.BiFunction;

public final class CommandPermissions {

    private static final String COMMAND_BLOCK_COMMAND = "";
    private static final String COMMAND_BLOCK_PERMISSION = "minecraft.commandblock";
    private static final int COMMAND_BLOCK_LEVEL = 2;
    private static final String SELECTOR_COMMAND = "@";
    private static final String SELECTOR_PERMISSION = "minecraft.selector";
    private static final int SELECTOR_LEVEL = 2;

    private CommandPermissions() {
    }

    public static boolean testPermission(CommandSource source, String commandName) {
        if (commandName.equals(CommandPermissions.SELECTOR_COMMAND)) {
            return source.hasPermission(CommandPermissions.SELECTOR_PERMISSION);
        }
        if (commandName.equals(CommandPermissions.COMMAND_BLOCK_COMMAND)) {
            return source.hasPermission(CommandPermissions.COMMAND_BLOCK_PERMISSION);
        }
        Optional<? extends CommandMapping> mapping = SpongeImpl.getGame().getCommandManager().get(commandName);
        if (mapping.isPresent()) {
            Command callable = mapping.get().getCallable();
            if (callable instanceof MinecraftCommandWrapper) {
                return source.hasPermission(((MinecraftCommandWrapper) callable).getCommandPermission());
            }
            return callable.testPermission(source);
        }
        return source.hasPermission(commandName);
    }

    public static void populateNonCommandPermissions(SubjectData data, BiFunction<Integer, String, Boolean> testPermission) {
        if (testPermission.apply(CommandPermissions.COMMAND_BLOCK_LEVEL, CommandPermissions.COMMAND_BLOCK_COMMAND)) {
            data.setPermission(SubjectData.GLOBAL_CONTEXT, SELECTOR_PERMISSION, Tristate.TRUE);
        }
        if (testPermission.apply(CommandPermissions.SELECTOR_LEVEL, CommandPermissions.SELECTOR_COMMAND)) {
            data.setPermission(SubjectData.GLOBAL_CONTEXT, COMMAND_BLOCK_COMMAND, Tristate.TRUE);
        }
    }
}
