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
package org.spongepowered.common.mixin.core.world.end;

import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.end.DragonSpawnManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.world.dragon.DragonPhase;

import java.util.List;
import java.util.UUID;

@Mixin(DragonFightManager.class)
public abstract class MixinDragonFightManager {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private BossInfoServer bossInfo;
    @Shadow @Final private WorldServer world;
    @Shadow @Final private List<Integer> gateways;
    @Shadow @Final private BlockPattern portalPattern;
    @Shadow private int ticksSinceDragonSeen;
    @Shadow private int aliveCrystals;
    @Shadow private int ticksSinceCrystalsScanned;
    @Shadow private int ticksSinceLastPlayerScan;
    @Shadow private boolean dragonKilled;
    @Shadow private boolean previouslyKilled;
    @Shadow private UUID dragonUniqueId;
    @Shadow private boolean scanForLegacyFight;
    @Shadow private BlockPos exitPortalLocation;
    @Shadow private DragonSpawnManager respawnState;
    @Shadow private int respawnStateTicks;
    @Shadow private List<EntityEnderCrystal> crystals;

    @Shadow public abstract void respawnDragon();
    @Shadow private boolean hasDragonBeenKilled() {
        return false; // Shadowed
    }
    @Shadow private void updateplayers() { }
    @Shadow private void findAliveCrystals() { }
    @Shadow private void loadChunks() { }
    @Shadow private void generatePortal(boolean flag) { }
    @Shadow private EntityDragon func_192445_m() {
        return null; // Shadowed
    }

    /**
     * @author gabizou - January 22nd, 2017
     * @reason Injects Sponge necessary phase state switches
     */
    @Overwrite
    public void tick() {
        this.bossInfo.setVisible(!this.dragonKilled);

        if (++this.ticksSinceLastPlayerScan >= 20) {
            this.updateplayers();
            this.ticksSinceLastPlayerScan = 0;
        }

        if (!this.bossInfo.getPlayers().isEmpty()) {
            if (this.scanForLegacyFight) {
                LOGGER.info("Scanning for legacy world dragon fight...");
                this.loadChunks();
                this.scanForLegacyFight = false;
                boolean flag = this.hasDragonBeenKilled();

                if (flag) {
                    LOGGER.info("Found that the dragon has been killed in this world already.");
                    this.previouslyKilled = true;
                } else {
                    LOGGER.info("Found that the dragon has not yet been killed in this world.");
                    this.previouslyKilled = false;
                    this.generatePortal(false);
                }

                List<EntityDragon> list = this.world.getEntities(EntityDragon.class, EntitySelectors.IS_ALIVE);

                if (list.isEmpty()) {
                    this.dragonKilled = true;
                } else {
                    EntityDragon entitydragon = list.get(0);
                    this.dragonUniqueId = entitydragon.getUniqueID();
                    LOGGER.info("Found that there\'s a dragon still alive ({})", entitydragon);
                    this.dragonKilled = false;

                    if (!flag) {
                        LOGGER.info("But we didn\'t have a portal, let\'s remove it.");
                        entitydragon.setDead();
                        this.dragonUniqueId = null;
                    }
                }

                if (!this.previouslyKilled && this.dragonKilled) {
                    this.dragonKilled = false;
                }
            }

            if (this.respawnState != null) {
                if (this.crystals == null) {
                    this.respawnState = null;
                    this.respawnDragon();
                }

                // Sponge Start - Cause tracker - todo: do more logistical configuration of how this all works.
                final CauseTracker causeTracker = CauseTracker.getInstance();
                causeTracker.switchToPhase(DragonPhase.State.RESPAWN_DRAGON, PhaseContext.start()
                    .addCaptures()
                    .complete());
                // Sponge End
                this.respawnState.process(this.world, (DragonFightManager) (Object) this, this.crystals, this.respawnStateTicks++, this.exitPortalLocation);
                causeTracker.completePhase(DragonPhase.State.RESPAWN_DRAGON); // Sponge - Complete cause tracker
            }

            if (!this.dragonKilled) {
                if (this.dragonUniqueId == null || ++this.ticksSinceDragonSeen >= 1200) {
                    this.loadChunks();
                    List<EntityDragon> list1 = this.world.getEntities(EntityDragon.class, EntitySelectors.IS_ALIVE);

                    if (list1.isEmpty()) {
                        LOGGER.debug("Haven\'t seen the dragon, respawning it");
                        this.func_192445_m();
                    } else {
                        LOGGER.debug("Haven\'t seen our dragon, but found another one to use.");
                        this.dragonUniqueId = list1.get(0).getUniqueID();
                    }

                    this.ticksSinceDragonSeen = 0;
                }

                if (++this.ticksSinceCrystalsScanned >= 100) {
                    this.findAliveCrystals();
                    this.ticksSinceCrystalsScanned = 0;
                }
            }
        }
    }


}
