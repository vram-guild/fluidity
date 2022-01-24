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

package grondag.fluidity.base.storage.bulk;

import java.util.Set;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.api.storage.ArticleFunction;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.api.storage.Store;
import grondag.fluidity.api.transact.Transaction;
import grondag.fluidity.base.article.AggregateBulkStoredArticle;
import grondag.fluidity.base.storage.AbstractAggregateStore;
import grondag.fluidity.base.storage.bulk.helper.BulkTrackingNotifier;
import grondag.fluidity.impl.Fluidity;

// NB: Previous versions attempted to consolidate member notifications
// but this can lead to de-sync and other problems with creative bins
// or other members that don't behave in a conventional manner.
// A future version may consolidate notifications for downstream listeners
// (for performance) but will have to do so based on actual member notifications.

@Experimental
public class AggregateBulkStore extends AbstractAggregateStore<AggregateBulkStoredArticle, AggregateBulkStore> implements BulkStore, BulkStorageListener {
	protected final BulkTrackingNotifier notifier;

	protected final MutableFraction requested = new MutableFraction();
	protected final MutableFraction delta = new MutableFraction();
	protected final MutableFraction result = new MutableFraction();

	public AggregateBulkStore(int startingSlotCount) {
		super(startingSlotCount);
		notifier = new BulkTrackingNotifier(Fraction.ZERO, this);
	}

	public AggregateBulkStore() {
		this(32);
	}

	@Override
	public ArticleFunction getConsumer() {
		return consumer;
	}

	@Override
	public boolean hasConsumer() {
		return true;
	}

	@Override
	public ArticleFunction getSupplier() {
		return supplier;
	}

	@Override
	public boolean hasSupplier() {
		return true;
	}

	protected final ObjectArrayList<Store> searchList = new ObjectArrayList<>();

	protected final Consumer consumer = new Consumer();

	protected class Consumer implements BulkArticleFunction {
		@Override
		public TransactionDelegate getTransactionDelegate() {
			return AggregateBulkStore.this;
		}

		@Override
		public Fraction apply(Article item, Fraction volume, boolean simulate) {
			if (item.isNothing() || stores.isEmpty()) {
				return Fraction.ZERO;
			}

			if (simulate) {
				return acceptInner(item, volume, true).toImmutable();
			} else {
				try (Transaction tx = Transaction.open()) {
					final Fraction result = acceptInner(item, volume, false).toImmutable();
					tx.commit();
					return result;
				}
			}
		}

		@Override
		public long apply(Article item, long numerator, long divisor, boolean simulate) {
			Preconditions.checkArgument(numerator >= 0, "Request to accept negative amounts. (%s)", numerator);
			Preconditions.checkArgument(divisor >= 1, "Request to accept divisor < 1. (%s)", divisor);
			Preconditions.checkNotNull(item, "Request to accept null article");

			if (item.isNothing() || stores.isEmpty()) {
				return 0;
			}

			if (simulate) {
				return acceptInner(item, numerator, divisor, true);
			} else {
				try (Transaction tx = Transaction.open()) {
					final long result = acceptInner(item, numerator, divisor, false);
					tx.commit();
					return result;
				}
			}
		}

		@Override
		public Article suggestArticle(ArticleType<?> type) {
			return type == null ? getAnyArticle().article() : getAnyMatch(type.viewPredicate()).article();
		}
	}

	protected final Supplier supplier = new Supplier();

	protected class Supplier implements BulkArticleFunction {
		@Override
		public TransactionDelegate getTransactionDelegate() {
			return AggregateBulkStore.this;
		}

		@Override
		public Fraction apply(Article item, Fraction volume, boolean simulate) {
			Preconditions.checkNotNull(item, "Request to accept null article");

			if (item.isNothing() || isEmpty()) {
				return Fraction.ZERO;
			}

			if (simulate) {
				return supplyInner(item, volume, true).toImmutable();
			} else {
				try (Transaction tx = Transaction.open()) {
					final Fraction result = supplyInner(item, volume, false).toImmutable();
					tx.commit();
					return result;
				}
			}
		}

		@Override
		public long apply(Article item, long numerator, long divisor, boolean simulate) {
			Preconditions.checkArgument(numerator >= 0, "Request to accept negative amounts. (%s)", numerator);
			Preconditions.checkArgument(divisor >= 1, "Request to accept divisor < 1. (%s)", divisor);
			Preconditions.checkNotNull(item, "Request to accept null article");

			if (item.isNothing() || isEmpty()) {
				return 0;
			}

			if (simulate) {
				return supplyInner(item, numerator, divisor, true);
			} else {
				try (Transaction tx = Transaction.open()) {
					final long result = supplyInner(item, numerator, divisor, false);
					tx.commit();
					return result;
				}
			}
		}

		@Override
		public Article suggestArticle(ArticleType<?> type) {
			return type == null ? getAnyArticle().article() : getAnyMatch(type.viewPredicate()).article();
		}
	}

	protected Fraction acceptInner(Article item, Fraction volume, boolean simulate) {
		result.set(0);

		final AggregateBulkStoredArticle article = articles.findOrCreateArticle(item);

		// Try stores that already have article first
		final Set<Store> existing = article.stores();

		if (!existing.isEmpty()) {
			// save non-existing stores here in case existing have insufficient capacity
			searchList.clear();

			for (final Store store : stores) {
				if (store.hasConsumer() && !store.isFull()) {
					if (existing.contains(store)) {
						result.add(store.getConsumer().apply(item, delta.set(volume).subtract(result), simulate));

						if (result.equals(volume)) {
							break;
						}
					} else {
						searchList.add(store);
					}
				}
			}

			if (result.isLessThan(volume)) {
				for (final Store store : searchList) {
					final Fraction f = store.getConsumer().apply(item, delta.set(volume).subtract(result), simulate);

					if (!f.isZero()) {
						result.add(f);

						// add new stores to per-article tracking
						if (!simulate) {
							existing.add(store);
						}

						if (result.equals(volume)) {
							break;
						}
					}
				}
			}
		} else {
			for (final Store store : stores) {
				if (store.hasConsumer() && !store.isFull()) {
					final Fraction f = store.getConsumer().apply(item, delta.set(volume).subtract(result), simulate);

					if (!f.isZero()) {
						result.add(f);

						// add new stores to per-article tracking
						if (!simulate) {
							existing.add(store);
						}

						if (result.equals(volume)) {
							break;
						}
					}
				}
			}
		}

		return result;
	}

	protected long acceptInner(Article item, long numerator, long denominator, boolean simulate) {
		long result = 0;

		final AggregateBulkStoredArticle article = articles.findOrCreateArticle(item);

		// Try stores that already have article first
		final Set<Store> existing = article.stores();

		if (!existing.isEmpty()) {
			// save non-existing stores here in case existing have insufficient capacity
			searchList.clear();

			for (final Store store : stores) {
				if (store.hasConsumer() && !store.isFull()) {
					if (existing.contains(store)) {
						result += store.getConsumer().apply(item, numerator - result, denominator, simulate);

						if (result == numerator) {
							break;
						}
					} else {
						searchList.add(store);
					}
				}
			}

			if (result < numerator) {
				for (final Store store : searchList) {
					final long delta = store.getConsumer().apply(item, numerator - result, denominator, simulate);

					if (delta != 0) {
						result += delta;

						// add new stores to per-article tracking
						if (!simulate) {
							existing.add(store);
						}

						if (result == numerator) {
							break;
						}
					}
				}
			}
		} else {
			for (final Store store : stores) {
				if (store.hasConsumer() && !store.isFull()) {
					final long delta = store.getConsumer().apply(item, numerator - result, denominator, simulate);

					if (delta != 0) {
						result += delta;

						// add new stores to per-article tracking
						if (!simulate) {
							existing.add(store);
						}

						if (result == numerator) {
							break;
						}
					}
				}
			}
		}

		return result;
	}

	protected Fraction supplyInner(Article item, Fraction volume, boolean simulate) {
		final AggregateBulkStoredArticle article = articles.get(item);

		if (article == null || article.isEmpty()) {
			return Fraction.ZERO;
		}

		result.set(0);

		final Set<Store> existing = article.stores();

		for (final Store store : existing) {
			if (store.hasSupplier()) {
				final Fraction f = store.getSupplier().apply(item, delta.set(volume).subtract(result), simulate);

				if (!f.isZero()) {
					result.add(f);

					// remove from per-article tracking if store no longer contains
					if (!simulate && store.amountOf(item).isZero()) {
						existing.remove(store);
					}

					if (result.equals(volume)) {
						break;
					}
				}
			}
		}

		return result;
	}

	protected long supplyInner(Article item, long numerator, long denominator, boolean simulate) {
		final AggregateBulkStoredArticle article = articles.get(item);

		if (article == null || article.isEmpty()) {
			return 0;
		}

		long result = 0;

		final Set<Store> existing = article.stores();

		for (final Store store : existing) {
			if (store.hasSupplier()) {
				final long delta = store.getSupplier().apply(item, numerator - result, denominator, simulate);

				if (delta != 0) {
					result += delta;

					// remove from per-article tracking if store no longer contains
					if (!simulate && store.amountOf(item).isZero()) {
						existing.remove(store);
					}

					if (result == numerator) {
						break;
					}
				}
			}
		}

		return result;
	}

	@Override
	protected AggregateBulkStoredArticle newArticle() {
		return new AggregateBulkStoredArticle();
	}

	@Override
	public StoredArticleView view(int slot) {
		return ObjectUtils.defaultIfNull(articles.get(slot), StoredArticleView.EMPTY);
	}

	@Override
	protected StorageListener listener() {
		return this;
	}

	@Override
	public Fraction amount() {
		return notifier.amount();
	}

	@Override
	public Fraction volume() {
		return notifier.volume();
	}

	@Override
	public boolean isFull() {
		return notifier.amount().isGreaterThanOrEqual(notifier.volume());
	}

	@Override
	public boolean isEmpty() {
		return notifier.amount().isZero();
	}

	@Override
	public void onAccept(Store storage, int slot, Article item, Fraction delta, Fraction newVolume) {
		final AggregateBulkStoredArticle article = articles.findOrCreateArticle(item);
		article.add(delta);
		article.stores().add(storage);
		notifier.notifyAccept(article.article(), article.handle(), delta, article.amount());
	}

	static boolean warnIgnore = true;
	static boolean warnPartialIgnore = true;

	@Override
	public void onSupply(Store storage, int slot, Article item, Fraction delta, Fraction newVolume) {
		final AggregateBulkStoredArticle article = articles.get(item);

		if (article == null) {
			if (warnIgnore) {
				Fluidity.LOG.warn("AggregateStorage ignored notification of supply for non-tracked article.");
				Fluidity.LOG.warn("This probably indicates a bug in a mod using Fludity. Warnings for subsequent events are suppressed.");
				warnIgnore = false;
			}

			return;
		}

		if (delta.isGreaterThan(article.amount())) {
			if (warnPartialIgnore) {
				Fluidity.LOG.warn("AggregateStorage partially ignored notification of supply for article with mimatched amount.");
				Fluidity.LOG.warn("This probably indicates a bug in a mod using Fludity. Warnings for subsequent events are suppressed.");
				warnPartialIgnore = false;
			}

			delta = article.amount().toImmutable();
		}

		if (newVolume.isZero()) {
			article.stores().remove(storage);
		}

		article.subtract(delta);
		notifier.notifySupply(article.article(), article.handle(), delta, article.amount());
	}

	@Override
	public void onCapacityChange(Store storage, Fraction capacityDelta) {
		notifier.addToCapacity(capacityDelta);
	}

	/** Removes all stores, not the underlying storages. */
	@Override
	public void clear() {
		if (stores.isEmpty()) {
			return;
		}

		for (final Store store : stores.toArray(new Store[stores.size()])) {
			removeStore(store);
		}
	}

	@Override
	protected void sendFirstListenerUpdate(StorageListener listener) {
		notifier.sendFirstListenerUpdate(listener);
	}

	@Override
	protected void sendLastListenerUpdate(StorageListener listener) {
		notifier.sendLastListenerUpdate(listener);
	}

	@Override
	public CompoundTag writeTag() {
		throw new UnsupportedOperationException("Aggregate storage view do not support saving");
	}

	@Override
	public void readTag(CompoundTag tag) {
		throw new UnsupportedOperationException("Aggregate storage view do not support saving");
	}

	@Override
	protected void onListenersEmpty() {
		articles.compact();
	}
}
