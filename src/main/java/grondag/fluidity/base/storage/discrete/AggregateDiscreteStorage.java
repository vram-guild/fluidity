package grondag.fluidity.base.storage.discrete;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.DiscreteStoredArticle;
import grondag.fluidity.base.storage.AbstractAggregateStorage;
import grondag.fluidity.base.storage.component.DiscreteTrackingNotifier;

@API(status = Status.EXPERIMENTAL)
public class AggregateDiscreteStorage extends AbstractAggregateStorage<DiscreteStoredArticle, AggregateDiscreteStorage> implements DiscreteStorage, DiscreteStorageListener {
	protected final DiscreteTrackingNotifier notifier;

	public AggregateDiscreteStorage(int startingSlotCount) {
		super(startingSlotCount);
		notifier = new DiscreteTrackingNotifier(0, this);
	}
	public AggregateDiscreteStorage() {
		this(32);
	}

	@Nullable
	protected DiscreteStoredArticle getArticle(Item item, CompoundTag tag) {
		return articles.get(Article.of(item, tag));
	}

	@Override
	public long accept(Article item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to accept null item");

		if (item.isNothing() || stores.isEmpty()) {
			return 0;
		}

		// consolidate notifications
		itMe  = true;
		long result = 0;

		for (final Storage store : stores) {
			enlister.accept(store);
			result += store.accept(item, count - result, simulate);

			if (result == count) {
				break;
			}
		}

		itMe = false;

		if(result > 0 && !simulate) {
			final DiscreteStoredArticle article = articles.findOrCreateArticle(item);
			article.count += result;
			notifier.notifyAccept(article, result);
		}

		return result;
	}

	@Override
	public long supply(Article item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to supply negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to supply null item");

		if (item.isNothing() || isEmpty()) {
			return 0;
		}

		final DiscreteStoredArticle article = articles.get(item);

		if(article == null || article.isEmpty()) {
			return 0;
		}

		// consolidate notifications
		itMe  = true;
		long result = 0;

		for (final Storage store : article.stores) {
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
	protected DiscreteStoredArticle newArticle() {
		return new DiscreteStoredArticle();
	}

	@Override
	public StoredArticleView view(int slot) {
		return articles.get(slot);
	}

	@Override
	protected StorageListener listener() {
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
	public void onAccept(Storage storage, int slot, Article item, long delta, long newCount) {
		if (!itMe) {
			// TODO Auto-generated method stub
		}
	}

	@Override
	public void onSupply(Storage storage, int slot, Article item, long delta, long newCount) {
		if (!itMe) {
			// TODO Auto-generated method stub
		}
	}

	@Override
	public void onCapacityChange(Storage storage, long capacityDelta) {
		notifier.changeCapacity(capacityDelta);
	}

	@Override
	public void disconnect(Storage target) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// NOOP - unsupported
	}

	@Override
	protected void sendFirstListenerUpdate(StorageListener listener) {
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
