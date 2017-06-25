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
package org.spongepowered.common.mixin.core.tileentity;

import static org.spongepowered.api.data.DataQuery.of;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.tileentity.carrier.Hopper;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.tileentity.CooldownData;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.OrderedInventory;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinInventory;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.comp.GridInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftLens;
import org.spongepowered.common.item.inventory.lens.impl.ReusableLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NonnullByDefault
@Mixin(TileEntityHopper.class)
public abstract class MixinTileEntityHopper extends MixinTileEntityLockableLoot implements Hopper, IMixinInventory {

    @Shadow public int transferCooldown;
    @Shadow private static ItemStack insertStack(IInventory source, IInventory destination, ItemStack stack, int index, EnumFacing direction) {
        throw new AbstractMethodError("Shadow");
    }

    public List<SlotTransaction> capturedTransactions = new ArrayList<>();

    @Override
    public List<SlotTransaction> getCapturedTransactions() {
        return this.capturedTransactions;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        ReusableLens<? extends Lens<IInventory, ItemStack>> reusableLens = MinecraftLens.getLens(GridInventoryLens.class,
                ((InventoryAdapter) this),
                s -> new GridInventoryLensImpl(0, 5, 1, 5, s),
                () -> new SlotCollection.Builder().add(5).build());
        this.slots = reusableLens.getSlots();
        this.lens = reusableLens.getLens();
    }

    @Inject(method = "putDropInInventoryAllSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItem;getItem()Lnet/minecraft/item/ItemStack;"))
    private static void onPutDrop(IInventory inventory, IInventory hopper, EntityItem entityItem, CallbackInfoReturnable<Boolean> callbackInfo) {
        ((IMixinEntity) entityItem).getCreatorUser().ifPresent(owner -> {
            if (inventory instanceof TileEntity) {
                TileEntity te = (TileEntity) inventory;
                BlockPos pos = te.getPos();
                IMixinChunk spongeChunk = (IMixinChunk) te.getWorld().getChunkFromBlockCoords(pos);
                spongeChunk.addTrackedBlockPosition(te.getBlockType(), pos, owner, PlayerTracker.Type.NOTIFIER);
            }
        });
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        return container.set(of("TransferCooldown"), this.transferCooldown);
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        Optional<CooldownData> cooldownData = get(CooldownData.class);
        if (cooldownData.isPresent()) {
            manipulators.add(cooldownData.get());
        }
    }

    @Inject(method = "transferItemsOut", locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1))
    private void afterPutStackInSlots(CallbackInfoReturnable<Boolean> cir, IInventory iInventory, EnumFacing enumFacing, int i, ItemStack itemStack, ItemStack itemStack1) {
        System.out.println("afterPutStackSlots");
        // after putStackInInventoryAllSlots
        // if the transfer worked
        if (itemStack1.isEmpty()) {
            Slot slot = ((OrderedInventory) ((MinecraftInventoryAdapter) this).query(OrderedInventory.class)).getSlot(SlotIndex.of(i)).get();
            SlotTransaction trans = new SlotTransaction(slot, ItemStackUtil.snapshotOf(itemStack), ItemStackUtil.snapshotOf(slot.peek().orElse(
                            org.spongepowered.api.item.inventory.ItemStack.empty())));
            this.capturedTransactions.add(trans);
            Cause.Builder builder = Cause.source(this);

            // TODO problem: Transfer affects 2 inventories!
            ChangeInventoryEvent.Transfer event =
                    SpongeEventFactory.createChangeInventoryEventTransfer(builder.build(), ((Inventory) this), this.capturedTransactions);

            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                // restore inventories
                for (SlotTransaction transaction : event.getTransactions()) {
                    transaction.getSlot().set(transaction.getOriginal().createStack());
                }
                // TODO do we want to try to transfer more items? or just cancel everything
                itemStack1 = itemStack; // vanilla thinks there was not enough place for item in target
            }

            this.capturedTransactions.clear();
        }
    }

    @Redirect(method = "putStackInInventoryAllSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityHopper;insertStack(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/inventory/IInventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/EnumFacing;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack onInsertStack(IInventory source, IInventory destination, ItemStack stack, int index, EnumFacing direction) {
        Slot slot = ((OrderedInventory) ((MinecraftInventoryAdapter) destination).query(OrderedInventory.class)).getSlot(SlotIndex.of(index)).get();
        ItemStackSnapshot from = slot.peek().map(ItemStackUtil::snapshotOf).orElse(ItemStackSnapshot.NONE);
        ItemStack remaining = insertStack(source, destination, stack, index, direction);

        ItemStackSnapshot to = slot.peek().map(ItemStackUtil::snapshotOf).orElse(ItemStackSnapshot.NONE);

        if (source instanceof IMixinInventory) {
            ((IMixinInventory) source).getCapturedTransactions()
                    .add(new SlotTransaction(slot, from, to));;
        }
        return remaining;
    }

    /*
    private static boolean pullItemFromSlot(IHopper hopper, IInventory inventoryIn, int index, EnumFacing direction) {
        // TODO after putStackInInventoryAllSlots
        // get slot
        // itemstack1 is before, after one less
        // Build SlotTransaction and add it to a list for the event later
        // Additionally we need the slottransaction in the remote inventory
        return false;
    }

    private static boolean putDropInInventoryAllSlots(IInventory source, IInventory destination, EntityItem entity) {
        // TODO after putStackInInventoryAllSlots
        // entity?
        // Additionally we need the slottransaction in the remote inventory
        return false;
    }
    */
}
