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
package grondag.fluidity.base.storage;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.device.StorageDevice;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.api.transact.TransactionContext;

@API(status = Status.EXPERIMENTAL)
public class ForwardingStorage implements Storage {
	protected Storage wrapped = Storage.EMPTY;

	public Storage getWrapped() {
		return wrapped;
	}

	public void setWrapped(Storage wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		return wrapped.prepareRollback(context);
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
	public long accept(Article item, long count, boolean simulate) {
		return wrapped.accept(item, count, simulate);
	}

	@Override
	public long supply(Article item, long count, boolean simulate) {
		return wrapped.supply(item, count, simulate);
	}

	@Override
	public long count() {
		return wrapped.count();
	}

	@Override
	public FractionView amount() {
		return wrapped.amount();
	}

	@Override
	public long capacity() {
		return wrapped.capacity();
	}

	@Override
	public FractionView volume() {
		return wrapped.volume();
	}

	@Override
	public void clear() {
		wrapped.clear();
	}

	@Override
	public FractionView accept(Article item, FractionView volume, boolean simulate) {
		return wrapped.accept(item, volume, simulate);
	}

	@Override
	public FractionView supply(Article item, FractionView volume, boolean simulate) {
		return wrapped.supply(item, volume, simulate);
	}

	@Override
	public long accept(Article item, long numerator, long divisor, boolean simulate) {
		return wrapped.accept(item, numerator, divisor, simulate);
	}

	@Override
	public long supply(Article item, long numerator, long divisor, boolean simulate) {
		return wrapped.supply(item, numerator, divisor, simulate);
	}

	@Override
	public void startListening(StorageListener listener, boolean sendNotifications) {
		wrapped.startListening(listener, sendNotifications);
	}

	@Override
	public void stopListening(StorageListener listener, boolean sendNotifications) {
		wrapped.stopListening(listener, sendNotifications);
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
	public Storage viewOwner() {
		return wrapped.viewOwner();
	}

	@Override
	public boolean isAggregate() {
		return wrapped.isAggregate();
	}

	@Override
	public StorageDevice device() {
		return wrapped.device();
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
	public FractionView amountOf(Article item) {
		return wrapped.amountOf(item);
	}

	@Override
	public long accept(Item item, CompoundTag tag, long count, boolean simulate) {
		return wrapped.accept(item, tag, count, simulate);
	}

	@Override
	public long accept(Item item, long count, boolean simulate) {
		return wrapped.accept(item, count, simulate);
	}

	@Override
	public long accept(ItemStack stack, long count, boolean simulate) {
		return wrapped.accept(stack, count, simulate);
	}

	@Override
	public long accept(ItemStack stack, boolean simulate) {
		return wrapped.accept(stack, simulate);
	}

	@Override
	public long supply(Item item, CompoundTag tag, long count, boolean simulate) {
		return wrapped.supply(item, tag, count, simulate);
	}

	@Override
	public long supply(Item item, long count, boolean simulate) {
		return wrapped.supply(item, count, simulate);
	}

	@Override
	public long supply(ItemStack stack, long count, boolean simulate) {
		return wrapped.supply(stack, count, simulate);
	}

	@Override
	public long supply(ItemStack stack, boolean simulate) {
		return wrapped.supply(stack, simulate);
	}
}
