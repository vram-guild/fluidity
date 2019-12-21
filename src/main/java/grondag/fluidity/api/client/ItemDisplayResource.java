package grondag.fluidity.api.client;

import net.minecraft.item.ItemStack;

public interface ItemDisplayResource {

	ItemStack sampleItemStack();

	boolean isStackEqual(ItemStack heldStack);
}
