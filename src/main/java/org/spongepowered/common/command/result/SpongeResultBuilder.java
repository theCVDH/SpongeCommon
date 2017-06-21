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
package org.spongepowered.common.command.result;

import org.spongepowered.api.command.Result;

import javax.annotation.Nullable;

public class SpongeResultBuilder implements Result.Builder {

    @Nullable private Integer successCount;
    @Nullable private Integer affectedBlocks;
    @Nullable private Integer affectedEntities;
    @Nullable private Integer affectedItems;
    @Nullable private Integer queryResult;

    @Override
    public Result.Builder from(Result value) {
        reset();

        value.successCount().ifPresent(x -> this.successCount = x);
        value.affectedBlocks().ifPresent(x -> this.affectedBlocks = x);
        value.affectedEntities().ifPresent(x -> this.affectedEntities = x);
        value.affectedItems().ifPresent(x -> this.affectedItems = x);
        value.queryResult().ifPresent(x -> this.queryResult = x);

        return this;
    }

    @Override
    public Result.Builder reset() {
        this.successCount = null;
        this.affectedBlocks = null;
        this.affectedEntities = null;
        this.affectedItems = null;
        this.queryResult = null;

        return this;
    }

    /**
     * Sets if the command has been processed.
     *
     * @param successCount If the command has been processed
     * @return This builder, for chaining
     */
    public Result.Builder successCount(@Nullable Integer successCount) {
        this.successCount = successCount;
        return this;
    }

    /**
     * Sets the amount of blocks affected by the command.
     *
     * @param affectedBlocks The amount of blocks affected by the command
     * @return This builder, for chaining
     */
    public Result.Builder affectedBlocks(@Nullable Integer affectedBlocks) {
        this.affectedBlocks = affectedBlocks;
        return this;
    }

    /**
     * Sets the amount of entities affected by the command.
     *
     * @param affectedEntities The amount of entities affected by the
     *     command
     * @return This builder, for chaining
     */
    public Result.Builder affectedEntities(@Nullable Integer affectedEntities) {
        this.affectedEntities = affectedEntities;
        return this;
    }

    /**
     * Sets the amount of items affected by the command.
     *
     * @param affectedItems The amount of items affected by the command
     * @return This builder, for chaining
     */
    public Result.Builder affectedItems(@Nullable Integer affectedItems) {
        this.affectedItems = affectedItems;
        return this;
    }

    /**
     * Sets the query result of the command, e.g. the time of the day,
     * an amount of money or a player's amount of XP.
     *
     * @param queryResult The query result of the command
     * @return This builder, for chaining
     */
    public Result.Builder queryResult(@Nullable Integer queryResult) {
        this.queryResult = queryResult;
        return this;
    }

    /**
     * Builds the {@link Result}.
     *
     * @return A Result with the specified settings
     */
    public Result build() {
        return new SpongeResult(this.successCount, this.affectedBlocks, this.affectedEntities, this.affectedItems, this.queryResult);
    }

}
