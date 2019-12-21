package grondag.fluidity.api.client;

import net.minecraft.item.ItemStack;

public interface ItemDisplayContainer {

	boolean isDead();

	ItemDisplayResource resourceFromHandle(int resourceHandle);

	int storedCount(ItemDisplayResource targetResource);

	int add(ItemStack stack, int howMany);

	int takeUpTo(ItemDisplayResource targetResource, int howMany);
}
