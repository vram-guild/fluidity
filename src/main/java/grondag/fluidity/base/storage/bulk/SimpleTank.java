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
package grondag.fluidity.base.storage.bulk;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.api.storage.ArticleConsumer;
import grondag.fluidity.api.storage.ArticleSupplier;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.StoredBulkArticle;
import grondag.fluidity.base.article.StoredBulkArticleView;
import grondag.fluidity.base.storage.AbstractLazyRollbackStorage;
import grondag.fluidity.base.storage.bulk.BulkStorage.BulkArticleConsumer;
import grondag.fluidity.base.storage.bulk.BulkStorage.BulkArticleSupplier;

@API(status = Status.EXPERIMENTAL)
public class SimpleTank extends AbstractLazyRollbackStorage<StoredBulkArticle, SimpleTank> implements BulkStorage, BulkArticleConsumer, BulkArticleSupplier {
	protected final MutableFraction content = new MutableFraction();
	protected final MutableFraction calc = new MutableFraction();
	protected final View view = new View();
	protected Article article = Article.NOTHING;
	protected Fraction capacity;

	public SimpleTank(Fraction capacity) {
		this.capacity = capacity;
	}

	@Override
	public ArticleConsumer getConsumer() {
		return this;
	}

	@Override
	public boolean hasConsumer() {
		return true;
	}

	@Override
	public ArticleSupplier getSupplier() {
		return this;
	}

	@Override
	public boolean hasSupplier() {
		return true;
	}

	@Override
	public boolean isFull() {
		return content.isGreaterThankOrEqual(capacity);
	}

	@Override
	public boolean isEmpty() {
		return content.isZero();
	}

	@Override
	public int handleCount() {
		return 1;
	}

	@Override
	public StoredArticleView view(int handle) {
		return  handle == 0 ? view : StoredArticleView.EMPTY;
	}

	@Override
	public FractionView accept(Article item, FractionView volume, boolean simulate) {
		Preconditions.checkArgument(!volume.isNegative(), "Request to accept negative volume. (%s)", volume);

		if (item == Article.NOTHING || volume.isZero() || (item != article && article != Article.NOTHING)) {
			return Fraction.ZERO;
		}

		// compute available space
		calc.set(capacity);
		calc.subtract(content);

		// can't accept if full
		if (calc.isZero()) {
			return Fraction.ZERO;
		}

		// can't accept more than we got
		if (calc.isGreaterThankOrEqual(volume)) {
			calc.set(volume);
		}

		if (!simulate) {
			rollbackHandler.prepareIfNeeded();
			content.add(calc);
			dirtyNotifier.run();
			listeners.forEach(l -> l.onSupply(this, 0, article, calc, content));
		}

		return calc;
	}

	@Override
	public FractionView supply(Article item, FractionView volume, boolean simulate) {
		Preconditions.checkArgument(!volume.isNegative(), "Request to supply negative volume. (%s)", volume);

		if (item == Article.NOTHING || item != article || content.isZero() || volume.isZero()) {
			return Fraction.ZERO;
		}

		calc.set(content.isLessThan(volume) ? content : volume);

		if (!simulate) {
			rollbackHandler.prepareIfNeeded();
			content.subtract(calc);
			dirtyNotifier.run();
			listeners.forEach(l -> l.onSupply(this, 0, article, calc, content));
		}

		return calc;
	}

	@Override
	public long accept(Article item, long numerator, long divisor, boolean simulate) {
		Preconditions.checkArgument(numerator >= 0, "Request to accept negative volume. (%s)", numerator);
		Preconditions.checkArgument(divisor >= 1, "Divisor must be >= 1. (%s)", divisor);

		if (item == Article.NOTHING || numerator == 0 || (item != article && article != Article.NOTHING)) {
			return 0;
		}

		// compute available space
		calc.set(capacity);
		calc.subtract(content);

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
			content.add(result, divisor);
			dirtyNotifier.run();

			if(!listeners.isEmpty()) {
				calc.set(result, divisor);
				listeners.forEach(l -> l.onAccept(this, 0, item, calc, content));
			}
		}

		return result;
	}

	@Override
	public long supply(Article item, long numerator, long divisor, boolean simulate) {
		Preconditions.checkArgument(numerator >= 0, "Request to supply negative volume. (%s)", numerator);
		Preconditions.checkArgument(divisor >= 1, "Divisor must be >= 1. (%s)", divisor);

		if (item == Article.NOTHING || item != article || content.isZero() || numerator == 0) {
			return 0;
		}

		calc.set(content);
		calc.floor(divisor);

		long result = calc.toLong(divisor);

		if (result == 0) {
			return 0;
		}

		if (result > numerator) {
			result = numerator;
		}

		if (!simulate) {
			rollbackHandler.prepareIfNeeded();
			content.subtract(result, divisor);
			dirtyNotifier.run();

			if(!listeners.isEmpty()) {
				calc.set(result, divisor);
				listeners.forEach(l -> l.onAccept(this, 0, item, calc, content));
			}
		}

		return result;
	}

	public void writeTag(CompoundTag tag) {
		tag.put("capacity",capacity.toTag());
		tag.put("content",content.toTag());
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
		content.readTag(tag.getCompound("content"));
		article = Article.fromTag(tag.get("art"));
	}

	protected class View implements StoredBulkArticleView {
		@Override
		public int handle() {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return content.isZero();
		}

		@Override
		public FractionView volume() {
			return content;
		}

		@Override
		public Article article() {
			return article;
		}
	}

	@Override
	protected Object createRollbackState() {
		return Pair.of(article, content.toImmutable());
	}

	@Override
	protected void applyRollbackState(Object state, boolean isCommitted) {
		if(!isCommitted) {
			@SuppressWarnings("unchecked")
			final Pair<Article, Fraction> pair = (Pair<Article, Fraction>) state;
			final Article bulkItem = pair.getFirst();
			final Fraction newContent = pair.getSecond();

			if(bulkItem == article) {
				if(newContent.isGreaterThan(content)) {
					calc.set(newContent);
					calc.subtract(content);
					accept(bulkItem, calc, false);
				} else if (newContent.isLessThan(content)) {
					calc.set(content);
					calc.subtract(newContent);
					supply(bulkItem, calc, false);
				}
			} else {
				supply(article, content, false);
				accept(bulkItem, newContent, false);
			}
		}
	}

	@Override
	protected void sendFirstListenerUpdate(StorageListener listener) {
		listener.onAccept(this, 0, article, content, content);
	}

	@Override
	protected void sendLastListenerUpdate(StorageListener listener) {
		listener.onSupply(this, 0, article, content, Fraction.ZERO);
	}

	@Override
	protected void onListenersEmpty() {
		// NOOP
	}

	@Override
	public void clear() {
		listeners.forEach(l -> l.onSupply(this, 0, article, content, Fraction.ZERO));
		content.set(0);
		dirtyNotifier.run();
	}

	@Override
	public FractionView amount() {
		return content;
	}

	@Override
	public FractionView volume() {
		return capacity;
	}
}
