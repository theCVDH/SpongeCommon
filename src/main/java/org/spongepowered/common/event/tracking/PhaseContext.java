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
package org.spongepowered.common.event.tracking;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * Similar to {@link Cause} except it can be built continuously and retains no
 * real side effects. Strictly speaking this object exists to avoid confusion
 * between what is suggested to be a {@link Cause} for an {@link Event} versus
 * the context of which a {@link IPhaseState} is being completed with.
 */
public class PhaseContext {

    private boolean isCompleted = false;

    @Nullable private CapturedBlocksSupplier blocksSupplier;
    @Nullable private BlockItemDropsSupplier blockItemDropsSupplier;
    @Nullable private BlockItemEntityDropsSupplier blockItemEntityDropsSupplier;
    @Nullable private CapturedItemsSupplier capturedItemsSupplier;
    @Nullable private CapturedEntitiesSupplier capturedEntitiesSupplier;
    @Nullable private CapturedItemStackSupplier capturedItemStackSupplier;
    @Nullable private EntityItemDropsSupplier entityItemDropsSupplier;
    @Nullable private EntityItemEntityDropsSupplier entityItemEntityDropsSupplier;
    @Nullable private CapturedMultiMapSupplier<BlockPos, net.minecraft.entity.Entity> blockEntitySpawnSupplier;
    @Nullable private CaptureBlockPos captureBlockPos;
    @Nullable private CapturePlayer capturePlayer;
    @Nullable protected User owner;
    @Nullable protected User notifier;
    protected final Map<String, Object> extraContext = Maps.newHashMap();
    protected boolean processImmediately;

    private Object source;

    public static PhaseContext start() {
        return new PhaseContext();
    }

    public PhaseContext addExtra(String key, Object val) {
        this.extraContext.put(key, val);
        return this;
    }

    public Object getExtra(String key) {
        return this.extraContext.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtra(String key, Class<T> type) {
        Object o = this.extraContext.get(key);
        if (type.isInstance(o)) {
            return (T) o;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getRequiredExtra(String key, Class<T> type) {
        Object o = this.extraContext.get(key);
        if (type.isInstance(o)) {
            return (T) o;
        }
        throw TrackingUtil.throwWithContext("Extra context value " + key + " with type " + type.getName()
                + " was requested but was not present in the phase context.", this).get();
    }

    public Map<String, Object> getExtraContext() {
        return this.extraContext;
    }

    public PhaseContext source(Object owner) {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.source = owner;
        return this;
    }

    public PhaseContext owner(User owner) {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        if (this.owner != null) {
            throw new IllegalStateException("Owner for this phase context is already set!");
        }
        this.owner = checkNotNull(owner, "Owner cannot be null!");
        return this;
    }

    public PhaseContext notifier(User notifier) {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        if (this.notifier != null) {
            throw new IllegalStateException("Notifier for this phase context is already set!");
        }
        this.notifier = checkNotNull(notifier, "Notifier cannot be null!");
        return this;
    }

    private void checkBlockSuppliers() {
        checkState(this.blocksSupplier == null, "BlocksSuppler is already set!");
        checkState(this.blockItemEntityDropsSupplier == null, "BlockItemEntityDropsSupplier is already set!");
        checkState(this.blockItemDropsSupplier == null, "BlockItemDropsSupplier is already set!");
        checkState(this.blockEntitySpawnSupplier == null, "BlockEntitySpawnSupplier is already set!");
        checkState(this.captureBlockPos == null, "CaptureBlockPos is already set!");
    }

    public PhaseContext addBlockCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.checkBlockSuppliers();

        CapturedBlocksSupplier blocksSupplier = new CapturedBlocksSupplier();
        this.blocksSupplier = blocksSupplier;
        BlockItemEntityDropsSupplier blockItemEntityDropsSupplier = new BlockItemEntityDropsSupplier();
        this.blockItemEntityDropsSupplier = blockItemEntityDropsSupplier;
        BlockItemDropsSupplier blockItemDropsSupplier = new BlockItemDropsSupplier();
        this.blockItemDropsSupplier = blockItemDropsSupplier;
        CapturedBlockEntitySpawnSupplier capturedBlockEntitySpawnSupplier = new CapturedBlockEntitySpawnSupplier();
        this.blockEntitySpawnSupplier = capturedBlockEntitySpawnSupplier;

        CaptureBlockPos blockPos = new CaptureBlockPos();
        this.captureBlockPos = blockPos;
        return this;
    }

    public PhaseContext addCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.checkBlockSuppliers();
        checkState(this.capturedItemsSupplier == null, "CapturedItemsSupplier is already set!");
        checkState(this.capturedEntitiesSupplier == null, "CapturedEntitiesSupplier is already set!");
        checkState(this.capturedItemStackSupplier == null, "CapturedItemStackSupplier is already set!");

        CapturedBlocksSupplier blocksSupplier = new CapturedBlocksSupplier();
        this.blocksSupplier = blocksSupplier;
        BlockItemEntityDropsSupplier blockItemEntityDropsSupplier = new BlockItemEntityDropsSupplier();
        this.blockItemEntityDropsSupplier = blockItemEntityDropsSupplier;
        BlockItemDropsSupplier blockItemDropsSupplier = new BlockItemDropsSupplier();
        this.blockItemDropsSupplier = blockItemDropsSupplier;
        CapturedItemsSupplier capturedItemsSupplier = new CapturedItemsSupplier();
        this.capturedItemsSupplier = capturedItemsSupplier;
        CapturedEntitiesSupplier capturedEntitiesSupplier = new CapturedEntitiesSupplier();
        this.capturedEntitiesSupplier = capturedEntitiesSupplier;
        CapturedItemStackSupplier capturedItemStackSupplier = new CapturedItemStackSupplier();
        this.capturedItemStackSupplier = capturedItemStackSupplier;

        CapturedBlockEntitySpawnSupplier capturedBlockEntitySpawnSupplier = new CapturedBlockEntitySpawnSupplier();
        this.blockEntitySpawnSupplier = capturedBlockEntitySpawnSupplier;
        return this;
    }

    public PhaseContext addEntityCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        checkState(this.capturedItemsSupplier == null, "CapturedItemsSupplier is already set!");
        checkState(this.capturedEntitiesSupplier == null, "CapturedEntitiesSupplier is already set!");
        checkState(this.capturedItemStackSupplier == null, "CapturedItemStackSupplier is already set!");

        CapturedItemsSupplier capturedItemsSupplier = new CapturedItemsSupplier();
        this.capturedItemsSupplier = capturedItemsSupplier;
        CapturedEntitiesSupplier capturedEntitiesSupplier = new CapturedEntitiesSupplier();
        this.capturedEntitiesSupplier = capturedEntitiesSupplier;
        CapturedItemStackSupplier capturedItemStackSupplier = new CapturedItemStackSupplier();
        this.capturedItemStackSupplier = capturedItemStackSupplier;
        return this;
    }

    public PhaseContext addEntityDropCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        checkState(this.entityItemDropsSupplier == null, "EntityItemDropsSupplier is already set!");
        checkState(this.entityItemEntityDropsSupplier == null, "EntityItemEntityDropsSupplier is already set!");

        EntityItemDropsSupplier entityItemDropsSupplier = new EntityItemDropsSupplier();
        this.entityItemDropsSupplier = entityItemDropsSupplier;
        EntityItemEntityDropsSupplier entityItemEntityDropsSupplier = new EntityItemEntityDropsSupplier();
        this.entityItemEntityDropsSupplier = entityItemEntityDropsSupplier;
        return this;
    }

    public PhaseContext complete() {
        this.isCompleted = true;
        return this;
    }

    public boolean isComplete() {
        return this.isCompleted;
    }

    public boolean shouldProcessImmediately() {
        return this.processImmediately;
    }

    public void setProcessImmediately(boolean state) {
        this.processImmediately = state;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getSource(Class<T> sourceClass) {
        if (this.source == null) {
            return Optional.empty();
        }
        if (sourceClass.isInstance(this.source)) {
            return Optional.of((T) this.source);
        }
        return Optional.empty();
    }

    public Optional<User> getOwner() {
        return Optional.ofNullable(this.owner);
    }

    public Optional<User> getNotifier() {
        return Optional.ofNullable(this.notifier);
    }

    public List<Entity> getCapturedEntities() throws IllegalStateException {
        return this.capturedEntitiesSupplier.get();
    }

    public CapturedSupplier<Entity> getCapturedEntitySupplier() throws IllegalStateException {
        if (this.capturedEntitiesSupplier == null) {
            throw TrackingUtil.throwWithContext("Intended to capture entity spawns!", this).get();
        }
        return this.capturedEntitiesSupplier;
    }

    public List<EntityItem> getCapturedItems() throws IllegalStateException {
        if (this.capturedItemsSupplier == null) {
            throw TrackingUtil.throwWithContext("Intended to capture dropped item entities!", this).get();
        }
        return this.capturedItemsSupplier.get();
    }

    public CapturedSupplier<EntityItem> getCapturedItemsSupplier() throws IllegalStateException {
        if (this.capturedItemsSupplier == null) {
            throw TrackingUtil.throwWithContext("Intended to capture dropped item entities!", this).get();
        }
        return this.capturedItemsSupplier;
    }

    public List<BlockSnapshot> getCapturedBlocks() throws IllegalStateException {
        return this.blocksSupplier.get();
    }

    public CapturedSupplier<BlockSnapshot> getCapturedBlockSupplier() throws IllegalStateException {
        if (this.blocksSupplier == null) {
            throw TrackingUtil.throwWithContext("Expected to be capturing blocks, but we're not capturing them!", this).get();
        }
        return this.blocksSupplier;
    }

    public Multimap<BlockPos, ItemDropData> getCapturedBlockDrops() throws IllegalStateException {
        if (this.blockItemDropsSupplier == null) {
            throw TrackingUtil.throwWithContext("Expected to be capturing block drops!", this).get();
        }
        return this.blockItemDropsSupplier.get();
    }

    public CapturedMultiMapSupplier<BlockPos, ItemDropData> getBlockDropSupplier() throws IllegalStateException {
        if (this.blockItemDropsSupplier == null) {
            throw TrackingUtil.throwWithContext("Expected to be capturing block drops!", this).get();
        }
        return this.blockItemDropsSupplier;
    }

    public CapturedMultiMapSupplier<BlockPos, EntityItem> getBlockItemDropSupplier() throws IllegalStateException {
        if (this.blockItemEntityDropsSupplier == null) {
            throw TrackingUtil.throwWithContext("Intended to track block item drops!", this).get();
        }
        return this.blockItemEntityDropsSupplier;
    }

    public CapturedMultiMapSupplier<UUID, ItemDropData> getCapturedEntityDropSupplier() throws IllegalStateException {
        if (this.entityItemDropsSupplier == null) {
            throw TrackingUtil.throwWithContext("Intended to capture entity drops!", this).get();
        }
        return this.entityItemDropsSupplier;
    }

    public CapturedMultiMapSupplier<UUID, EntityItem> getCapturedEntityItemDropSupplier() throws IllegalStateException {
        if (this.entityItemEntityDropsSupplier == null) {
            throw TrackingUtil.throwWithContext("Intended to capture entity drops!", this).get();
        }
        return this.entityItemEntityDropsSupplier;
    }

    public CapturedSupplier<ItemDropData> getCapturedItemStackSupplier() throws IllegalStateException {
        if (this.capturedItemStackSupplier == null) {
            throw TrackingUtil.throwWithContext("Expected to be capturing ItemStack drops from entities!", this).get();
        }
        return this.capturedItemStackSupplier;
    }

    public CapturedMultiMapSupplier<BlockPos, net.minecraft.entity.Entity> getBlockEntitySpawnSupplier() throws IllegalStateException {
        if (this.blockEntitySpawnSupplier == null) {
            throw TrackingUtil.throwWithContext("Intended to track block entity spawns!", this).get();
        }
        return this.blockEntitySpawnSupplier;
    }

    public CaptureBlockPos getCaptureBlockPos() throws IllegalStateException {
        if (this.captureBlockPos == null) {
            throw TrackingUtil.throwWithContext("Intended to capture a block position!", this).get();
        }
        return this.captureBlockPos;
    }

    public Optional<BlockPos> getBlockPosition() {
        return getCaptureBlockPos()
                .getPos();
    }

    public CapturePlayer getCapturedPlayerSupplier() throws IllegalStateException {
        if (this.capturePlayer == null) {
            throw TrackingUtil.throwWithContext("Expected to be capturing a Player from an event listener, but we're not capturing them!", this)
                    .get();
        }
        return this.capturePlayer;
    }

    public Optional<Player> getCapturedPlayer() throws IllegalStateException {
        return getCapturedPlayerSupplier().getPlayer();
    }

    public void addNotifierAndOwnerToCauseStack() {
        if (this.owner != null) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, this.owner);
        }
        if (this.notifier != null) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, this.notifier);
        }
    }

    PhaseContext() {
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.isCompleted);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PhaseContext other = (PhaseContext) obj;
        return Objects.equals(this.isCompleted, other.isCompleted);
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("isCompleted", this.isCompleted)
                .toString();
    }

    static class BlockItemDropsSupplier extends CapturedMultiMapSupplier<BlockPos, ItemDropData> {

        BlockItemDropsSupplier() {
        }

    }

    static class EntityItemDropsSupplier extends CapturedMultiMapSupplier<UUID, ItemDropData> {

        EntityItemDropsSupplier() {
        }
    }

    static final class CapturedItemsSupplier extends CapturedSupplier<EntityItem> {

        CapturedItemsSupplier() {
        }
    }

    static final class CapturedItemStackSupplier extends CapturedSupplier<ItemDropData> {

        CapturedItemStackSupplier() {
        }
    }

    static final class CapturedBlocksSupplier extends CapturedSupplier<BlockSnapshot> {

        CapturedBlocksSupplier() {
        }
    }

    static final class CapturedEntitiesSupplier extends CapturedSupplier<Entity> {

        CapturedEntitiesSupplier() {
        }
    }

    static final class EntityItemEntityDropsSupplier extends CapturedMultiMapSupplier<UUID, EntityItem> {

        EntityItemEntityDropsSupplier() {
        }
    }

    static final class BlockItemEntityDropsSupplier extends CapturedMultiMapSupplier<BlockPos, EntityItem> {

        BlockItemEntityDropsSupplier() {
        }
    }

    static final class CapturedBlockEntitySpawnSupplier extends CapturedMultiMapSupplier<BlockPos, net.minecraft.entity.Entity> {

        CapturedBlockEntitySpawnSupplier() {
        }
    }

    public static final class CapturePlayer {

        @Nullable private Player player;

        CapturePlayer() {

        }

        CapturePlayer(@Nullable Player player) {
            this.player = player;
        }

        public Optional<Player> getPlayer() {
            return Optional.ofNullable(this.player);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CapturePlayer that = (CapturePlayer) o;
            return com.google.common.base.Objects.equal(this.player, that.player);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(this.player);
        }

        @Override
        public String toString() {
            return com.google.common.base.MoreObjects.toStringHelper(this)
                    .add("player", this.player)
                    .toString();
        }

        public void addPlayer(EntityPlayerMP playerMP) {
            this.player = ((Player) playerMP);
        }
    }

    public static final class CaptureExplosion {

        @Nullable private Explosion explosion;

        CaptureExplosion() {

        }

        CaptureExplosion(@Nullable Explosion explosion) {
            this.explosion = explosion;
        }

        public Optional<Explosion> getExplosion() {
            return Optional.ofNullable(this.explosion);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CaptureExplosion that = (CaptureExplosion) o;
            return com.google.common.base.Objects.equal(this.explosion, that.explosion);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(this.explosion);
        }

        @Override
        public String toString() {
            return com.google.common.base.MoreObjects.toStringHelper(this)
                    .add("explosion", this.explosion)
                    .toString();
        }

        public void addExplosion(Explosion explosion) {
            this.explosion = explosion;
        }
    }

    public static final class CaptureBlockPos {

        @Nullable private BlockPos pos;
        @Nullable private WeakReference<IMixinWorldServer> mixinWorldReference;

        public CaptureBlockPos() {
        }

        public CaptureBlockPos(@Nullable BlockPos pos) {
            this.pos = pos;
        }

        public Optional<BlockPos> getPos() {
            return Optional.ofNullable(this.pos);
        }

        public void setPos(@Nullable BlockPos pos) {
            this.pos = pos;
        }

        public void setWorld(@Nullable IMixinWorldServer world) {
            if (world == null) {
                this.mixinWorldReference = null;
            } else {
                this.mixinWorldReference = new WeakReference<>(world);
            }
        }

        public void setWorld(@Nullable WorldServer world) {
            if (world == null) {
                this.mixinWorldReference = null;
            } else {
                this.mixinWorldReference = new WeakReference<>((IMixinWorldServer) world);
            }
        }

        public Optional<IMixinWorldServer> getMixinWorld() {
            return this.mixinWorldReference == null ? Optional.empty() : Optional.ofNullable(this.mixinWorldReference.get());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CaptureBlockPos that = (CaptureBlockPos) o;
            return com.google.common.base.Objects.equal(this.pos, that.pos);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(this.pos);
        }
    }
}
