package grondag.fluidity.api.item;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * Represents a game resource that may be a fluid, xp, power or may be some other
 * thing that is quantified, stored or transported.<p>
 */
@FunctionalInterface
@API(status = Status.EXPERIMENTAL)
public interface StorageItem {
	default boolean isBulk() {
		return false;
	}

	@Nullable
	Fluid toFluid();

	default boolean isFluid() {
		return toFluid() != null;
	}

	default boolean isCommon() {
		return false;
	}

	default boolean isNothing() {
		return false;
	}

	default void writeTag(CompoundTag tag, String tagName) {
		tag.putString(tagName, StorageItemRegistry.INSTANCE.getId(this).toString());
	}

	default ItemStack toStack(long count) {
		return ItemStack.EMPTY;
	}

	default ItemStack toStack() {
		return ItemStack.EMPTY;
	}

	default boolean matches(ItemStack stack) {
		return stack == ItemStack.EMPTY;
	}

	static <V extends StorageItem> V fromTag(CompoundTag tag, String tagName) {
		return StorageItemRegistry.INSTANCE.get(tag.getString(tagName));
	}

	StorageItem NOTHING = () -> null;
}
