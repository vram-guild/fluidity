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
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.Store;
import grondag.fluidity.base.article.StoredBulkArticle;
import grondag.fluidity.base.storage.bulk.BulkStorageListener;

@Experimental
public class BulkStorageServerDelegate extends AbstractStorageServerDelegate<StoredBulkArticle> implements BulkStorageListener {
	public BulkStorageServerDelegate(ServerPlayer player, Store storage) {
		super(player, storage);
	}

	@Override
	public void onAccept(Store storage, int handle, Article item, Fraction delta, Fraction newVolume) {
		assert !newVolume.isNegative();

		if (storage != null) {
			final StoredBulkArticle update = updates.get(handle);

			if (update == null) {
				updates.put(handle, StoredBulkArticle.of(item, newVolume, handle));
			} else {
				update.prepare(item, newVolume, handle);
			}
		}
	}

	@Override
	public void onSupply(Store storage, int slot, Article item, Fraction delta, Fraction newVolume) {
		assert !newVolume.isNegative();

		onAccept(storage, slot, item, delta, newVolume);
	}

	@Override
	public void onCapacityChange(Store storage, Fraction capacityDelta) {
		if (storage != null) {
			capacityChange = true;
		}
	}

	@Override
	public void sendUpdates() {
		if (updates.isEmpty() && !(isFirstUpdate || capacityChange)) {
			return;
		}

		final FriendlyByteBuf buf = BulkStorageUpdateS2C.begin(updates.size());

		for (final StoredBulkArticle a : updates.values()) {
			BulkStorageUpdateS2C.append(buf, a.article(), a.amount(), a.handle());
		}

		if (isFirstUpdate) {
			// UGLY: find way to avoid unreliable cast here and in next block
			BulkStorageUpdateS2C.sendFullRefresh(player, buf, storage.volume());
			isFirstUpdate = false;
			capacityChange = false;
		} else if (capacityChange) {
			BulkStorageUpdateS2C.sendUpdateWithCapacity(player, buf, storage.volume());
			capacityChange = false;
		} else {
			BulkStorageUpdateS2C.sendUpdate(player, buf);
		}

		updates.clear();
	}
}
