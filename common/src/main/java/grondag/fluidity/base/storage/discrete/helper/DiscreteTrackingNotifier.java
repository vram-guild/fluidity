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
public class DiscreteTrackingNotifier extends DiscreteNotifier {
	protected long capacity;
	protected long count;
	protected int articleCount = 0;
	protected DiscreteTrackingJournal journal = null;

	public DiscreteTrackingNotifier(long capacity, AbstractStore<? extends StoredDiscreteArticle, ?> owner) {
		super(owner);
		this.capacity = capacity;
	}

	@Override
	public void notifySupply(Article item, int handle, long delta, long newCount) {
		if (delta > 0) {
			journal(item, -delta);
			count -= delta;
			super.notifySupply(item, handle, delta, newCount);

			if (newCount == 0) {
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

			if (newCount == delta) {
				++articleCount;
			}
		}
	}

	public void setCapacity(long newCapacity) {
		if (newCapacity != capacity) {
			notifyCapacityChange(newCapacity - capacity);
		}
	}

	public void addToCapacity(long delta) {
		if (delta != 0) {
			notifyCapacityChange(delta);
		}
	}

	@Override
	public void notifyCapacityChange(long capacityDelta) {
		if (journal != null) {
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
		if (journal != null) {
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

		if (old != null) {
			DiscreteTrackingJournal.release(old);
		}

		this.journal = journal;
	}

	public DiscreteTrackingJournal journal() {
		return journal;
	}
}
