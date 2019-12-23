package grondag.fluidity.base.storage.component;

import grondag.fluidity.api.article.DiscreteArticleView;
import grondag.fluidity.api.item.DiscreteItem;
import grondag.fluidity.api.storage.DiscreteStorageListener;
import grondag.fluidity.base.article.DiscreteArticle;
import grondag.fluidity.base.storage.bulk.AbstractStorage;

public class DiscreteItemNotifier {
	protected final AbstractStorage<DiscreteArticleView,  DiscreteStorageListener, DiscreteItem> owner;

	public DiscreteItemNotifier(AbstractStorage<DiscreteArticleView,  DiscreteStorageListener, DiscreteItem> owner) {
		this.owner = owner;
	}

	public void notifySupply(DiscreteItem item, int handle, long delta, long newCount) {
		if(!owner.listeners.isEmpty()) {
			for(final DiscreteStorageListener l : owner.listeners) {
				l.onSupply(owner, handle, item, delta, newCount);
			}
		}
	}

	public void notifySupply(DiscreteArticle article, long delta) {
		if(!owner.listeners.isEmpty()) {
			final long newCount = article.count() - delta;
			final DiscreteItem item = article.item();
			final int handle = article.handle;

			for(final DiscreteStorageListener l : owner.listeners) {
				l.onAccept(owner, handle, item, delta, newCount);
			}
		}
	}

	public void notifyAccept(DiscreteItem item, int handle, long delta, long newCount) {
		if(!owner.listeners.isEmpty()) {
			for(final DiscreteStorageListener l : owner.listeners) {
				l.onAccept(owner, handle, item, delta, newCount);
			}
		}
	}

	public void notifyAccept(DiscreteArticle article, long delta) {
		if(!owner.listeners.isEmpty()) {
			final long newCount = article.count();
			final DiscreteItem item = article.item();
			final int handle = article.handle;

			for(final DiscreteStorageListener l : owner.listeners) {
				l.onAccept(owner, handle, item, delta, newCount);
			}
		}
	}

	public void notifyCapacityChange(long capacityDelta) {
		if(!owner.listeners.isEmpty()) {
			for(final DiscreteStorageListener l : owner.listeners) {
				l.onCapacityChange(owner, capacityDelta);
			}
		}
	}

	public void sendFirstListenerUpdate(DiscreteStorageListener listener, long capacity) {
		listener.onCapacityChange(owner, capacity);

		owner.forEach(a -> {
			if (!a.isEmpty()) {
				listener.onAccept(owner, a.handle(), a.item(), a.count(), a.count());
			}

			return true;
		});
	}
}
