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
package org.spongepowered.common.interfaces.entity;

import co.aikar.timings.Timing;
import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.dismount.DismountType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public interface IMixinEntity extends org.spongepowered.api.entity.Entity {

    boolean isInConstructPhase();

    void firePostConstructEvents();

    boolean isTeleporting();

    void setIsTeleporting(boolean teleporting);

    Entity getTeleportVehicle();

    void setTeleportVehicle(Entity entity);

    boolean dismountRidingEntity(DismountType type);

    boolean removePassengers(DismountType type);

    Optional<User> getTrackedPlayer(String nbtKey);

    Optional<User> getCreatorUser();

    Optional<User> getNotifierUser();

    void trackEntityUniqueId(String nbtKey, @Nullable UUID uuid);

    NBTTagCompound getEntityData();

    default NBTTagCompound getSpongeData() {
        final NBTTagCompound data = this.getEntityData();
        if (!data.hasKey(NbtDataUtil.SPONGE_DATA, NbtDataUtil.TAG_COMPOUND)) {
            data.setTag(NbtDataUtil.SPONGE_DATA, new NBTTagCompound());
        }
        return data.getCompoundTag(NbtDataUtil.SPONGE_DATA);
    }

    default void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
    }

    /**
     * Read extra data (SpongeData) from the entity's NBT tag.
     *
     * @param compound The SpongeData compound to read from
     */
    void readFromNbt(NBTTagCompound compound);

    /**
     * Write extra data (SpongeData) to the entity's NBT tag.
     *
     * @param compound The SpongeData compound to write to
     */
    void writeToNbt(NBTTagCompound compound);

    void setImplVelocity(Vector3d velocity);

    boolean isVanished();

    void setVanished(boolean vanished);

    boolean ignoresCollision();

    void setIgnoresCollision(boolean prevents);

    boolean isUntargetable();

    void setUntargetable(boolean untargetable);

    @Nullable Text getDisplayNameText();

    void setDisplayName(@Nullable Text displayName);

    void setCurrentCollidingBlock(BlockState state);

    BlockState getCurrentCollidingBlock();

    BlockPos getLastCollidedBlockPos();

    boolean isVanilla();

    void setDestructCause(Cause cause);

    void setLocationAndAngles(Location<World> location);

    void setLocationAndAngles(Transform<World> transform);

    void createForgeCapabilities();

    // Timings
    Timing getTimingsHandler();

    default void onJoinWorld() {

    }
}
