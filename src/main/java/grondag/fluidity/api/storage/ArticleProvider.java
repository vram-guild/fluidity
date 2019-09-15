package grondag.fluidity.api.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

public interface ArticleProvider<T> {
	SimpleRegistry<ArticleProvider<?>> REGISTRY = Registry.REGISTRIES.add(new Identifier("fluidity:article_providers"), 
            new SimpleRegistry<ArticleProvider<?>>());
	
	default boolean isFluid() {
		return false;
	}
	
	default boolean isItem() {
		return false;
	}
	
	default boolean hasVolume() {
		return false;
	}

	default boolean hasTag() {
		return false;
	}
	
	default CompoundTag tag(T data) {
		return null;
	}
	
	default ItemStack toStack(T data) {
		return ItemStack.EMPTY;
	}
	
	void writeBuffer(T data, PacketByteBuf buf);

	void writeTag(T data, CompoundTag tag);

	T fromTag(CompoundTag tag);

	T fromBuffer(PacketByteBuf buf);
}
