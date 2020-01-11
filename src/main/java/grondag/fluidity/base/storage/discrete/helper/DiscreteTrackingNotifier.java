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
package grondag.fluidity.base.storage.discrete.helper;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.StoredDiscreteArticle;
import grondag.fluidity.base.storage.AbstractStorage;

@API(status = Status.EXPERIMENTAL)
public class DiscreteTrackingNotifier extends DiscreteNotifier {
	protected long capacity;
	protected long count;
	protected int articleCount = 0;
	protected DiscreteTrackingJournal journal = null;

	public DiscreteTrackingNotifier(long capacity, AbstractStorage<? extends StoredDiscreteArticle, ?> owner) {
		super(owner);
		this.capacity = capacity;
	}

	@Override
	public void notifySupply(Article item, int handle, long delta, long newCount) {
		if (delta > 0) {
			journal(item, -delta);
			count -= delta;
			super.notifySupply(item, handle, delta, newCount);

			if(newCount == 0) {
				--articleCount;
			}
		}
	}

	@Override
	public void notifyAccept(Article item, int handle, long delta, long newCount) {
		if (delta > 0) {
			journal(item, delta);
			count += delta;
			super.notifyAccept(item, handle, delta, newCount);

			if(newCount == delta) {
				++articleCount;
			}
		}
	}

	public void setCapacity(long newCapacity) {
		if(newCapacity != capacity) {
			notifyCapacityChange(newCapacity - capacity);
		}
	}

	public void addToCapacity(long delta) {
		if(delta != 0) {
			notifyCapacityChange(delta);
		}
	}

	@Override
	public void notifyCapacityChange(long capacityDelta) {
		if(journal != null) {
			journal.capacityDelta += capacityDelta;
		}

		capacity += capacityDelta;
		super.notifyCapacityChange(capacityDelta);
	}

	public void sendFirstListenerUpdate(StorageListener listener) {
		super.sendFirstListenerUpdate(listener, capacity);
	}

	public void sendLastListenerUpdate(StorageListener listener) {
		super.sendLastListenerUpdate(listener, capacity);
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

	public void clear() {
		count = 0;
		articleCount = 0;
	}

	protected void journal(Article article, long delta) {
		if(journal != null) {
			journal.changes.addTo(article, delta);
		}
	}

	public DiscreteTrackingJournal beginNewJournalAndReturnPrior() {
		final DiscreteTrackingJournal result = journal;
		journal = DiscreteTrackingJournal.claim();
		return result;
	}

	public void restoreJournal(DiscreteTrackingJournal journal) {
		final DiscreteTrackingJournal old = this.journal;

		if(old != null ) {
			DiscreteTrackingJournal.release(old);
		}

		this.journal = journal;
	}

	public DiscreteTrackingJournal journal() {
		return journal;
	}
}
