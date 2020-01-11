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
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.StoredBulkArticle;
import grondag.fluidity.base.storage.AbstractStorage;

@API(status = Status.EXPERIMENTAL)
public class BulkTrackingNotifier extends BulkNotifier {
	protected final MutableFraction capacity = new MutableFraction();
	protected final MutableFraction amount = new MutableFraction();
	protected int articleCount = 0;
	protected BulkTrackingJournal journal = null;

	public BulkTrackingNotifier(FractionView capacity, AbstractStorage<? extends StoredBulkArticle, ?> owner) {
		super(owner);
		this.capacity.set(capacity);
	}

	@Override
	public void notifySupply(Article item, int handle, FractionView delta, FractionView newCount) {
		assert !delta.isNegative();

		if (!delta.isZero()) {
			journal(item, delta, true);
			amount.subtract(delta);
			super.notifySupply(item, handle, delta, newCount);

			if(newCount.isZero()) {
				--articleCount;
			}
		}
	}

	@Override
	public void notifyAccept(Article item, int handle, FractionView delta, FractionView newCount) {
		assert !delta.isNegative();

		if (!delta.isZero()) {
			journal(item, delta, false);
			amount.add(delta);
			super.notifyAccept(item, handle, delta, newCount);

			if(newCount.equals(delta)) {
				++articleCount;
			}
		}
	}

	public void setCapacity(FractionView newCapacity) {
		if(newCapacity != capacity) {
			notifyCapacityChange(newCapacity.withSubtraction(capacity));
		}
	}

	public void addToCapacity(FractionView delta) {
		if(!delta.isZero()) {
			notifyCapacityChange(delta);
		}
	}

	@Override
	public void notifyCapacityChange(FractionView capacityDelta) {
		if(journal != null) {
			journal.capacityDelta.add(capacityDelta);
		}

		capacity.add(capacityDelta);
		super.notifyCapacityChange(capacityDelta);
	}

	public void sendFirstListenerUpdate(StorageListener listener) {
		super.sendFirstListenerUpdate(listener, capacity);
	}

	public void sendLastListenerUpdate(StorageListener listener) {
		super.sendLastListenerUpdate(listener, capacity);
	}

	public FractionView amount() {
		return amount;
	}

	public FractionView volume() {
		return capacity;
	}

	public int articleCount() {
		return articleCount;
	}

	public void clear() {
		amount.set(0);
		articleCount = 0;
	}

	protected void journal(Article article, FractionView delta, boolean subtract) {
		if(journal != null) {
			final MutableFraction current = journal.changes.get(article);

			if(current == null) {
				final MutableFraction f = new MutableFraction(delta);

				if(subtract) {
					f.negate();
				}

				journal.changes.put(article, f);
			} else {
				if(subtract) {
					current.subtract(delta);
				} else {
					current.add(delta);
				}
			}
		}
	}

	public BulkTrackingJournal beginNewJournalAndReturnPrior() {
		final BulkTrackingJournal result = journal;
		journal = BulkTrackingJournal.claim();
		return result;
	}

	public void restoreJournal(BulkTrackingJournal journal) {
		final BulkTrackingJournal old = this.journal;

		if(old != null ) {
			BulkTrackingJournal.release(old);
		}

		this.journal = journal;
	}

	public BulkTrackingJournal journal() {
		return journal;
	}
}