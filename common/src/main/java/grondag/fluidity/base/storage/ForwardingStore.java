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

package grondag.fluidity.base.storage;

import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.ArticleFunction;
import grondag.fluidity.api.storage.StorageEventStream;
import grondag.fluidity.api.storage.Store;
import grondag.fluidity.api.util.AmbiguousBoolean;

@Experimental
public class ForwardingStore implements Store {
	protected Store wrapped = Store.EMPTY;

	public Store getWrapped() {
		return wrapped;
	}

	public void setWrapped(Store wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public ArticleFunction getConsumer() {
		return wrapped.getConsumer();
	}

	@Override
	public boolean hasConsumer() {
		return wrapped.hasConsumer();
	}

	@Override
	public ArticleFunction getSupplier() {
		return wrapped.getSupplier();
	}

	@Override
	public boolean hasSupplier() {
		return wrapped.hasSupplier();
	}

	@Override
	public int handleCount() {
		return wrapped.handleCount();
	}

	@Override
	public StoredArticleView view(int handle) {
		return wrapped.view(handle);
	}

	@Override
	public long count() {
		return wrapped.count();
	}

	@Override
	public Fraction amount() {
		return wrapped.amount();
	}

	@Override
	public long capacity() {
		return wrapped.capacity();
	}

	@Override
	public Fraction volume() {
		return wrapped.volume();
	}

	@Override
	public double usage() {
		return wrapped.usage();
	}

	@Override
	public void clear() {
		wrapped.clear();
	}

	@Override
	public StorageEventStream eventStream() {
		return wrapped.eventStream();
	}

	@Override
	public CompoundTag writeTag() {
		return wrapped.writeTag();
	}

	@Override
	public void readTag(CompoundTag tag) {
		wrapped.readTag(tag);
	}

	@Override
	public boolean isEmpty() {
		return wrapped.isEmpty();
	}

	@Override
	public boolean isHandleValid(int handle) {
		return wrapped.isHandleValid(handle);
	}

	@Override
	public boolean isView() {
		return true;
	}

	@Override
	public Store viewOwner() {
		return wrapped;
	}

	@Override
	public boolean isAggregate() {
		return wrapped.isAggregate();
	}

	@Override
	public void forEach(Predicate<? super StoredArticleView> filter, Predicate<? super StoredArticleView> action) {
		wrapped.forEach(filter, action);
	}

	@Override
	public void forEach(Predicate<? super StoredArticleView> action) {
		wrapped.forEach(action);
	}

	@Override
	public long countOf(Article item) {
		return wrapped.countOf(item);
	}

	@Override
	public Fraction amountOf(Article item) {
		return wrapped.amountOf(item);
	}

	@Override
	public boolean isFull() {
		return wrapped.isFull();
	}

	@Override
	public TransactionDelegate getTransactionDelegate() {
		return wrapped.getTransactionDelegate();
	}

	@Override
	public boolean isSelfEnlisting() {
		return wrapped.isSelfEnlisting();
	}

	@Override
	public boolean hasEventStream() {
		return wrapped.hasEventStream();
	}

	@Override
	public AmbiguousBoolean allowsType(ArticleType<?> type) {
		return wrapped.allowsType(type);
	}
}
