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

import grondag.fluidity.api.article.BulkArticleView;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.api.item.StorageItem;
import grondag.fluidity.api.item.StorageItemRegistry;
import grondag.fluidity.api.storage.BulkStorage;
import grondag.fluidity.api.storage.BulkStorageListener;
import grondag.fluidity.base.storage.AbstractLazyRollbackStorage;

@API(status = Status.EXPERIMENTAL)
public class SimpleTank extends AbstractLazyRollbackStorage<BulkArticleView,  BulkStorageListener> implements BulkStorage {
	protected final MutableFraction content = new MutableFraction();
	protected final MutableFraction calc = new MutableFraction();
	protected final View view = new View();
	protected StorageItem bulkItem = StorageItem.NOTHING;
	protected Fraction capacity;

	public SimpleTank(Fraction capacity) {
		this.capacity = capacity;
	}

	@Override
	public int handleCount() {
		return 1;
	}

	@Override
	public BulkArticleView view(int handle) {
		return  handle == 0 ? view : null;
	}

	@Override
	public FractionView accept(StorageItem item, FractionView volume, boolean simulate) {
		Preconditions.checkArgument(!volume.isNegative(), "Request to accept negative volume. (%s)", volume);

		if (item == StorageItem.NOTHING || volume.isZero() || (item != bulkItem && bulkItem != StorageItem.NOTHING)) {
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
			listeners.forEach(l -> l.onSupply(0, bulkItem, calc, content));
		}

		return calc;
	}

	@Override
	public FractionView supply(StorageItem item, FractionView volume, boolean simulate) {
		Preconditions.checkArgument(!volume.isNegative(), "Request to supply negative volume. (%s)", volume);

		if (item == StorageItem.NOTHING || item != bulkItem || content.isZero() || volume.isZero()) {
			return Fraction.ZERO;
		}

		calc.set(content.isLessThan(volume) ? content : volume);

		if (!simulate) {
			rollbackHandler.prepareIfNeeded();
			content.subtract(calc);
			dirtyNotifier.run();
			listeners.forEach(l -> l.onSupply(0, bulkItem, calc, content));
		}

		return calc;
	}

	@Override
	public long accept(StorageItem item, long numerator, long divisor, boolean simulate) {
		Preconditions.checkArgument(numerator >= 0, "Request to accept negative volume. (%s)", numerator);
		Preconditions.checkArgument(divisor >= 1, "Divisor must be >= 1. (%s)", divisor);

		if (item == StorageItem.NOTHING || numerator == 0 || (item != bulkItem && bulkItem != StorageItem.NOTHING)) {
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
				listeners.forEach(l -> l.onAccept(0, item, calc, content));
			}
		}

		return result;
	}

	@Override
	public long supply(StorageItem item, long numerator, long divisor, boolean simulate) {
		Preconditions.checkArgument(numerator >= 0, "Request to supply negative volume. (%s)", numerator);
		Preconditions.checkArgument(divisor >= 1, "Divisor must be >= 1. (%s)", divisor);

		if (item == StorageItem.NOTHING || item != bulkItem || content.isZero() || numerator == 0) {
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
				listeners.forEach(l -> l.onAccept(0, item, calc, content));
			}
		}

		return result;
	}

	public void writeTag(CompoundTag tag) {
		tag.put("capacity",capacity.toTag());
		tag.put("content",content.toTag());
		tag.putString("bulkItem", StorageItemRegistry.INSTANCE.getId(bulkItem).toString());
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
		bulkItem = StorageItemRegistry.INSTANCE.get(tag.getString("bulkItem"));
	}

	protected class View implements BulkArticleView {
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

		@SuppressWarnings("unchecked")
		@Override
		public <V extends StorageItem> V item() {
			return (V) bulkItem;
		}
	}

	@Override
	protected Object createRollbackState() {
		return Pair.of(bulkItem, content.toImmutable());
	}

	@Override
	protected void applyRollbackState(Object state) {
		@SuppressWarnings("unchecked")
		final Pair<StorageItem, Fraction> pair = (Pair<StorageItem, Fraction>) state;
		final StorageItem bulkItem = pair.getFirst();
		final Fraction newContent = pair.getSecond();

		if(bulkItem == this.bulkItem) {
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
			supply(this.bulkItem, content, false);
			accept(bulkItem, newContent, false);
		}
	}

	@Override
	protected void sendFirstListenerUpdate(BulkStorageListener listener) {
		listener.onAccept(0, bulkItem, content, content);
	}

	@Override
	protected void onListenersEmpty() {
		// NOOP
	}

	public void clear() {
		//TODO: implement rest of it

		dirtyNotifier.run();
	}
}
