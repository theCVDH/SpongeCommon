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
package org.spongepowered.common.event.tracking.phase.general;

import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.ItemDropData;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.block.BlockPhaseState;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.world.WorldManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

final class CommandState extends GeneralState {

    @Override
    public boolean canSwitchTo(IPhaseState state) {
        return state instanceof BlockPhaseState;
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }

    @Override
    void unwind(PhaseContext phaseContext) {
        final CommandSource sender = phaseContext.getSource(CommandSource.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a Command Sender, but none found!", phaseContext));
        phaseContext.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(list -> TrackingUtil.processBlockCaptures(list, this, phaseContext));
        Object frame = Sponge.getCauseStackManager().pushCauseFrame();
        Sponge.getCauseStackManager().pushCause(sender);
        Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.PLACEMENT);
        phaseContext.getCapturedEntitySupplier()
                .ifPresentAndNotEmpty(entities -> {
                    // TODO the entity spawn causes are not likely valid, need to investigate further.
                    final SpawnEntityEvent spawnEntityEvent = SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), entities);
                    SpongeImpl.postEvent(spawnEntityEvent);
                    if (!spawnEntityEvent.isCancelled()) {
                        final boolean isPlayer = sender instanceof Player;
                        final Player player = isPlayer ? (Player) sender : null;
                        for (Entity entity : spawnEntityEvent.getEntities()) {
                            if (isPlayer) {
                                EntityUtil.toMixin(entity).setCreator(player.getUniqueId());
                            }
                            EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                        }
                    }
                });
        Sponge.getCauseStackManager().popCauseFrame(frame);
        frame = Sponge.getCauseStackManager().pushCauseFrame();
        Sponge.getCauseStackManager().pushCause(sender);
        Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.DROPPED_ITEM);
        phaseContext.getCapturedEntityDropSupplier()
                .ifPresentAndNotEmpty(uuidItemStackMultimap -> {
                    for (Map.Entry<UUID, Collection<ItemDropData>> entry : uuidItemStackMultimap.asMap().entrySet()) {
                        final UUID key = entry.getKey();
                        @Nullable net.minecraft.entity.Entity foundEntity = null;
                        for (WorldServer worldServer : WorldManager.getWorlds()) {
                            final net.minecraft.entity.Entity entityFromUuid = worldServer.getEntityFromUuid(key);
                            if (entityFromUuid != null) {
                                foundEntity = entityFromUuid;
                                break;
                            }
                        }
                        final Optional<Entity> affectedEntity = Optional.ofNullable((Entity) foundEntity);
                        if (!affectedEntity.isPresent()) {
                            continue;
                        }
                        final Collection<ItemDropData> itemStacks = entry.getValue();
                        if (itemStacks.isEmpty()) {
                            return;
                        }
                        final List<ItemDropData> items = new ArrayList<>();
                        items.addAll(itemStacks);
                        itemStacks.clear();

                        final WorldServer minecraftWorld = EntityUtil.getMinecraftWorld(affectedEntity.get());
                        if (!items.isEmpty()) {
                            final List<Entity> itemEntities = items.stream()
                                    .map(data -> data.create(minecraftWorld))
                                    .map(EntityUtil::fromNative)
                                    .collect(Collectors.toList());
                            Sponge.getCauseStackManager().pushCause(affectedEntity.get());
                            final DropItemEvent.Destruct
                                    destruct =
                                    SpongeEventFactory.createDropItemEventDestruct(Sponge.getCauseStackManager().getCurrentCause(), itemEntities);
                            SpongeImpl.postEvent(destruct);
                            Sponge.getCauseStackManager().popCause();
                            if (!destruct.isCancelled()) {
                                final boolean isPlayer = sender instanceof Player;
                                final Player player = isPlayer ? (Player) sender : null;
                                for (Entity entity : destruct.getEntities()) {
                                    if (isPlayer) {
                                        EntityUtil.toMixin(entity).setCreator(player.getUniqueId());
                                    }
                                    EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                                }
                            }

                        }
                    }
                });
        Sponge.getCauseStackManager().popCauseFrame(frame);
    }

    @Override
    public boolean spawnEntityOrCapture(PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        return context.getCapturedEntities().add(entity);
    }

    @Override
    public Cause generateTeleportCause(PhaseContext context) {
        final Entity entity = context.getSource(Entity.class).orElse(null);
        if (entity != null) {
            return Cause
                    .source(EntityTeleportCause.builder()
                            .entity(entity)
                            .type(TeleportTypes.COMMAND)
                            .build()
                    )
                    .build();
        }

        return Cause.of(NamedCause.source(TeleportCause.builder().type(TeleportTypes.COMMAND).build()));
    }
}
