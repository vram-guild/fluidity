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

package grondag.fluidity.wip.base.transport;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.transact.TransactionParticipant;
import grondag.fluidity.wip.api.transport.CarrierSession;

/**
 * Mimics consumer/supplier functions - used to throttle throughput.
 * All implementations should be self-enlisting because cost accounting
 * is typically transparent to users of a carrier.
 */
@Experimental
public interface CarrierCostFunction extends TransactionParticipant {
	/**
	 * Throttles articles being transported via carrier. May return less than requested.
	 *
	 * @param item Item to add
	 * @param tag NBT if item has it, null otherwise.
	 * @param count How many to add. Must be >= 0;
	 * @param simulate If true, will forecast result without making changes.
	 * @return Count added, or that would be added if {@code simulate} = true.
	 */
	long apply(CarrierSession sender, Article item, long count, boolean simulate);

	default long apply(CarrierSession sender, Item item, @Nullable CompoundTag tag, long count, boolean simulate) {
		return apply(sender, Article.of(item, tag), count, simulate);
	}

	default long apply(CarrierSession sender, Item item, long count, boolean simulate) {
		return apply(sender, Article.of(item), count, simulate);
	}

	default long apply(CarrierSession sender, ItemStack stack, long count, boolean simulate) {
		return apply(sender, Article.of(stack), count, simulate);
	}

	default long apply(CarrierSession sender, ItemStack stack, boolean simulate) {
		return apply(sender, Article.of(stack), stack.getCount(), simulate);
	}

	Fraction apply(CarrierSession sender, Article item, Fraction volume, boolean simulate);

	long apply(CarrierSession sender, Article item, long numerator, long divisor, boolean simulate);

	@Override
	default boolean isSelfEnlisting() {
		return true;
	}

	CarrierCostFunction FREE = new CarrierCostFunction() {
		@Override
		public TransactionDelegate getTransactionDelegate() {
			return TransactionDelegate.IGNORE;
		}

		@Override
		public long apply(CarrierSession sender, Article item, long count, boolean simulate) {
			return count;
		}

		@Override
		public Fraction apply(CarrierSession sender, Article item, Fraction volume, boolean simulate) {
			return volume.toImmutable();
		}

		@Override
		public long apply(CarrierSession sender, Article item, long numerator, long divisor, boolean simulate) {
			return numerator;
		}
	};
}
