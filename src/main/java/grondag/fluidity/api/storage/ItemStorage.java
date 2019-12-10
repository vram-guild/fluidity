package grondag.fluidity.api.storage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeInputProvider;

public interface ItemStorage extends Storage, Inventory, RecipeInputProvider {
	long accept(Item item, CompoundTag tag, long count, boolean simulate);

	default long accept(Item item, long count, boolean simulate) {
		return accept(item, null, count, simulate);
	}

	default long accept(ItemStack stack, long count, boolean simulate) {
		return accept(stack.getItem(), stack.getTag(), count, simulate);
	}

	default long accept(ItemStack stack, boolean simulate) {
		return accept(stack.getItem(), stack.getTag(), stack.getCount(), simulate);
	}

	long supply(Item item, CompoundTag tag, long count, boolean simulate);

	default long supply(Item item, long count, boolean simulate) {
		return supply(item, null, count, simulate);
	}

	default long supply(ItemStack stack, long count, boolean simulate) {
		return supply(stack.getItem(), stack.getTag(), count, simulate);
	}

	default long supply(ItemStack stack, boolean simulate) {
		return supply(stack.getItem(), stack.getTag(), stack.getCount(), simulate);
	}

	@Override default boolean canPlayerUseInv(PlayerEntity player) {
		return true;
	}

	@Override
	default void provideRecipeInputs(RecipeFinder finder) {
		this.forEach(v -> {
			if (!v.isEmpty()  && v.isItem()) {
				finder.addItem(v.toItemView().toStack());
			}

			return true;
		});
	}

	@Override
	default int getInvSize() {
		return slotCount();
	}

	@Override
	default boolean isInvEmpty() {
		return isEmpty();
	}
}
