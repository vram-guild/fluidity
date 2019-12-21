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
	protected final DiscreteItemNotifier notifier;

	public AggregateItemStorage(int startingSlotCount) {
		super(startingSlotCount);
		notifier = new DiscreteItemNotifier(0, this, articles);
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
			final DiscreteArticle article = articles.findOrCreateArticle(item);
			article.count += result;
			notifier.notifyAccept(article, result);
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
		return notifier.count;
	}

	@Override
	public long capacity() {
		return notifier.capacity;
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
		notifier.capacity += capacityDelta;
	}

	@Override
	public void disconnect(Storage<?, DiscreteStorageListener, ?> target) {
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
}
