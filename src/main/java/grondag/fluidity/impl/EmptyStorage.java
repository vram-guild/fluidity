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
package grondag.fluidity.impl;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.FixedStorage;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.api.transact.TransactionContext;

@API(status = INTERNAL)
public final class EmptyStorage implements FixedStorage {
	private EmptyStorage() {}

	public static FixedStorage INSTANCE = new EmptyStorage();

	@Override
	public int handleCount() {
		return 0;
	}

	@Override
	public StoredArticleView view(int handle) {
		return StoredArticleView.EMPTY;
	}

	@Override
	public long accept(Article item, long count, boolean simulate) {
		return 0;
	}

	@Override
	public long supply(Article item, long count, boolean simulate) {
		return 0;
	}

	@Override
	public long count() {
		return 0;
	}

	@Override
	public FractionView amount() {
		return Fraction.ZERO;
	}

	@Override
	public long capacity() {
		return 0;
	}

	@Override
	public FractionView volume() {
		return Fraction.ZERO;
	}

	@Override
	public void clear() {
		// NOOP
	}

	@Override
	public FractionView accept(Article item, FractionView volume, boolean simulate) {
		return Fraction.ZERO;
	}

	@Override
	public FractionView supply(Article item, FractionView volume, boolean simulate) {
		return Fraction.ZERO;
	}

	@Override
	public long accept(Article item, long numerator, long divisor, boolean simulate) {
		return 0;
	}

	@Override
	public long supply(Article item, long numerator, long divisor, boolean simulate) {
		return 0;
	}

	@Override
	public void startListening(StorageListener listener, boolean sendNotifications) {
		// NOOP
	}

	@Override
	public void stopListening(StorageListener listener, boolean sendNotifications) {
		// NOOP
	}

	@Override
	public CompoundTag writeTag() {
		return new CompoundTag();
	}

	@Override
	public void readTag(CompoundTag tag) {
		// NOOP
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		return c -> {};
	}

	@Override
	public long accept(int handle, Article item, long count, boolean simulate) {
		return 0;
	}

	@Override
	public long supply(int handle, Article item, long count, boolean simulate) {
		return 0;
	}

	@Override
	public FractionView accept(int handle, Article item, FractionView volume, boolean simulate) {
		return Fraction.ZERO;
	}

	@Override
	public FractionView supply(int handle, Article item, FractionView volume, boolean simulate) {
		return Fraction.ZERO;
	}

	@Override
	public long accept(int handle, Article item, long numerator, long divisor, boolean simulate) {
		return 0;
	}

	@Override
	public long supply(int handle, Article item, long numerator, long divisor, boolean simulate) {
		return 0;
	}

	@Override
	public boolean isFull() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean canAccept() {
		return false;
	}

	@Override
	public boolean canSupply() {
		return false;
	}
}
