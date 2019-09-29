package grondag.fluidity.api.storage;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import grondag.fluidity.api.item.base.ItemStackView;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeInputProvider;

public interface StorageImpl extends Storage, Inventory, RecipeInputProvider {
	List<Consumer<ItemView>> listeners();
	
	@Override
	default void startListening(Consumer<ItemView> listener, Object connection, Predicate<ItemView> articleFilter) {
		listeners().add(listener);
		this.forEach(v -> {
			listener.accept(v);
			return true;
		});
	}

	@Override
	default void stopListening(Consumer<ItemView> listener) {
		listeners().remove(listener);
	}

	default void notifyListeners(ItemView article) {
		final List<Consumer<ItemView>> listeners = listeners();
		final int limit = listeners.size();
		
		for (int i = 0; i < limit; i++) {
			listeners.get(i).accept(article);
		}
	}
	
	default ItemStackView notifyListeners(@Nullable ItemStackView view, ItemStack stack, int slot) {
		final List<Consumer<ItemView>> listeners = listeners();
		if (listeners.isEmpty()) return view;
		
		if (view == null) view = new ItemStackView();
		
		notifyListeners(view.prepare(stack, slot));
		
		return view;
	}
}
