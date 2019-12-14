package grondag.fluidity.api.item;

import net.minecraft.nbt.CompoundTag;

public interface ArticleItem {
	void writeTag(CompoundTag tag, String tagName);

	boolean isBulk();

	boolean isItem();
}
