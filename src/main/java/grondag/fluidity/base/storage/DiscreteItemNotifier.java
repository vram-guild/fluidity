package grondag.fluidity.base.storage;

import java.util.List;

import javax.annotation.Nullable;

import grondag.fluidity.api.item.DiscreteItem;
import grondag.fluidity.api.storage.DiscreteStorage;
import grondag.fluidity.api.storage.DiscreteStorageListener;
import grondag.fluidity.base.article.DiscreteArticle;

public class DiscreteItemNotifier {
	protected long capacity;
	protected long count;

	protected final DiscreteStorage owner;
	protected final FlexibleSlotManager<?,DiscreteArticle> slots;

	public DiscreteItemNotifier(long capacity, DiscreteStorage owner, @Nullable FlexibleSlotManager<?, DiscreteArticle> slots) {
		this.owner = owner;
		this.capacity = capacity;
		this.slots = slots;
	}

	public void notifySupply(DiscreteArticle article, long count) {
		this.count -= count;

		final List<DiscreteStorageListener> listeners = owner.listeners();

		final int listenCount = listeners.size();
		final long newCount = article.count();

		if(listenCount > 0) {
			final DiscreteItem item = article.item();
			final int slot = article.slot;

			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onSupply(owner, slot, item, count, newCount);
			}
		} else if(newCount == 0 && slots != null) {
			slots.compactSlots();
		}
	}

	public void notifyAccept(DiscreteArticle article, long count) {
		this.count += count;

		final List<DiscreteStorageListener> listeners = owner.listeners();
		final int listenCount = listeners.size();

		if(listenCount > 0) {
			final long newCount = article.count();
			final DiscreteItem item = article.item();
			final int slot = article.slot;

			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onAccept(owner, slot, item, count, newCount);
			}
		}
	}

	public void notifyCapacityChange(long capacityDelta) {
		capacity += capacityDelta;

		final List<DiscreteStorageListener> listeners = owner.listeners();
		final int listenCount = listeners.size();

		if(listenCount > 0) {
			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onCapacityChange(owner, capacityDelta);
			}
		}
	}

	protected void sendFirstListenerUpdate(DiscreteStorageListener listener) {
		listener.onCapacityChange(owner, capacity);

		if(slots != null) {
			final int limit = slots.slotCount();
			for(int i = 0 ; i < limit; i++) {
				final DiscreteArticle article = slots.get(i);

				if (!article.isEmpty()) {
					listener.onAccept(owner, i, article.item(), article.count(), article.count());
				}
			}
		}
	}
}
