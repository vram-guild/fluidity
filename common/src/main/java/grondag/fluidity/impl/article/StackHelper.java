/*
 * This file is part of Fluidity and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.fluidity.impl.article;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Internal
public class StackHelper {
	public static ItemStack newStack(Item item, CompoundTag tag, long count) {
		if (item == null || item == Items.AIR || count <= 0) {
			return ItemStack.EMPTY;
		}

		final ItemStack result = new ItemStack(item, (int) Math.min(item.getMaxStackSize(), count));
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

	public static boolean areStacksEqual(ItemStack stackA, ItemStack stackB) {
		if (stackA.isEmpty() && stackB.isEmpty()) {
			return true;
		}

		if (stackA.sameItemStackIgnoreDurability(stackB) && stackA.getCount() == stackB.getCount()) {
			return true;
		}

		return false;
	}
}
