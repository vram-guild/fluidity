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

import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.FixedArticleConsumer;
import grondag.fluidity.api.storage.FixedStorage;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.api.transact.TransactionContext;

@API(status = INTERNAL)
public final class VoidStorage implements FixedStorage {
	private VoidStorage() {}

	public static FixedStorage INSTANCE = new VoidStorage();

	@Override
	public FixedArticleConsumer getConsumer() {
		return FixedArticleConsumer.VOID;
	}

	@Override
	public int handleCount() {
		return 0;
	}

	@Override
	public StoredArticleView view(int handle) {
		return StoredArticleView.EMPTY;
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
		return Long.MAX_VALUE;
	}

	@Override
	public FractionView volume() {
		return Fraction.MAX_VALUE;
	}

	@Override
	public void clear() {
		// NOOP
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
	public boolean isFull() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}
}