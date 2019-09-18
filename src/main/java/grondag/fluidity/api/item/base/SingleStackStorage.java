package grondag.fluidity.api.item.base;

import java.util.function.Consumer;
import java.util.function.Predicate;

import grondag.fluidity.api.item.ItemStorage;
import grondag.fluidity.api.item.StoredItemView;
import grondag.fluidity.api.transact.TransactionContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;

public class SingleStackStorage extends ItemStackView implements ItemStorage<Void> {
	protected ObjectArrayList<Consumer<StoredItemView>> listeners;
	
	public SingleStackStorage() {
		stack = ItemStack.EMPTY;
		slot = 0;
	}
	
	@Override
	public long capacity() {
		return stack.getMaxCount();
	}

	@Override
	public long capacityAvailable() {
		return stack.getMaxCount() - stack.getCount();
	}
	
	@Override
	public boolean fixedSlots() {
		return true;
	}

	@Override
	public int slotCount() {
		return 1;
	}

	@Override
	public long accept(ItemStack article, long count, boolean simulate) {
		final ItemStack stack = this.stack;
		if (!stack.isItemEqual(article)) {
			return 0;
		}
		
		int result = Math.min((int)count, stack.getMaxCount() - stack.getCount());
		
		if (!simulate) {
			stack.increment(result);
			notifyListeners();
		}
		return result;
	}

	@Override
	public long supply(ItemStack article, long count, boolean simulate) {
		final ItemStack stack = this.stack;
		if (!stack.isItemEqual(article)) {
			return 0;
		}
		
		int result = Math.min((int)count, stack.getCount());
		
		if (!simulate) {
			stack.decrement(result);
			notifyListeners();
		}
		return result;
	}

	@Override
	public void forEach(Void connection, Predicate<StoredItemView> filter, Predicate<StoredItemView> consumer) {
		if (filter.test(this)) {
			consumer.test(this);
		}
	}

	@Override
	public void forSlot(int slot, Consumer<StoredItemView> consumer) {
		if (slot == 0) {
			consumer.accept(this);
		}
	}

	@Override
	public void startListening(Consumer<StoredItemView> listener, Void connection, Predicate<StoredItemView> articleFilter) {
        if (listeners == null) {
            listeners = new ObjectArrayList<>();
        }
        listeners.add(listener);
		if(stack != null && !stack.isEmpty()) {
			listener.accept(this);
		}
	}

	@Override
	public void stopListening(Consumer<StoredItemView> listener) {
		if (listeners != null) {
            listeners.remove(listener);
        }
	}

    protected void notifyListeners() {
        if (this.listeners != null) {
        	final int limit = listeners.size();
        	for  (int i = 0; i < limit; i++) {
        		listeners.get(i).accept(this);
        	}
        }
    }
    
	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
    	context.setState(stack.copy());
		return rollackHandler;
	}
	
	private final Consumer<TransactionContext> rollackHandler = this::handleRollback;
	
	private void handleRollback(TransactionContext context) {
		if (!context.isCommited()) {
			ItemStack state = context.getState();
			if(!stack.isItemEqual(state)) {
				stack.setTag(state.getTag());
				stack.setCount(state.getCount());
				notifyListeners();
			}
		}
	}
}
