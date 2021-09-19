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

import org.jetbrains.annotations.ApiStatus.Internal;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.FixedArticleFunction;
import grondag.fluidity.api.storage.FixedStore;
import grondag.fluidity.api.storage.StorageEventStream;
import net.minecraft.nbt.CompoundTag;

@Internal
public final class CreativeStore implements FixedStore {
	private CreativeStore() {}

	public static FixedStore INSTANCE = new CreativeStore();

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
		return 0;
	}

	@Override
	public Fraction volume() {
		return Fraction.ZERO;
	}

	@Override
	public double usage() {
		return 0;
	}

	@Override
	public void clear() {
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
	public TransactionDelegate getTransactionDelegate() {
		return TransactionDelegate.IGNORE;
	}

	@Override
	public boolean isFull() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public FixedArticleFunction getSupplier() {
		return FixedArticleFunction.ALWAYS_RETURN_REQUESTED;
	}

	@Override
	public StorageEventStream eventStream() {
		return StorageEventStream.IGNORE;
	}
}
