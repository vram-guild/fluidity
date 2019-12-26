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
public class DiscreteTrackingNotifier extends DiscreteNotifier{
	protected long capacity;
	protected long count;
	protected int articleCount = 0;

	public DiscreteTrackingNotifier(long capacity, AbstractStorage<DiscreteStoredArticle, ?> owner) {
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

	public void sendFirstListenerUpdate(StorageListener listener) {
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
