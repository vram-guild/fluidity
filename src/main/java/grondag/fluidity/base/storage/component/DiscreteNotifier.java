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
import grondag.fluidity.base.article.StoredDiscreteArticle;
import grondag.fluidity.base.storage.AbstractStorage;

@API(status = Status.EXPERIMENTAL)
public class DiscreteNotifier {
	protected final AbstractStorage<? extends StoredDiscreteArticle, ?> owner;

	public DiscreteNotifier(AbstractStorage<? extends StoredDiscreteArticle, ?> owner) {
		this.owner = owner;
	}

	public void notifySupply(Article item, int handle, long delta, long newCount) {
		if(!owner.listeners.isEmpty()) {
			for(final StorageListener l : owner.listeners) {
				l.onSupply(owner, handle, item, delta, newCount);
			}
		}
	}

	public void notifySupply(StoredDiscreteArticle article, long delta) {
		notifySupply(article.article(), article.handle(), delta, article.count() - delta);
	}

	public void notifyAccept(Article item, int handle, long delta, long newCount) {
		if(!owner.listeners.isEmpty()) {
			for(final StorageListener l : owner.listeners) {
				l.onAccept(owner, handle, item, delta, newCount);
			}
		}
	}

	public void notifyAccept(StoredDiscreteArticle article, long delta) {
		notifyAccept(article.article(), article.handle(), delta, article.count());
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
				listener.onAccept(owner, a.handle(), a.article(), a.count(), a.count());
			}

			return true;
		});
	}

	public void sendLastListenerUpdate(StorageListener listener, long capacity) {
		owner.forEach(a -> {
			if (!a.isEmpty()) {
				listener.onSupply(owner, a.handle(), a.article(), a.count(), 0);
			}

			return true;
		});

		listener.onCapacityChange(owner, 0);
	}
}
