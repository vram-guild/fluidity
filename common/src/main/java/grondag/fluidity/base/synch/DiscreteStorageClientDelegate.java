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

@Experimental
public class DiscreteStorageClientDelegate extends AbstractStorageClientDelegate<DiscreteDisplayDelegate> {
	public static final DiscreteStorageClientDelegate INSTANCE = new DiscreteStorageClientDelegate();

	protected long capacity;
	protected long usedCapacity;

	/**
	 * Incorporates changes and updates sort order. Returns true if a refresh was
	 * performed.
	 */
	protected DiscreteDisplayDelegate[] readItems(FriendlyByteBuf buf) {
		final int limit = buf.readInt();
		final DiscreteDisplayDelegate[] items = new DiscreteDisplayDelegate[limit];

		for (int i = 0; i < limit; i++) {
			items[i] = DiscreteDisplayDelegate.create(Article.fromPacket(buf), buf.readVarLong(), buf.readVarInt());
		}

		return items;
	}

	@Override
	public void handleUpdateWithCapacity(FriendlyByteBuf buffer, PacketContext ctx) {
		final DiscreteDisplayDelegate[] items = readItems(buffer);
		final long newCapacity = buffer.readVarLong();
		ctx.queue(() -> handleUpdateInner(items, newCapacity));
	}

	@Override
	public void handleUpdate(FriendlyByteBuf buffer, PacketContext ctx) {
		final DiscreteDisplayDelegate[] items = readItems(buffer);
		ctx.queue(() -> handleUpdateInner(items, -1));
	}

	@Override
	public void handleFullRefresh(FriendlyByteBuf buffer, PacketContext ctx) {
		final DiscreteDisplayDelegate[] items = readItems(buffer);
		final long newCapacity = buffer.readVarLong();
		ctx.queue(() -> handleFullRefreshInner(items, newCapacity));
	}

	protected void handleFullRefreshInner(DiscreteDisplayDelegate[] items, long newCapacity) {
		capacity = newCapacity;
		MAP.clear();
		LIST.clear();
		usedCapacity = 0;

		final int limit = items.length;

		for (int i = 0; i < limit; i++) {
			final DiscreteDisplayDelegate item = items[i];

			if (item.getCount() > 0) {
				MAP.put(item.handle(), item);
				addToListIfIncluded(item);
				usedCapacity += item.getCount();
			}
		}

		isSortDirty = true;
	}

	protected void handleUpdateInner(DiscreteDisplayDelegate[] items, long newCapacity) {
		final int limit = items.length;

		if (newCapacity >= 0) {
			capacity = newCapacity;
		}

		for (int i = 0; i < limit; i++) {
			final DiscreteDisplayDelegate update = items[i];
			final DiscreteDisplayDelegate prior = MAP.get(update.handle());

			assert update.getCount() >= 0;

			if (prior == null) {
				if (update.getCount() > 0) {
					MAP.put(update.handle(), update);
					addToListIfIncluded(update);
					usedCapacity += update.getCount();
				}
			} else if (update.getCount() == 0) {
				MAP.remove(update.handle());
				LIST.remove(prior);
				usedCapacity -= prior.getCount();
			} else {
				usedCapacity += update.getCount() - prior.getCount();
				prior.setCount(update.getCount());
			}
		}

		isSortDirty = true;
	}

	public long capacity() {
		return capacity;
	}

	public long usedCapacity() {
		return usedCapacity;
	}

	@Override
	public int fillPercentage() {
		return capacity == 0 ? 0 : (int) (usedCapacity * 100 / capacity);
	}
}
