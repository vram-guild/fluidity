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
package grondag.fluidity.base.storage.bulk;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.api.storage.ArticleFunction;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.StoredBulkArticle;
import grondag.fluidity.base.article.StoredBulkArticleView;
import grondag.fluidity.base.storage.AbstractLazyRollbackStore;

@API(status = Status.EXPERIMENTAL)
public class SimpleTank extends AbstractLazyRollbackStore<StoredBulkArticle, SimpleTank> implements BulkStore {
	protected final MutableFraction quantity = new MutableFraction();
	protected final MutableFraction calc = new MutableFraction();
	protected final View view = new View();
	protected Article article = Article.NOTHING;
	protected Fraction capacity;

	public SimpleTank(Fraction capacity) {
		this.capacity = capacity;
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

	@Override
	public boolean isFull() {
		return quantity.isGreaterThanOrEqual(capacity);
	}

	@Override
	public boolean isEmpty() {
		return quantity.isZero();
	}

	@Override
	public int handleCount() {
		return 1;
	}

	@Override
	public StoredArticleView view(int handle) {
		return  handle == 0 ? view : StoredArticleView.EMPTY;
	}

	protected final Supplier supplier = new Supplier();

	protected class Supplier implements BulkArticleFunction {

		@Override
		public Fraction apply(Article item, Fraction volume, boolean simulate) {
			Preconditions.checkArgument(!volume.isNegative(), "Request to supply negative volume. (%s)", volume);

			if (item == Article.NOTHING || !item.equals(article) || quantity.isZero() || volume.isZero()) {
				return Fraction.ZERO;
			}

			calc.set(quantity.isLessThan(volume) ? quantity : volume);

			if (!simulate) {
				rollbackHandler.prepareIfNeeded();
				quantity.subtract(calc);
				dirtyNotifier.run();
				listeners.forEach(l -> l.onSupply(SimpleTank.this, 0, article, calc, quantity));

				if(quantity.isZero()) {
					article = Article.NOTHING;
				}
			}

			return calc;
		}

		@Override
		public long apply(Article item, long numerator, long divisor, boolean simulate) {
			Preconditions.checkArgument(numerator >= 0, "Request to supply negative volume. (%s)", numerator);
			Preconditions.checkArgument(divisor >= 1, "Divisor must be >= 1. (%s)", divisor);

			if (item == Article.NOTHING || !item.equals(article) || quantity.isZero() || numerator == 0) {
				return 0;
			}

			calc.set(quantity);
			calc.roundDown(divisor);

			long result = calc.toLong(divisor);

			if (result == 0) {
				return 0;
			}

			if (result > numerator) {
				result = numerator;
			}

			if (!simulate) {
				rollbackHandler.prepareIfNeeded();
				quantity.subtract(result, divisor);
				dirtyNotifier.run();

				if(!listeners.isEmpty()) {
					calc.set(result, divisor);
					listeners.forEach(l -> l.onAccept(SimpleTank.this, 0, item, calc, quantity));
				}

				if(quantity.isZero()) {
					article = Article.NOTHING;
				}
			}

			return result;
		}

		@Override
		public TransactionDelegate getTransactionDelegate() {
			return SimpleTank.this;
		}
	}

	protected final Consumer consumer = new Consumer();

	protected class Consumer implements BulkArticleFunction {

		@Override
		public Fraction apply(Article item, Fraction volume, boolean simulate) {
			Preconditions.checkArgument(!volume.isNegative(), "Request to accept negative volume. (%s)", volume);

			if (item == Article.NOTHING || volume.isZero() || (!item.equals(article) && article != Article.NOTHING)) {
				return Fraction.ZERO;
			}

			if(article.isNothing()) {
				article = item;
			}

			// compute available space
			calc.set(capacity);
			calc.subtract(quantity);

			// can't accept if full
			if (calc.isZero()) {
				return Fraction.ZERO;
			}

			// can't accept more than we got
			if (calc.isGreaterThanOrEqual(volume)) {
				calc.set(volume);
			}

			if (!simulate) {
				rollbackHandler.prepareIfNeeded();
				quantity.add(calc);
				dirtyNotifier.run();
				listeners.forEach(l -> l.onSupply(SimpleTank.this, 0, article, calc, quantity));
			}

			return calc;
		}

		@Override
		public long apply(Article item, long numerator, long divisor, boolean simulate) {
			Preconditions.checkArgument(numerator >= 0, "Request to accept negative volume. (%s)", numerator);
			Preconditions.checkArgument(divisor >= 1, "Divisor must be >= 1. (%s)", divisor);

			if (item == Article.NOTHING || numerator == 0 || (!item.equals(article) && article != Article.NOTHING)) {
				return 0;
			}

			if(article.isNothing()) {
				article = item;
			}

			// compute available space
			calc.set(capacity);
			calc.subtract(quantity);

			long result = calc.toLong(divisor);

			// can't accept if full
			if (result == 0) {
				return 0;
			}

			// can't accept more than we got
			if (result > numerator) {
				result = numerator;
			}

			if (!simulate) {
				rollbackHandler.prepareIfNeeded();
				quantity.add(result, divisor);
				dirtyNotifier.run();

				if(!listeners.isEmpty()) {
					calc.set(result, divisor);
					listeners.forEach(l -> l.onAccept(SimpleTank.this, 0, item, calc, quantity));
				}
			}

			return result;
		}

		@Override
		public TransactionDelegate getTransactionDelegate() {
			return SimpleTank.this;
		}

	}

	public void writeTag(CompoundTag tag) {
		tag.put("capacity",capacity.toTag());
		tag.put("quantity",quantity.toTag());
		tag.put("art", article.toTag());
	}

	@Override
	public CompoundTag writeTag() {
		final CompoundTag result = new CompoundTag();
		writeTag(result);
		return result;
	}

	@Override
	public void readTag(CompoundTag tag) {
		capacity = new Fraction(tag.getCompound("capacity"));
		quantity.readTag(tag.getCompound("quantity"));
		article = Article.fromTag(tag.get("art"));
	}

	protected class View implements StoredBulkArticleView {
		@Override
		public int handle() {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return quantity.isZero();
		}

		@Override
		public Fraction amount() {
			return quantity;
		}

		@Override
		public Article article() {
			return article;
		}
	}

	@Override
	protected Object createRollbackState() {
		return Pair.of(article, quantity.toImmutable());
	}

	@Override
	protected void applyRollbackState(Object state, boolean isCommitted) {
		if(!isCommitted) {
			@SuppressWarnings("unchecked")
			final Pair<Article, Fraction> pair = (Pair<Article, Fraction>) state;
			final Article bulkItem = pair.getFirst();
			final Fraction newContent = pair.getSecond();

			if(bulkItem == article) {
				if(newContent.isGreaterThan(quantity)) {
					calc.set(newContent);
					calc.subtract(quantity);
					consumer.apply(bulkItem, calc, false);
				} else if (newContent.isLessThan(quantity)) {
					calc.set(quantity);
					calc.subtract(newContent);
					supplier.apply(bulkItem, calc, false);
				}
			} else {
				supplier.apply(article, quantity, false);
				consumer.apply(bulkItem, newContent, false);
			}
		}
	}

	@Override
	protected void sendFirstListenerUpdate(StorageListener listener) {
		listener.onAccept(this, 0, article, quantity, quantity);
	}

	@Override
	protected void sendLastListenerUpdate(StorageListener listener) {
		listener.onSupply(this, 0, article, quantity, Fraction.ZERO);
	}

	@Override
	protected void onListenersEmpty() {
		// NOOP
	}

	@Override
	public void clear() {
		rollbackHandler.prepareIfNeeded();
		listeners.forEach(l -> l.onSupply(this, 0, article, quantity, Fraction.ZERO));
		quantity.set(0);
		dirtyNotifier.run();
	}

	@Override
	public Fraction amount() {
		return quantity;
	}

	@Override
	public Fraction volume() {
		return capacity;
	}
}
