package grondag.fluidity.api.item.base;

import java.util.function.Consumer;
import java.util.function.Predicate;

import grondag.fluidity.api.item.ItemStorage;
import grondag.fluidity.api.item.StoredItemView;
import grondag.fluidity.api.transact.TransactionContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class InventoryStorageAdapter implements ItemStorage<Void> {

	protected final Inventory inventory;

	public InventoryStorageAdapter(Inventory inventory) {
		this.inventory = inventory;
	}
	
	@Override
	public long capacity() {
		return inventory.getInvMaxStackAmount() * inventory.getInvSize();
	}

	@Override
	public long capacityAvailable() {
		int result = 0;
		final int size = inventory.getInvSize();
		for (int i = 0; i < size; i++) {
			ItemStack stack = inventory.getInvStack(i);
			if (stack.isEmpty()) {
				result += 64;
			} else {
				result += stack.getMaxCount() - stack.getCount();
			}
		}
		return result;
	}

	@Override
	public long accept(ItemStack article, long count, boolean simulate) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long supply(ItemStack article, long count, boolean simulate) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void forEach(Void connection, Predicate<StoredItemView> filter, Predicate<StoredItemView> consumer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forSlot(int slot, Consumer<StoredItemView> consumer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startListening(Consumer<StoredItemView> listener, Void connection,
			Predicate<StoredItemView> articleFilter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopListening(Consumer<StoredItemView> listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
