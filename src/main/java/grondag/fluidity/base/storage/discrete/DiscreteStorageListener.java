package grondag.fluidity.base.storage.discrete;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.api.storage.StorageListener;

public interface DiscreteStorageListener extends StorageListener {
	@Override
	default void onCapacityChange(Storage storage, FractionView capacityDelta) {
		onCapacityChange(storage, capacityDelta.whole());
	}

	@Override
	default void onAccept(Storage storage, int handle, Article item, FractionView delta, FractionView newVolume) {
		onAccept(storage, handle, item, delta.whole(), newVolume.whole());
	}

	@Override
	default void onSupply(Storage storage, int handle, Article item, FractionView delta, FractionView newVolume) {
		onSupply(storage, handle, item, delta.whole(), newVolume.whole());
	}
}
