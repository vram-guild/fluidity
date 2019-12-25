package grondag.fluidity.base.storage.component;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.DiscreteStoredArticle;
import grondag.fluidity.base.storage.AbstractStorage;

public class DiscreteNotifier {
	protected final AbstractStorage<DiscreteStoredArticle, ?> owner;

	public DiscreteNotifier(AbstractStorage<DiscreteStoredArticle, ?> owner) {
		this.owner = owner;
	}

	public void notifySupply(Article item, int handle, long delta, long newCount) {
		if(!owner.listeners.isEmpty()) {
			for(final StorageListener l : owner.listeners) {
				l.onSupply(owner, handle, item, delta, newCount);
			}
		}
	}

	public void notifySupply(DiscreteStoredArticle article, long delta) {
		if(!owner.listeners.isEmpty()) {
			final long newCount = article.count() - delta;
			final Article item = article.item();
			final int handle = article.handle;

			for(final StorageListener l : owner.listeners) {
				l.onAccept(owner, handle, item, delta, newCount);
			}
		}
	}

	public void notifyAccept(Article item, int handle, long delta, long newCount) {
		if(!owner.listeners.isEmpty()) {
			for(final StorageListener l : owner.listeners) {
				l.onAccept(owner, handle, item, delta, newCount);
			}
		}
	}

	public void notifyAccept(DiscreteStoredArticle article, long delta) {
		if(!owner.listeners.isEmpty()) {
			final long newCount = article.count();
			final Article item = article.item();
			final int handle = article.handle;

			for(final StorageListener l : owner.listeners) {
				l.onAccept(owner, handle, item, delta, newCount);
			}
		}
	}

	public void notifyCapacityChange(long capacityDelta) {
		if(!owner.listeners.isEmpty()) {
			for(final StorageListener l : owner.listeners) {
				l.onCapacityChange(owner, capacityDelta);
			}
		}
	}

	public void sendFirstListenerUpdate(StorageListener listener, long capacity) {
		listener.onCapacityChange(owner, capacity);

		owner.forEach(a -> {
			if (!a.isEmpty()) {
				listener.onAccept(owner, a.handle(), a.item(), a.count(), a.count());
			}

			return true;
		});
	}
}
