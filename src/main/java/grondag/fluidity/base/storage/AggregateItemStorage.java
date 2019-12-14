package grondag.fluidity.base.storage;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.ArticleView;
import grondag.fluidity.api.article.ItemArticleView;
import grondag.fluidity.api.item.DiscreteItem;
import grondag.fluidity.api.storage.DiscreteStorage;
import grondag.fluidity.api.storage.DiscreteStorageListener;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.base.article.ItemArticle;

@SuppressWarnings("rawtypes")
@API(status = Status.EXPERIMENTAL)
public class AggregateItemStorage extends AbstractAggregateStorage<ItemArticleView, DiscreteStorageListener, DiscreteItem, ItemArticle> implements DiscreteStorage {
	protected final DiscreteItem.Mutable lookupKey = new DiscreteItem.Mutable();

	public AggregateItemStorage(int startingSlotCount) {
		super(startingSlotCount);
	}

	public AggregateItemStorage() {
		this(32);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	protected ItemArticle<AggregateItemStorage> getArticle(Item item, CompoundTag tag) {
		return articles.get(lookupKey.set(item, tag));
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

		for (final Storage store : stores) {
			enlister.accept(store);
			result += ((DiscreteStorage) store).accept(item, tag, count - result, simulate);

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

		final ItemArticle a = getArticle(item, tag);

		if(a == null || a.isEmpty()) {
			return 0;
		}

		// consolidate notifications
		itMe  = true;
		long result = 0;

		for (final DiscreteStorage store : a.stores) {
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

	@Override
	public long accept(DiscreteItem item, long count, boolean simulate) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long supply(DiscreteItem item, long count, boolean simulate) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected ItemArticle<AggregateItemStorage> newArticle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DiscreteItem keyFromArticleView(ArticleView a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemArticle<AggregateItemStorage> view(int slot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DiscreteStorageListener listener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void sendFirstListenerUpdate(DiscreteStorageListener listener) {
		// TODO Auto-generated method stub

	}
}
