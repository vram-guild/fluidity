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
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.base.article.StoredDiscreteArticle;
import grondag.fluidity.base.storage.discrete.DiscreteStorageListener;

@API(status = Status.EXPERIMENTAL)
public class DiscreteStorageServerDelegate extends AbstractStorageServerDelegate<StoredDiscreteArticle> implements DiscreteStorageListener {
	public DiscreteStorageServerDelegate(ServerPlayerEntity player, Storage storage) {
		super(player, storage);
	}

	@Override
	public void onAccept(Storage storage, int handle, Article item, long delta, long newCount) {
		assert newCount >= 0;

		if(storage != null) {
			final StoredDiscreteArticle update = updates.get(handle);

			if(update == null) {
				updates.put(handle, StoredDiscreteArticle.of(item, newCount, handle));
			} else {
				update.prepare(item, newCount, handle);
			}
		}
	}

	@Override
	public void onSupply(Storage storage, int slot, Article item, long delta, long newCount) {
		assert newCount >= 0;

		onAccept(storage, slot, item, delta, newCount);
	}

	@Override
	public void onCapacityChange(Storage storage, long capacityDelta) {
		if(storage != null) {
			capacityChange = true;
		}
	}

	@Override
	public void sendUpdates() {
		if(updates.isEmpty() && !(isFirstUpdate || capacityChange)) {
			return;
		}

		final PacketByteBuf buf = DiscreteStorageUpdateS2C.begin(updates.size());

		for(final StoredDiscreteArticle a : updates.values()) {
			DiscreteStorageUpdateS2C.append(buf, a.article(), a.count(), a.handle());
		}

		if(isFirstUpdate) {
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
