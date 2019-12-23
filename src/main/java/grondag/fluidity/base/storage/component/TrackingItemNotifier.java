package grondag.fluidity.base.storage.component;

import grondag.fluidity.api.article.DiscreteArticleView;
import grondag.fluidity.api.item.DiscreteItem;
import grondag.fluidity.api.storage.DiscreteStorageListener;
import grondag.fluidity.base.article.DiscreteArticle;
import grondag.fluidity.base.storage.bulk.AbstractStorage;

public class TrackingItemNotifier extends DiscreteItemNotifier{
	protected long capacity;
	protected long count;

	public TrackingItemNotifier(long capacity, AbstractStorage<DiscreteArticleView,  DiscreteStorageListener, DiscreteItem> owner) {
		super(owner);
		this.capacity = capacity;
	}

	@Override
	public void notifySupply(DiscreteItem item, int handle, long delta, long newCount) {
		count -= delta;
		super.notifySupply(item, handle, delta, newCount);
	}

	@Override
	public void notifySupply(DiscreteArticle article, long delta) {
		count -= delta;
		super.notifySupply(article, delta);
	}

	@Override
	public void notifyAccept(DiscreteItem item, int handle, long delta, long newCount) {
		count += delta;
		super.notifyAccept(item, handle, delta, newCount);
	}

	@Override
	public void notifyAccept(DiscreteArticle article, long delta) {
		count += delta;
		super.notifyAccept(article, delta);
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
}
