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

package grondag.fluidity.base.synch;

import dev.architectury.networking.NetworkManager.PacketContext;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.network.FriendlyByteBuf;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.MutableFraction;

@Experimental
public class BulkStorageClientDelegate extends AbstractStorageClientDelegate<BulkDisplayDelegate> {
	public static final BulkStorageClientDelegate INSTANCE = new BulkStorageClientDelegate();

	protected final MutableFraction capacity = new MutableFraction();
	protected final MutableFraction usedCapacity = new MutableFraction();

	/**
	 * Incorporates changes and updates sort order. Returns true if a refresh was
	 * performed.
	 */
	protected BulkDisplayDelegate[] readItems(FriendlyByteBuf buf) {
		final int limit = buf.readInt();
		final BulkDisplayDelegate[] items = new BulkDisplayDelegate[limit];

		for (int i = 0; i < limit; i++) {
			items[i] = BulkDisplayDelegate.create(Article.fromPacket(buf), new Fraction(buf), buf.readVarInt());
		}

		return items;
	}

	@Override
	public void handleUpdateWithCapacity(FriendlyByteBuf buffer, PacketContext ctx) {
		final BulkDisplayDelegate[] items = readItems(buffer);
		final Fraction newCapacity = new Fraction(buffer);
		ctx.queue(() -> handleUpdateInner(items, newCapacity));
	}

	@Override
	public void handleUpdate(FriendlyByteBuf buffer, PacketContext ctx) {
		final BulkDisplayDelegate[] items = readItems(buffer);
		ctx.queue(() -> handleUpdateInner(items, null));
	}

	@Override
	public void handleFullRefresh(FriendlyByteBuf buffer, PacketContext ctx) {
		final BulkDisplayDelegate[] items = readItems(buffer);
		final Fraction newCapacity = new Fraction(buffer);
		ctx.queue(() -> handleFullRefreshInner(items, newCapacity));
	}

	protected void handleFullRefreshInner(BulkDisplayDelegate[] items, Fraction newCapacity) {
		capacity.set(newCapacity);
		MAP.clear();
		LIST.clear();
		usedCapacity.set(0);

		final int limit = items.length;

		for (int i = 0; i < limit; i++) {
			final BulkDisplayDelegate item = items[i];

			if (!item.getAmount().isZero()) {
				MAP.put(item.handle(), item);
				LIST.add(item);
				usedCapacity.add(item.getAmount());
			}
		}

		isSortDirty = true;
	}

	protected void handleUpdateInner(BulkDisplayDelegate[] items, Fraction newCapacity) {
		final int limit = items.length;

		if (newCapacity != null) {
			capacity.set(newCapacity);
		}

		for (int i = 0; i < limit; i++) {
			final BulkDisplayDelegate update = items[i];
			final BulkDisplayDelegate prior = MAP.get(update.handle());

			assert !update.getAmount().isNegative();

			if (prior == null) {
				if (!update.getAmount().isZero()) {
					MAP.put(update.handle(), update);
					addToListIfIncluded(update);
					usedCapacity.add(update.getAmount());
				}
			} else if (update.getAmount().isZero()) {
				MAP.remove(update.handle());
				LIST.remove(prior);
				usedCapacity.subtract(prior.getAmount());
			} else {
				usedCapacity.add(update.getAmount()).subtract(prior.getAmount());
				prior.setAmount(update.getAmount());
			}
		}

		isSortDirty = true;
	}

	public Fraction capacity() {
		return capacity;
	}

	public Fraction usedCapacity() {
		return usedCapacity;
	}

	@Override
	public int fillPercentage() {
		return capacity.isZero() ? 0 : (int) Math.round(usedCapacity.toDouble() * 100 / capacity.toDouble());
	}
}
