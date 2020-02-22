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
package grondag.fluidity.base.storage;

import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.ArticleFunction;
import grondag.fluidity.api.storage.StorageEventStream;
import grondag.fluidity.api.storage.Store;

@API(status = Status.EXPERIMENTAL)
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
		return wrapped.isView();
	}

	@Override
	public Store viewOwner() {
		return wrapped.viewOwner();
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
}
