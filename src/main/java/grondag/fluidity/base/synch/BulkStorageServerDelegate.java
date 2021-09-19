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

import org.jetbrains.annotations.ApiStatus.Experimental;
import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.Store;
import grondag.fluidity.base.article.StoredBulkArticle;
import grondag.fluidity.base.storage.bulk.BulkStorageListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

@Experimental
public class BulkStorageServerDelegate extends AbstractStorageServerDelegate<StoredBulkArticle> implements BulkStorageListener {
	public BulkStorageServerDelegate(ServerPlayer player, Store storage) {
		super(player, storage);
	}

	@Override
	public void onAccept(Store storage, int handle, Article item, Fraction delta, Fraction newVolume) {
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
	public void onSupply(Store storage, int slot, Article item, Fraction delta, Fraction newVolume) {
		assert !newVolume.isNegative();

		onAccept(storage, slot, item, delta, newVolume);
	}

	@Override
	public void onCapacityChange(Store storage, Fraction capacityDelta) {
		if(storage != null) {
			capacityChange = true;
		}
	}

	@Override
	public void sendUpdates() {
		if(updates.isEmpty() && !(isFirstUpdate || capacityChange)) {
			return;
		}

		final FriendlyByteBuf buf = BulkStorageUpdateS2C.begin(updates.size());

		for(final StoredBulkArticle a : updates.values()) {
			BulkStorageUpdateS2C.append(buf, a.article(), a.amount(), a.handle());
		}

		if(isFirstUpdate) {
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
