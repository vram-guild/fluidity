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
	protected final FlexibleArticleManager<?,DiscreteArticle> articles;

	public DiscreteItemNotifier(long capacity, DiscreteStorage owner, @Nullable FlexibleArticleManager<?, DiscreteArticle> articles) {
		this.owner = owner;
		this.capacity = capacity;
		this.articles = articles;
	}

	public void notifySupply(DiscreteArticle article, long count) {
		this.count -= count;

		final List<DiscreteStorageListener> listeners = owner.listeners();

		final int listenCount = listeners.size();
		final long newCount = article.count() - count;

		if(listenCount > 0) {
			final DiscreteItem item = article.item();
			final int handle = article.handle;

			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onSupply(owner, handle, item, count, newCount);
			}
		} else if(newCount == 0 && articles != null) {
			articles.compact();
		}
	}

	public void notifyAccept(DiscreteArticle article, long count) {
		this.count += count;

		final List<DiscreteStorageListener> listeners = owner.listeners();
		final int listenCount = listeners.size();

		if(listenCount > 0) {
			final long newCount = article.count();
			final DiscreteItem item = article.item();
			final int handle = article.handle;

			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onAccept(owner, handle, item, count, newCount);
			}
		}
	}

	public void setCapacity(long newCapacity) {
		if(newCapacity != capacity) {
			notifyCapacityChange(newCapacity - capacity);
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

		if(articles != null) {
			final int limit = articles.handleCount();

			for(int i = 0 ; i < limit; i++) {
				final DiscreteArticle article = articles.get(i);

				if (!article.isEmpty()) {
					listener.onAccept(owner, i, article.item(), article.count(), article.count());
				}
			}
		}
	}
}
