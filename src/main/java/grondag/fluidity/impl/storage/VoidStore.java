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
package grondag.fluidity.impl.storage;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.FixedArticleFunction;
import grondag.fluidity.api.storage.FixedStore;
import grondag.fluidity.api.storage.StorageEventStream;

@API(status = INTERNAL)
public final class VoidStore implements FixedStore {
	private VoidStore() {}

	public static FixedStore INSTANCE = new VoidStore();

	@Override
	public FixedArticleFunction getConsumer() {
		return FixedArticleFunction.VOID;
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
	public Fraction amount() {
		return Fraction.ZERO;
	}

	@Override
	public long capacity() {
		return Long.MAX_VALUE;
	}

	@Override
	public Fraction volume() {
		return Fraction.MAX_VALUE;
	}

	@Override
	public void clear() {
		// NOOP
	}

	@Override
	public StorageEventStream eventStream() {
		return StorageEventStream.IGNORE;
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
	public TransactionDelegate getTransactionDelegate() {
		return TransactionDelegate.IGNORE;
	}

	@Override
	public boolean isFull() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public double usage() {
		return 0;
	}
}
