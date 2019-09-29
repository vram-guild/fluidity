package grondag.fluidity.api.item.base;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;

public class StackHelper {
	public static ItemStack newStack(Item item, CompoundTag tag, int count) {
		if (item == null || item == Items.AIR) {
			return ItemStack.EMPTY;
		}
		
		ItemStack result = new ItemStack(item, count);
		result.setTag(tag);
		return result;
	}
	
	public static boolean areItemsEqual(Item itemA, CompoundTag tagA, Item itemB, CompoundTag tagB) {
		if (itemA != itemB) return false;
		
		if (tagA == null) return tagB == null;
		
		return tagB != null && tagA.equals(tagB);
	}
	
	public static boolean areItemsEqual(Item item, CompoundTag tag, ItemStack stack) {
		if (item != stack.getItem()) return false;
		
		if (tag == null) return !stack.hasTag();
		
		return stack.hasTag() && tag.equals(stack.getTag());
	}
}
