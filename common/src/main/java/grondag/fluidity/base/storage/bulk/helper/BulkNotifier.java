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

package grondag.fluidity.base.storage.bulk.helper;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.StoredBulkArticle;
import grondag.fluidity.base.storage.AbstractStore;

@Experimental
public class BulkNotifier {
	protected final AbstractStore<? extends StoredBulkArticle, ?> owner;

	public BulkNotifier(AbstractStore<? extends StoredBulkArticle, ?> owner) {
		this.owner = owner;
	}

	public void notifySupply(Article item, int handle, Fraction delta, Fraction newAmount) {
		assert !newAmount.isNegative();

		if (!owner.listeners.isEmpty()) {
			for (final StorageListener l : owner.listeners) {
				l.onSupply(owner, handle, item, delta, newAmount);
			}
		}
	}

	public void notifyAccept(Article item, int handle, Fraction delta, Fraction newAmount) {
		assert !newAmount.isNegative();

		if (!owner.listeners.isEmpty()) {
			for (final StorageListener l : owner.listeners) {
				l.onAccept(owner, handle, item, delta, newAmount);
			}
		}
	}

	public void notifyCapacityChange(Fraction capacityDelta) {
		if (!owner.listeners.isEmpty()) {
			for (final StorageListener l : owner.listeners) {
				l.onCapacityChange(owner, capacityDelta);
			}
		}
	}

	public void sendFirstListenerUpdate(StorageListener listener, Fraction capacity) {
		listener.onCapacityChange(owner, capacity);

		owner.forEach(a -> {
			if (!a.isEmpty()) {
				listener.onAccept(owner, a.handle(), a.article(), a.amount(), a.amount());
			}

			return true;
		});
	}

	public void sendLastListenerUpdate(StorageListener listener, Fraction capacity) {
		owner.forEach(a -> {
			if (!a.isEmpty()) {
				listener.onSupply(owner, a.handle(), a.article(), a.amount(), Fraction.ZERO);
			}

			return true;
		});

		listener.onCapacityChange(owner, capacity.toNegated());
	}
}
