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
package grondag.fluidity.base.synch;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.base.article.StoredBulkArticle;
import grondag.fluidity.base.storage.bulk.BulkStorageListener;
import grondag.fluidity.impl.AbstractFraction;

@API(status = Status.EXPERIMENTAL)
public class BulkStorageServerDelegate extends AbstractStorageServerDelegate<StoredBulkArticle> implements BulkStorageListener {
	public BulkStorageServerDelegate(ServerPlayerEntity player, Storage storage) {
		super(player, storage);
	}

	@Override
	public void onAccept(Storage storage, int handle, Article item, FractionView delta, FractionView newVolume) {
		assert !newVolume.isNegative();

		if(storage != null) {
			final StoredBulkArticle update = updates.get(handle);

			if(update == null) {
				updates.put(handle, StoredBulkArticle.of(item, newVolume, handle));
			} else {
				update.prepare(item, newVolume, handle);
			}
		}
	}

	@Override
	public void onSupply(Storage storage, int slot, Article item, FractionView delta, FractionView newVolume) {
		assert !newVolume.isNegative();

		onAccept(storage, slot, item, delta, newVolume);
	}

	@Override
	public void onCapacityChange(Storage storage, FractionView capacityDelta) {
		if(storage != null) {
			capacityChange = true;
		}
	}

	@Override
	public void sendUpdates() {
		if(updates.isEmpty() && !(isFirstUpdate || capacityChange)) {
			return;
		}

		final PacketByteBuf buf = BulkStorageUpdateS2C.begin(updates.size());

		for(final StoredBulkArticle a : updates.values()) {
			BulkStorageUpdateS2C.append(buf, a.article(), a.amount(), a.handle());
		}

		if(isFirstUpdate) {
			// UGLY: find way to avoid unreliable cast here and in next block
			BulkStorageUpdateS2C.sendFullRefresh(player, buf, (AbstractFraction) storage.volume());
			isFirstUpdate = false;
			capacityChange = false;
		} else if (capacityChange) {
			BulkStorageUpdateS2C.sendUpdateWithCapacity(player, buf, (AbstractFraction) storage.volume());
			capacityChange = false;
		} else {
			BulkStorageUpdateS2C.sendUpdate(player, buf);
		}

		updates.clear();
	}
}
