package grondag.fluidity.base.storage;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.ItemArticleView;
import grondag.fluidity.api.item.DiscreteItem;
import grondag.fluidity.api.storage.DiscreteStorage;
import grondag.fluidity.api.storage.DiscreteStorageListener;
import grondag.fluidity.base.article.ItemArticle;

@API(status = Status.EXPERIMENTAL)
public class AggregateItemStorage extends AbstractAggregateStorage<ItemArticleView, DiscreteStorageListener, DiscreteItem, ItemArticle, DiscreteStorage> implements DiscreteStorage, DiscreteStorageListener {
	protected final DiscreteItem.Mutable lookupKey = new DiscreteItem.Mutable();

	public AggregateItemStorage(int startingSlotCount) {
		super(startingSlotCount);
	}
	public AggregateItemStorage() {
		this(32);
	}

	@Nullable
	protected ItemArticle getArticle(Item item, CompoundTag tag) {
		return articles.get(lookupKey.set(item, tag));
	}

	@Override
	public long accept(DiscreteItem item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to accept null item");

		if (item.isEmpty() || stores.isEmpty()) {
			return 0;
		}

		// consolidate notifications
		itMe  = true;
		long result = 0;

		for (final DiscreteStorage store : stores) {
			enlister.accept(store);
			result += store.accept(item, count - result, simulate);

			if (result == count) {
				break;
			}
		}

		itMe = false;

		if(result > 0 && !simulate) {
			final ItemArticle article = findOrCreateArticle(item);
			article.count += result;
			notifyAccept(article, result);
		}

		return result;
	}

	@Override
	public long supply(DiscreteItem item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to supply negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to supply null item");

		if (item.isEmpty() || articles.isEmpty()) {
			return 0;
		}

		final ItemArticle article = articles.get(item);

		if(article == null || article.isEmpty()) {
			return 0;
		}

		// consolidate notifications
		itMe  = true;
		long result = 0;

		for (final DiscreteStorage store : article.stores) {
			enlister.accept(store);
			result += store.supply(item, count - result, simulate);

			if (result == count) {
				break;
			}
		}

		itMe = false;

		if(result > 0 && !simulate) {
			notifySupply(article, result);
			article.count -= result;
		}

		return result;
	}

	@Override
	protected ItemArticle newArticle() {
		return new ItemArticle();
	}

	@Override
	public ItemArticleView view(int slot) {
		return slots[slot];
	}

	@Override
	protected DiscreteStorageListener listener() {
		return this;
	}

	protected void notifySupply(ItemArticle article, long count) {
		final int listenCount = listeners.size();

		if(listenCount > 0) {
			final boolean isEmpty = article.isEmpty();
			final DiscreteItem item = article.item();
			final int slot = article.slot;

			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onSupply(slot, item, count, isEmpty);
			}
		}
	}

	protected void notifyAccept(ItemArticle article, long result) {
		final int listenCount = listeners.size();

		if(listenCount > 0) {
			final boolean wasEmpty = article.count() == result;
			final DiscreteItem item = article.item();
			final int slot = article.slot;

			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onAccept(slot, item, result, wasEmpty);
			}
		}
	}

	@Override
	protected void sendFirstListenerUpdate(DiscreteStorageListener listener) {
		for(int i = 0 ; i < nextUnusedSlot; i++) {
			final ItemArticle article = slots[i];

			if (!article.isEmpty()) {
				listener.onAccept(i, article.item(), article.count(), true);
			}
		}
	}

	@Override
	public long onAccept(int slot, DiscreteItem item, long count, boolean wasEmpty) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long onSupply(int slot, DiscreteItem item, long count, boolean isEmpty) {
		// TODO Auto-generated method stub
		return 0;
	}
}
