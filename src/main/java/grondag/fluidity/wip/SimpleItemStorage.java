/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.fluidity.wip;

import java.util.Arrays;

import grondag.fluidity.api.storage.AbstractItemStorage;
import grondag.fluidity.api.storage.Storage;
import net.minecraft.item.ItemStack;

public class SimpleItemStorage extends AbstractItemStorage<Void> implements Storage<Void> {
	protected final int size;
	protected final ItemStack[] stacks;

	public SimpleItemStorage(int size) {
		this.size = size;
		stacks = new ItemStack[size];
		Arrays.fill(stacks, ItemStack.EMPTY);
	}

	@Override
	public int slotCount() {
		return size;
	}

	@Override
	protected ItemStack getStack(int slot) {
		return stacks[slot];
	}

	@Override
	protected void setStack(int slot, ItemStack stack) {
		stacks[slot] = stack;
	}
}
