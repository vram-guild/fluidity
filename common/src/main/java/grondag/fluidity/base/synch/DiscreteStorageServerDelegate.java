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

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.storage.Store;
import grondag.fluidity.base.article.StoredDiscreteArticle;
import grondag.fluidity.base.storage.discrete.DiscreteStorageListener;

@Experimental
public class DiscreteStorageServerDelegate extends AbstractStorageServerDelegate<StoredDiscreteArticle> implements DiscreteStorageListener {
	public DiscreteStorageServerDelegate(ServerPlayer player, Store storage) {
		super(player, storage);
	}

	@Override
	public void onAccept(Store storage, int handle, Article item, long delta, long newCount) {
		assert newCount >= 0;

		if (storage != null) {
			final StoredDiscreteArticle update = updates.get(handle);

			if (update == null) {
				updates.put(handle, StoredDiscreteArticle.of(item, newCount, handle));
			} else {
				update.prepare(item, newCount, handle);
			}
		}
	}

	@Override
	public void onSupply(Store storage, int slot, Article item, long delta, long newCount) {
		assert newCount >= 0;

		onAccept(storage, slot, item, delta, newCount);
	}

	@Override
	public void onCapacityChange(Store storage, long capacityDelta) {
		if (storage != null) {
			capacityChange = true;
		}
	}

	@Override
	public void sendUpdates() {
		if (updates.isEmpty() && !(isFirstUpdate || capacityChange)) {
			return;
		}

		final FriendlyByteBuf buf = DiscreteStorageUpdateS2C.begin(updates.size());

		for (final StoredDiscreteArticle a : updates.values()) {
			DiscreteStorageUpdateS2C.append(buf, a.article(), a.count(), a.handle());
		}

		if (isFirstUpdate) {
			DiscreteStorageUpdateS2C.sendFullRefresh(player, buf, storage.capacity());
			isFirstUpdate = false;
			capacityChange = false;
		} else if (capacityChange) {
			DiscreteStorageUpdateS2C.sendUpdateWithCapacity(player, buf, storage.capacity());
			capacityChange = false;
		} else {
			DiscreteStorageUpdateS2C.sendUpdate(player, buf);
		}

		updates.clear();
	}
}
