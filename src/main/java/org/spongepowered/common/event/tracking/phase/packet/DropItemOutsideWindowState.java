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
package org.spongepowered.common.event.tracking.phase.packet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.api.world.World;
import org.spongepowered.common.event.tracking.PhaseContext;

import java.util.List;

final class DropItemOutsideWindowState extends BasicInventoryPacketState {

    DropItemOutsideWindowState() {
        super(PacketPhase.MODE_CLICK | PacketPhase.BUTTON_PRIMARY | PacketPhase.BUTTON_SECONDARY | PacketPhase.CLICK_OUTSIDE_WINDOW);
    }

    @Override
    public boolean doesCaptureEntityDrops() {
        return true;
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
        super.populateContext(playerMP, packet, context);
    }

    @Override
    public ClickInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
            List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, int usedButton) {
        Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);

        for (Entity currentEntity : capturedEntities) {
            currentEntity.setCreator(playerMP.getUniqueID());
        }
        return usedButton == PacketPhase.PACKET_BUTTON_PRIMARY_ID
               ? SpongeEventFactory.createClickInventoryEventDropOutsidePrimary(Sponge.getCauseStackManager().getCurrentCause(), transaction, capturedEntities, openContainer, slotTransactions)
               : SpongeEventFactory.createClickInventoryEventDropOutsideSecondary(Sponge.getCauseStackManager().getCurrentCause(), transaction, capturedEntities, openContainer, slotTransactions);
    }

    @Override
    public boolean ignoresItemPreMerges() {
        return true;
    }
}
