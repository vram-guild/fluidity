/*******************************************************************************
 * Copyright 2019, 2020 grondag
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
package grondag.fluidity.impl.article;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;

@Internal
public class StackHelper {
	public static ItemStack newStack(Item item, CompoundTag tag, long count) {
		if (item == null || item == Items.AIR || count <= 0) {
			return ItemStack.EMPTY;
		}

		final ItemStack result = new ItemStack(item, (int) Math.min(item.getMaxCount(), count));
		result.setTag(tag);
		return result;
	}

	public static boolean areItemsEqual(Item itemA, CompoundTag tagA, Item itemB, CompoundTag tagB) {
		if (itemA != itemB) {
			return false;
		}

		if (tagA == null) {
			return tagB == null;
		}

		return tagB != null && tagA.equals(tagB);
	}

	public static boolean areItemsEqual(Item item, CompoundTag tag, ItemStack stack) {
		if (item != stack.getItem()) {
			return false;
		}

		if (tag == null) {
			return !stack.hasTag();
		}

		return stack.hasTag() && tag.equals(stack.getTag());
	}

	public static boolean areItemsEqual(ItemStack a, ItemStack b) {
		return areItemsEqual(a.getItem(), a.getTag(), b);
	}

	public static boolean areStacksEqual(ItemStack stackA, ItemStack stackB)  {
		if (stackA.isEmpty() && stackB.isEmpty()) {
			return true;
		}

		if (stackA.isItemEqual(stackB) && stackA.getCount() == stackB.getCount()) {
			return true;
		}

		return false;
	}
}
