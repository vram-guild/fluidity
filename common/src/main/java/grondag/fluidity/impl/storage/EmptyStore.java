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

package grondag.fluidity.impl.storage;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.FixedStore;
import grondag.fluidity.api.storage.StorageEventStream;

@Internal
public final class EmptyStore implements FixedStore {
	private EmptyStore() { }

	public static FixedStore INSTANCE = new EmptyStore();

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
		return true;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}
}
