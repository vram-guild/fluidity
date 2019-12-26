/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.fluidity.base.storage.component;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.DiscreteStoredArticle;
import grondag.fluidity.base.storage.AbstractStorage;

@API(status = Status.EXPERIMENTAL)
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
