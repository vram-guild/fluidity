package grondag.fluidity.api.storage;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.MathHelper;

import grondag.fluidity.api.article.ArticleView;
import grondag.fluidity.api.article.ItemArticleView;
import grondag.fluidity.api.item.ItemInstance;
import grondag.fluidity.api.item.ItemInstance.MutableItemInstance;
import grondag.fluidity.api.storage.base.AbstractStorage;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.Transactor;

public class AggregateItemStorage extends AbstractStorage implements ItemStorage {
	protected final MutableItemInstance lookupKey = new MutableItemInstance();
	protected final Consumer<TransactionContext> rollbackHandler = this::handleRollback;
	protected Article[] slots;
	protected final ObjectOpenHashSet<ItemStorage> stores = new ObjectOpenHashSet<>();
	protected final Object2ObjectOpenHashMap<ItemInstance, Article> articles = new Object2ObjectOpenHashMap<>();

	protected Consumer<Transactor> enlister = t -> {};
	protected int nextUnusedSlot = 0;
	protected int emptySlotCount = 0;
	protected boolean itMe = false;

	public AggregateItemStorage(int startingSlotCount) {
		startingSlotCount = MathHelper.smallestEncompassingPowerOfTwo(startingSlotCount);
		final Article[] slots = new Article[startingSlotCount];

		for(int i = 0; i < startingSlotCount; i++) {
			final Article a = new Article();
			a.slot = i;
			slots[i] = a;
		}

		this.slots = slots;
	}

	public AggregateItemStorage() {
		this(32);
	}

	@Override
	public int slotCount() {
		return nextUnusedSlot;
	}

	@Nullable
	protected Article getArticle(Item item, CompoundTag tag) {
		return articles.get(lookupKey.set(item, tag));
	}

	protected int getEmptySlot() {
		// fill empties first
		if(emptySlotCount > 0) {
			for(int i = 0; i < nextUnusedSlot; i++) {
				if(slots[i].isEmpty()) {
					return i;
				}
			}
		}

		// fill unused slot capacity
		final int slotCount = slots.length;

		if(nextUnusedSlot < slotCount) {
			return ++nextUnusedSlot;
		}

		// add slot capacity
		final int newCount = slotCount * 2;
		final Article[] newSlots = new Article[newCount];
		System.arraycopy(slots, 0, newSlots, 0, slotCount);

		for(int i = slotCount; i < newCount; i++) {
			final Article a = new Article();
			a.slot = i;
			newSlots[i] = a;
		}

		slots = newSlots;

		return ++nextUnusedSlot;
	}

	protected void compactSlots() {
		// no renumbering while listeners are active
		if(emptySlotCount == 0 || !listeners.isEmpty()) {
			return;
		}

		for (int i = nextUnusedSlot -  1; i > 0 && emptySlotCount > 0; --i) {
			if(slots[i].isEmpty()) {
				final int target = nextUnusedSlot - 1;

				if (i == target) {
					// already at end
					--nextUnusedSlot;
				} else {
					// swap with last non-empty and renumber
					final Article swap = slots[i];
					swap.slot = target;

					slots[i] = slots[target];
					slots[i].slot = i;

					slots[target] = swap;
				}

				--emptySlotCount;
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ArticleView> T view(int slot) {
		return (T) slots[slot];
	}

	@Override
	public boolean isEmpty() {
		return emptySlotCount == nextUnusedSlot;
	}

	@Override
	public boolean hasDynamicSlots() {
		return true;
	}

	@Override
	public long accept(Item item, @Nullable CompoundTag tag, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to accept null item");

		if (item == Items.AIR || stores.isEmpty()) {
			return 0;
		}

		// consolidate notifications
		itMe  = true;
		long result = 0;

		for (final ItemStorage store : stores) {
			enlister.accept(store);
			result += store.accept(item, tag, count - result, simulate);

			if (result == count) {
				break;
			}
		}

		itMe = false;

		if(result > 0 && !simulate) {
			notifyListeners(getArticle(item, tag));
		}

		return result;
	}

	@Override
	public long supply(Item item, @Nullable CompoundTag tag, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to supply negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to supply null item");

		if (item == Items.AIR || articles.isEmpty()) {
			return 0;
		}

		final Article a = getArticle(item, tag);

		if(a == null || a.isEmpty()) {
			return 0;
		}

		// consolidate notifications
		itMe  = true;
		long result = 0;

		for (final ItemStorage store : a.stores) {
			enlister.accept(store);
			result += store.supply(item, tag, count - result, simulate);

			if (result == count) {
				break;
			}
		}

		itMe = false;

		if(result > 0 && !simulate) {
			notifyListeners(a);
		}

		return result;
	}

	protected void handleRollback(TransactionContext context) {
		enlister = context.getState();
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		context.setState(enlister);
		enlister = context.enlister();
		return rollbackHandler;
	}

	protected class Article implements ItemArticleView {
		int slot;
		ItemInstance item;
		long count;
		ObjectArraySet<ItemStorage> stores;

		@Override
		public ItemStack toStack() {
			return item.toStack(count);
		}

		@Override
		public int slot() {
			return slot;
		}

		@Override
		public boolean isEmpty() {
			return count > 0;
		}

		@Override
		public long count() {
			return count;
		}

		@Override
		public boolean hasTag() {
			return item.getTag() != null;
		}

		@Override
		public Item item() {
			return item.getItem();
		}

		@Override
		public CompoundTag tag() {
			return item.getTag();
		}

		@Override
		public boolean isItem() {
			return true;
		}

		@Override
		public ItemArticleView toItemView() {
			return this;
		}
	}
}
