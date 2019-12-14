package grondag.fluidity.base.storage;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.storage.ItemStorage;
import grondag.fluidity.base.article.ItemArticle;
import grondag.fluidity.base.item.ItemInstance;
import grondag.fluidity.base.item.ItemInstance.MutableItemInstance;

@API(status = Status.EXPERIMENTAL)
public class AggregateItemStorage extends AbstractAggregateStorage<ItemArticle, ItemStorage, ItemInstance> implements ItemStorage {
	protected final MutableItemInstance lookupKey = new MutableItemInstance();

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

		final ItemArticle a = getArticle(item, tag);

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

	@Override
	protected ItemArticle newArticle() {
		return new ItemArticle();
	}
}
