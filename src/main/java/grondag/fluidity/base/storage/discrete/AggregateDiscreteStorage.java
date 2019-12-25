package grondag.fluidity.base.storage.discrete;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.DiscreteArticleView;
import grondag.fluidity.api.item.CommonItem;
import grondag.fluidity.api.item.StorageItem;
import grondag.fluidity.api.storage.CommonStorage;
import grondag.fluidity.api.storage.DiscreteStorage;
import grondag.fluidity.api.storage.DiscreteStorageListener;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.base.article.DiscreteArticle;
import grondag.fluidity.base.storage.AbstractAggregateStorage;
import grondag.fluidity.base.storage.component.TrackingItemNotifier;

@API(status = Status.EXPERIMENTAL)
public class AggregateDiscreteStorage extends AbstractAggregateStorage<DiscreteArticleView, DiscreteStorageListener, DiscreteArticle, DiscreteStorage> implements CommonStorage, DiscreteStorageListener {
	protected final CommonItem.Mutable lookupKey = new CommonItem.Mutable();
	protected final TrackingItemNotifier notifier;

	public AggregateDiscreteStorage(int startingSlotCount) {
		super(startingSlotCount);
		notifier = new TrackingItemNotifier(0, this);
	}
	public AggregateDiscreteStorage() {
		this(32);
	}

	@Nullable
	protected DiscreteArticle getArticle(Item item, CompoundTag tag) {
		return articles.get(lookupKey.set(item, tag));
	}

	@Override
	public long accept(StorageItem item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to accept null item");

		if (item.isNothing() || stores.isEmpty()) {
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
			final DiscreteArticle article = articles.findOrCreateArticle(item);
			article.count += result;
			notifier.notifyAccept(article, result);
		}

		return result;
	}

	@Override
	public long supply(StorageItem item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to supply negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to supply null item");

		if (item.isNothing() || isEmpty()) {
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
			notifier.notifySupply(article, result);
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
		return articles.get(slot);
	}

	@Override
	protected DiscreteStorageListener listener() {
		return this;
	}

	@Override
	public long count() {
		return notifier.count();
	}

	@Override
	public long capacity() {
		return notifier.capacity();
	}

	@Override
	public void onAccept(Storage<?, DiscreteStorageListener> storage, int slot, StorageItem item, long delta, long newCount) {
		if (!itMe) {
			// TODO Auto-generated method stub
		}
	}

	@Override
	public void onSupply(Storage<?, DiscreteStorageListener> storage, int slot, StorageItem item, long delta, long newCount) {
		if (!itMe) {
			// TODO Auto-generated method stub
		}
	}

	@Override
	public void onCapacityChange(Storage<?, DiscreteStorageListener> storage, long capacityDelta) {
		notifier.changeCapacity(capacityDelta);
	}

	@Override
	public void disconnect(Storage<?, DiscreteStorageListener> target) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// NOOP - unsupported
	}

	@Override
	protected void sendFirstListenerUpdate(DiscreteStorageListener listener) {
		notifier.sendFirstListenerUpdate(listener);
	}

	@Override
	public CompoundTag writeTag() {
		throw new UnsupportedOperationException("Aggregate storage view do not support saving");
	}

	@Override
	public void readTag(CompoundTag tag) {
		throw new UnsupportedOperationException("Aggregate storage view do not support saving");
	}

	@Override
	protected void onListenersEmpty() {
		articles.compact();
	}
}
