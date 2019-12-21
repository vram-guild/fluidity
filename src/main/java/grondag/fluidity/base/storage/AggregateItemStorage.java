package grondag.fluidity.base.storage;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.DiscreteArticleView;
import grondag.fluidity.api.item.DiscreteItem;
import grondag.fluidity.api.storage.DiscreteStorage;
import grondag.fluidity.api.storage.DiscreteStorageListener;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.base.article.DiscreteArticle;

@API(status = Status.EXPERIMENTAL)
public class AggregateItemStorage extends AbstractAggregateStorage<DiscreteArticleView, DiscreteStorageListener, DiscreteItem, DiscreteArticle, DiscreteStorage> implements DiscreteStorage, DiscreteStorageListener {
	protected final DiscreteItem.Mutable lookupKey = new DiscreteItem.Mutable();
	protected long capacity;
	protected long count;

	public AggregateItemStorage(int startingSlotCount) {
		super(startingSlotCount);
	}
	public AggregateItemStorage() {
		this(32);
	}

	@Nullable
	protected DiscreteArticle getArticle(Item item, CompoundTag tag) {
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
			final DiscreteArticle article = findOrCreateArticle(item);
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

		final DiscreteArticle article = articles.get(item);

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
	protected DiscreteArticle newArticle() {
		return new DiscreteArticle();
	}

	@Override
	public DiscreteArticleView view(int slot) {
		return slots[slot];
	}

	@Override
	protected DiscreteStorageListener listener() {
		return this;
	}

	protected void notifySupply(DiscreteArticle article, long count) {
		this.count -= count;

		final int listenCount = listeners.size();

		if(listenCount > 0) {
			final long newCount = article.count();
			final DiscreteItem item = article.item();
			final int slot = article.slot;

			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onSupply(this, slot, item, count, newCount);
			}
		}
	}

	protected void notifyAccept(DiscreteArticle article, long count) {
		this.count += count;

		final int listenCount = listeners.size();

		if(listenCount > 0) {
			final long newCount = article.count();
			final DiscreteItem item = article.item();
			final int slot = article.slot;

			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onAccept(this, slot, item, count, newCount);
			}
		}
	}

	protected void notifyCapacityChange(long capacityDelta) {
		capacity += capacityDelta;

		final int listenCount = listeners.size();

		if(listenCount > 0) {
			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onCapacityChange(this, capacityDelta);
			}
		}
	}

	@Override
	protected void sendFirstListenerUpdate(DiscreteStorageListener listener) {
		listener.onCapacityChange(this, capacity);

		for(int i = 0 ; i < nextUnusedSlot; i++) {
			final DiscreteArticle article = slots[i];

			if (!article.isEmpty()) {
				listener.onAccept(this, i, article.item(), article.count(), article.count());
			}
		}
	}

	@Override
	public long count() {
		return count;
	}

	@Override
	public long capacity() {
		return capacity;
	}

	@Override
	public void onAccept(Storage<?, DiscreteStorageListener, ?> storage, int slot, DiscreteItem item, long delta, long newCount) {
		if (!itMe) {
			// TODO Auto-generated method stub
		}
	}

	@Override
	public void onSupply(Storage<?, DiscreteStorageListener, ?> storage, int slot, DiscreteItem item, long delta, long newCount) {
		if (!itMe) {
			// TODO Auto-generated method stub
		}
	}

	@Override
	public void onCapacityChange(Storage<?, DiscreteStorageListener, ?> storage, long capacityDelta) {
		capacity += capacityDelta;
	}

	@Override
	public void disconnect(Storage<?, DiscreteStorageListener, ?> target) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// NOOP - unsupported
	}
}
