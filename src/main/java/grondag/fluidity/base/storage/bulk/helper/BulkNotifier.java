/*******************************************************************************
 * Copyright 2019, 2020 grondag
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
package grondag.fluidity.base.storage.bulk.helper;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.StoredBulkArticle;
import grondag.fluidity.base.storage.AbstractStorage;

@API(status = Status.EXPERIMENTAL)
public class BulkNotifier {
	protected final AbstractStorage<? extends StoredBulkArticle, ?> owner;

	public BulkNotifier(AbstractStorage<? extends StoredBulkArticle, ?> owner) {
		this.owner = owner;
	}

	public void notifySupply(Article item, int handle, FractionView delta, FractionView newAmount) {
		assert !newAmount.isNegative() && !newAmount.isZero();

		if(!owner.listeners.isEmpty()) {
			for(final StorageListener l : owner.listeners) {
				l.onSupply(owner, handle, item, delta, newAmount);
			}
		}
	}

	public void notifyAccept(Article item, int handle, FractionView delta, FractionView newAmount) {
		assert !newAmount.isNegative() && !newAmount.isZero();

		if(!owner.listeners.isEmpty()) {
			for(final StorageListener l : owner.listeners) {
				l.onAccept(owner, handle, item, delta, newAmount);
			}
		}
	}

	public void notifyCapacityChange(FractionView capacityDelta) {
		if(!owner.listeners.isEmpty()) {
			for(final StorageListener l : owner.listeners) {
				l.onCapacityChange(owner, capacityDelta);
			}
		}
	}

	public void sendFirstListenerUpdate(StorageListener listener, FractionView capacity) {
		listener.onCapacityChange(owner, capacity);

		owner.forEach(a -> {
			if (!a.isEmpty()) {
				listener.onAccept(owner, a.handle(), a.article(), a.volume(), a.volume());
			}

			return true;
		});
	}

	public void sendLastListenerUpdate(StorageListener listener, FractionView capacity) {
		owner.forEach(a -> {
			if (!a.isEmpty()) {
				listener.onSupply(owner, a.handle(), a.article(), a.volume(), Fraction.ZERO);
			}

			return true;
		});

		listener.onCapacityChange(owner, capacity.toNegated());
	}
}
