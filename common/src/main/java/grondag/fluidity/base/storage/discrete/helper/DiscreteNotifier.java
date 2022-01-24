/*
 * This file is part of Fluidity and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.fluidity.base.storage.discrete.helper;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.StoredDiscreteArticle;
import grondag.fluidity.base.storage.AbstractStore;

@Experimental
public class DiscreteNotifier {
	protected final AbstractStore<? extends StoredDiscreteArticle, ?> owner;

	public DiscreteNotifier(AbstractStore<? extends StoredDiscreteArticle, ?> owner) {
		this.owner = owner;
	}

	public void notifySupply(Article item, int handle, long delta, long newCount) {
		assert newCount >= 0;

		if (!owner.listeners.isEmpty()) {
			for (final StorageListener l : owner.listeners) {
				l.onSupply(owner, handle, item, delta, newCount);
			}
		}
	}

	public void notifySupply(StoredDiscreteArticle article, long delta) {
		notifySupply(article.article(), article.handle(), delta, article.count() - delta);
	}

	public void notifyAccept(Article item, int handle, long delta, long newCount) {
		assert newCount >= 0;

		if (!owner.listeners.isEmpty()) {
			for (final StorageListener l : owner.listeners) {
				l.onAccept(owner, handle, item, delta, newCount);
			}
		}
	}

	public void notifyAccept(StoredDiscreteArticle article, long delta) {
		notifyAccept(article.article(), article.handle(), delta, article.count());
	}

	public void notifyCapacityChange(long capacityDelta) {
		if (!owner.listeners.isEmpty()) {
			for (final StorageListener l : owner.listeners) {
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

		listener.onCapacityChange(owner, -capacity);
	}
}
