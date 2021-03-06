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
package org.spongepowered.common.item.inventory.adapter.impl.comp;

import net.minecraft.inventory.IInventory;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.comp.HotbarLens;

public class HotbarAdapter extends InventoryRowAdapter implements Hotbar {

    protected HotbarLens<IInventory, net.minecraft.item.ItemStack> hotbarLens;

    public HotbarAdapter(Fabric<IInventory> inventory, HotbarLens<IInventory, net.minecraft.item.ItemStack> root) {
        this(inventory, root, null);
    }

    public HotbarAdapter(Fabric<IInventory> inventory, HotbarLens<IInventory, net.minecraft.item.ItemStack> root, Inventory parent) {
        super(inventory, root, parent);
        this.hotbarLens = root;
    }

    @Override
    public int getSelectedSlotIndex() {
        return this.hotbarLens.getSelectedSlotIndex(this.inventory);
    }

    @Override
    public void setSelectedSlotIndex(int index) {
        this.hotbarLens.setSelectedSlotIndex(this.inventory, index);
    }

}
