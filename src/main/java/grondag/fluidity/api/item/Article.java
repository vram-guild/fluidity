package grondag.fluidity.api.item;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.impl.CommonItem;

/**
 * Represents a game resource that may be a fluid, xp, power or may be some other
 * thing that is quantified, stored or transported.<p>
 *
 * TODO: other article metadata: units (w/ registry, fixed for items and fluids)
 */
@API(status = Status.EXPERIMENTAL)
public interface Article {
	default boolean isBulk() {
		return false;
	}

	@Nullable
	default Fluid toFluid() {
		return null;
	}

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
		tag.putString(tagName, ArticleRegistry.INSTANCE.getId(this).toString());
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

	default boolean hasTag() {
		return false;
	}

	Article NOTHING = new Article() {

	};

	static <V extends Article> V fromTag(CompoundTag tag, String tagName) {
		return ArticleRegistry.INSTANCE.get(tag.getString(tagName));
	}

	default Item getItem() {
		return Items.AIR;
	}

	default boolean isEmpty() {
		return false;
	}

	default boolean doesTagMatch(CompoundTag otherTag) {
		return false;
	}

	static Article of(ItemStack stack) {
		return CommonItem.of(stack);
	}

	static Article of(Item item, CompoundTag tag) {
		return CommonItem.of(item, tag);
	}

	static Article of(Item item) {
		return CommonItem.of(item);
	}
}
