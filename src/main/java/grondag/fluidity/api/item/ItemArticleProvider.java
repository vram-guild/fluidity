package grondag.fluidity.api.item;

import grondag.fluidity.api.article.ArticleProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public class ItemArticleProvider implements ArticleProvider<ItemStack> {
	public static final ItemArticleProvider INSTANCE = new ItemArticleProvider();
	
	private ItemArticleProvider() {};
	
	@Override
	public void toBuffer(ItemStack stack, PacketByteBuf buf) {
		if(stack.getCount() > 1) {
			stack = stack.copy();
			stack.setCount(1);
		}
		buf.writeItemStack(stack);
	}

	@Override
	public void toTag(ItemStack stack, CompoundTag tag) {
		if(stack.getCount() > 1) {
			stack = stack.copy();
			stack.setCount(1);
		}
		stack.toTag(tag);
	}

	@Override
	public ItemStack fromTag(CompoundTag tag) {
		return ItemStack.fromTag(tag);
	}

	@Override
	public ItemStack fromBuffer(PacketByteBuf buf) {
		return buf.readItemStack();
	}

	@Override
	public boolean areEqual(ItemStack stack1, ItemStack stack2) {
		return stack1 != null && stack2 != null && ItemStack.areItemsEqual(stack1, stack2);
	}

	@Override
	public int hashCode(ItemStack stack) {
		int result = stack.getItem().hashCode();
		CompoundTag tag = stack.getTag();
		if (tag != null) {
			result ^= tag.hashCode();
		}
		return result;
	}
}
