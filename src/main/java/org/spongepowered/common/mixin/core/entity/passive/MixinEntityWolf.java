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
package org.spongepowered.common.mixin.core.entity.passive;

import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.SittingData;
import org.spongepowered.api.entity.living.animal.Wolf;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAggressiveData;
import org.spongepowered.common.interfaces.entity.IMixinAggressive;

import java.util.List;
import java.util.Random;

@Mixin(EntityWolf.class)
@Implements(value = @Interface(iface = IMixinAggressive.class, prefix = "aggr$"))
public abstract class MixinEntityWolf extends MixinEntityAnimal implements Wolf {

    @Shadow public abstract boolean isAngry();

    @Shadow public abstract void setAngry(boolean angry);

    @Intrinsic
    public boolean aggr$isAngry() {
        return this.isAngry();
    }

    @Intrinsic
    public void aggr$setAngry(boolean angry) {
        this.setAngry(angry);
    }

    @Redirect(method = "processInteract", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 0, remap = false))
    public int onTame(Random rand, int bound, EntityPlayer player, EnumHand hand) {
        int random = rand.nextInt(bound);
        ItemStack stack = player.getHeldItem(hand);
        if (random == 0) {
            stack.shrink(1);
            try {
                Sponge.getCauseStackManager().pushCause(((org.spongepowered.api.item.inventory.ItemStack) stack).createSnapshot());
                Sponge.getCauseStackManager().pushCause(player);
                if (!SpongeImpl.postEvent(SpongeEventFactory.createTameEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), this))) {
                    stack.grow(1);
                    return random;
                }
            } finally {
                Sponge.getCauseStackManager().popCauses(2);
            }
        }
        return 1;
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(get(SittingData.class).get());
        manipulators.add(new SpongeAggressiveData(this.isAngry()));
    }
}
