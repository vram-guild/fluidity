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
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.StoredBulkArticle;
import grondag.fluidity.base.storage.AbstractStore;

@Experimental
public class BulkTrackingNotifier extends BulkNotifier {
	protected final MutableFraction capacity = new MutableFraction();
	protected final MutableFraction amount = new MutableFraction();
	protected BulkTrackingJournal journal = null;

	public BulkTrackingNotifier(Fraction capacity, AbstractStore<? extends StoredBulkArticle, ?> owner) {
		super(owner);
		this.capacity.set(capacity);
	}

	@Override
	public void notifySupply(Article item, int handle, Fraction delta, Fraction newCount) {
		assert !delta.isNegative();

		if (!delta.isZero()) {
			journal(item, delta, true);
			amount.subtract(delta);
			super.notifySupply(item, handle, delta, newCount);
		}
	}

	@Override
	public void notifyAccept(Article item, int handle, Fraction delta, Fraction newCount) {
		assert !delta.isNegative();

		if (!delta.isZero()) {
			journal(item, delta, false);
			amount.add(delta);
			super.notifyAccept(item, handle, delta, newCount);
		}
	}

	public void setCapacity(Fraction newCapacity) {
		if (newCapacity != capacity) {
			notifyCapacityChange(newCapacity.withSubtraction(capacity));
		}
	}

	public void addToCapacity(Fraction delta) {
		if (!delta.isZero()) {
			notifyCapacityChange(delta);
		}
	}

	@Override
	public void notifyCapacityChange(Fraction capacityDelta) {
		if (journal != null) {
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

	public Fraction amount() {
		return amount;
	}

	public Fraction volume() {
		return capacity;
	}

	public void clear() {
		amount.set(0);
	}

	protected void journal(Article article, Fraction delta, boolean subtract) {
		if (journal != null) {
			final MutableFraction current = journal.changes.get(article);

			if (current == null) {
				final MutableFraction f = new MutableFraction(delta);

				if (subtract) {
					f.negate();
				}

				journal.changes.put(article, f);
			} else {
				if (subtract) {
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

		if (old != null) {
			BulkTrackingJournal.release(old);
		}

		this.journal = journal;
	}

	public BulkTrackingJournal journal() {
		return journal;
	}
}
