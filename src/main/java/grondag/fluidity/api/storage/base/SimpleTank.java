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
package grondag.fluidity.api.storage.base;

import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import grondag.fluidity.api.article.ArticleView;
import grondag.fluidity.api.article.BulkArticleView;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.api.item.BulkItem;
import grondag.fluidity.api.item.BulkItemRegistry;
import grondag.fluidity.api.storage.BulkStorage;
import grondag.fluidity.api.transact.TransactionContext;

@API(status = Status.EXPERIMENTAL)
public class SimpleTank extends AbstractStorage implements BulkStorage {
	protected final MutableFraction content = new MutableFraction();
	protected final MutableFraction calc = new MutableFraction();
	protected final View view = new View();
	protected final Consumer<TransactionContext> rollbackHandler = this::handleRollback;

	protected BulkItem bulkItem = BulkItem.NOTHING;
	protected Fraction capacity;

	public SimpleTank(Fraction capacity) {
		this.capacity = capacity;
	}

	@Override
	public int slotCount() {
		return 1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ArticleView> T view(int slot) {
		return  slot == 0 ? (T) view : null;
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		context.setState(Pair.of(bulkItem, content.toImmutable()));
		return rollbackHandler;
	}

	@Override
	protected void handleRollback(TransactionContext context) {
		if (!context.isCommited()) {
			final Pair<BulkItem, Fraction> state = context.getState();
			bulkItem = state.getFirst();
			content.set(state.getSecond());
			notifyListeners(0);
		}
	}

	@Override
	public FractionView accept(BulkItem item, FractionView volume, boolean simulate) {
		Preconditions.checkArgument(!volume.isNegative(), "Request to accept negative volume. (%s)", volume);

		if (item == BulkItem.NOTHING || volume.isZero() || (item != bulkItem && bulkItem != BulkItem.NOTHING)) {
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
			content.add(calc);
			notifyListeners(0);
		}

		return calc;
	}

	@Override
	public FractionView supply(BulkItem item, FractionView volume, boolean simulate) {
		Preconditions.checkArgument(!volume.isNegative(), "Request to supply negative volume. (%s)", volume);

		if (item == BulkItem.NOTHING || item != bulkItem || content.isZero() || volume.isZero()) {
			return Fraction.ZERO;
		}

		calc.set(content.isLessThan(volume) ? content : volume);

		if (!simulate) {
			content.subtract(calc);
			notifyListeners(0);
		}

		return calc;
	}

	@Override
	public long accept(BulkItem item, long numerator, long divisor, boolean simulate) {
		Preconditions.checkArgument(numerator >= 0, "Request to accept negative volume. (%s)", numerator);
		Preconditions.checkArgument(divisor >= 1, "Divisor must be >= 1. (%s)", divisor);

		if (item == BulkItem.NOTHING || numerator == 0 || (item != bulkItem && bulkItem != BulkItem.NOTHING)) {
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
			content.add(result, divisor);
			notifyListeners(0);
		}

		return result;
	}

	@Override
	public long supply(BulkItem item, long numerator, long divisor, boolean simulate) {
		Preconditions.checkArgument(numerator >= 0, "Request to supply negative volume. (%s)", numerator);
		Preconditions.checkArgument(divisor >= 1, "Divisor must be >= 1. (%s)", divisor);

		if (item == BulkItem.NOTHING || item != bulkItem || content.isZero() || numerator == 0) {
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
			content.subtract(result, divisor);
			notifyListeners(0);
		}

		return result;
	}

	public void writeTag(CompoundTag tag) {
		tag.put("capacity",capacity.toTag());
		tag.put("content",content.toTag());
		tag.putString("bulkItem", BulkItemRegistry.INSTANCE.getId(bulkItem).toString());
	}

	public Tag toTag() {
		final CompoundTag result = new CompoundTag();
		writeTag(result);
		return result;
	}

	public void readTag(CompoundTag tag) {
		capacity = new Fraction(tag.getCompound("capacity"));
		content.readTag(tag.getCompound("content"));
		bulkItem = BulkItemRegistry.INSTANCE.get(tag.getString("bulkItem"));
	}

	protected class View implements BulkArticleView {
		@Override
		public int slot() {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return content.isZero();
		}

		@Override
		public BulkItem bulkItem() {
			return bulkItem;
		}

		@Override
		public FractionView volume() {
			return content;
		}
	}
}
