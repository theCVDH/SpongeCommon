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
package org.spongepowered.common.data.processor.value.tileentity;

import net.minecraft.tileentity.TileEntityFurnace;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.world.World;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Optional;

public class MaxBurnTimeValueProcessor extends AbstractSpongeValueProcessor<TileEntityFurnace, Integer, MutableBoundedValue<Integer>> {

    public MaxBurnTimeValueProcessor() {
        super(TileEntityFurnace.class, Keys.MAX_BURN_TIME);
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(Integer defaultValue) {
        return SpongeValueFactory.boundedBuilder(Keys.MAX_BURN_TIME)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(defaultValue)
                .build();
    }

    @Override
    protected boolean set(TileEntityFurnace container, Integer value) {
        if (!container.isBurning() && value > 0 || container.isBurning() && value == 0) {
            final World world = (World) container.getWorld();
            world.setBlockType(container.getPos().getX(), container.getPos().getY(),
                    container.getPos().getZ(), value > 0 ? BlockTypes.LIT_FURNACE : BlockTypes.FURNACE);
            container = (TileEntityFurnace) container.getWorld().getTileEntity(container.getPos());
        }
        container.setField(1, value);
        return true;
    }

    @Override
    protected Optional<Integer> getVal(TileEntityFurnace container) {
        return Optional.of(container.getField(1));
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(Integer value) {
        return SpongeValueFactory.boundedBuilder(Keys.MAX_BURN_TIME)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(1000)
                .actualValue(value)
                .build()
                .asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData(); //cannot be removed
    }
}
