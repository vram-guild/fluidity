package grondag.fluidity.base.storage.component;

import grondag.fluidity.api.article.DiscreteArticleView;
import grondag.fluidity.api.item.Article;
import grondag.fluidity.api.storage.discrete.DiscreteStorageListener;
import grondag.fluidity.base.article.DiscreteStoredArticle;
import grondag.fluidity.base.storage.AbstractStorage;

public class TrackingItemNotifier extends DiscreteItemNotifier{
	protected long capacity;
	protected long count;
	protected int articleCount = 0;

	public TrackingItemNotifier(long capacity, AbstractStorage<DiscreteArticleView,  DiscreteStorageListener> owner) {
		super(owner);
		this.capacity = capacity;
	}

	@Override
	public void notifySupply(Article item, int handle, long delta, long newCount) {
		if (delta > 0) {
			count -= delta;
			super.notifySupply(item, handle, delta, newCount);

			if(newCount == 0) {
				--articleCount;
			}
		}
	}

	@Override
	public void notifySupply(DiscreteStoredArticle article, long delta) {
		if (delta > 0) {
			count -= delta;
			super.notifySupply(article, delta);

			if(article.count == delta) {
				--articleCount;
			}
		}
	}

	@Override
	public void notifyAccept(Article item, int handle, long delta, long newCount) {
		if (delta > 0) {
			count += delta;
			super.notifyAccept(item, handle, delta, newCount);

			if(newCount == delta) {
				++articleCount;
			}
		}
	}

	@Override
	public void notifyAccept(DiscreteStoredArticle article, long delta) {
		if (delta > 0) {
			count += delta;
			super.notifyAccept(article, delta);

			if(article.count == delta) {
				++articleCount;
			}
		}
	}

	public void setCapacity(long newCapacity) {
		if(newCapacity != capacity) {
			notifyCapacityChange(newCapacity - capacity);
		}
	}

	public void changeCapacity(long delta) {
		setCapacity(capacity + delta);
	}

	@Override
	public void notifyCapacityChange(long capacityDelta) {
		capacity += capacityDelta;
		super.notifyCapacityChange(capacityDelta);
	}

	public void sendFirstListenerUpdate(DiscreteStorageListener listener) {
		super.sendFirstListenerUpdate(listener, capacity);
	}

	public long count() {
		return count;
	}

	public long capacity() {
		return capacity;
	}

	public int articleCount() {
		return articleCount;
	}
}
