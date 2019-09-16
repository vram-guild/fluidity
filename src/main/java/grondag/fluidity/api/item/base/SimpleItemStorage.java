package grondag.fluidity.api.item.base;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import grondag.fluidity.api.item.ItemArticleView;
import grondag.fluidity.api.item.ItemPort;
import grondag.fluidity.api.item.ItemStorage;
import grondag.fluidity.api.storage.PortFilter;
import grondag.fluidity.api.storage.StopNotifier;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.api.transact.TransactionContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.inventory.InventoryListener;
import net.minecraft.item.ItemStack;

public class SimpleItemStorage implements ItemStorage {
	protected final int size;
    protected final ItemStack[] stacks; // second half contains copy during transaction
    protected ObjectArrayList<InventoryListener> listeners;
    protected boolean hasRollback = false;
    protected boolean needsNotify = false;
    
    public SimpleItemStorage(int size) {
        this.size = size;
        stacks = new ItemStack[size * 2];
    }
    
	@Override
	public long capacity() {
		return size * 64;
	}
	
	@Override
	public long capacityAvailable() {
		int result = 0;
        for (int i = 0; i < size; i++) {
        	ItemStack stack = stacks[i];
        	if (stack == null || stack.isEmpty()) {
        		result += 64;
        	} else {
        		result += stack.getMaxCount() - stack.getCount();
        	}
        }
		return result;
	}

	@Override
	public boolean isEmpty() {
        for (int i = 0; i < size; i++) {
            if (!(stacks[i] == null || stacks[i].isEmpty()))
                return false;
        }
        return true;
	}
	
	@Override
	public boolean hasSlots() {
		return true;
	}

	@Override
	public int slotCount() {
		return size;
	}


	private final ItemPort port = new ItemPort() {
		@Override
		public long accept(ItemStack article, long count, int flags) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long supply(ItemStack article, long count, int flags) {
			// TODO Auto-generated method stub
			return 0;
		}
	};
	
	private final ImmutableList<ItemPort> ports = ImmutableList.of(port);

	@Override
	public Iterable<ItemPort> ports(PortFilter portFilter) {
		return portFilter.test(port) ? ports : ImmutableList.of();
	}

	@Override
	public boolean canStore(ItemStack article) {
		return true;
	}

	@Override
	public boolean contains(ItemStack article) {
        for (int i = 0; i < size; i++) {
        	ItemStack stack = stacks[i];
        	if (stack != null && stack.isItemEqual(article)) {
        		return true;
        	}
        }
		return false;
	}

	@Override
	public Iterable<ItemArticleView> articles(PortFilter portFilter, Predicate<ItemStack> articleFilter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemArticleView articleForSlot(int slot) {
		return stacks[slot];
	}

	@Override
	public StopNotifier startListening(StorageListener<ItemArticleView, ItemStack> listener, PortFilter portFilter,
			Predicate<ItemStack> articleFilter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Consumer<TransactionContext> prepareTx(TransactionContext context) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
