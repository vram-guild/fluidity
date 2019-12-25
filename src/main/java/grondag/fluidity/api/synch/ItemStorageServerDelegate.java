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

package grondag.fluidity.api.synch;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

import grondag.fluidity.api.item.Article;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.api.storage.discrete.DiscreteStorage;
import grondag.fluidity.api.storage.discrete.DiscreteStorageListener;
import grondag.fluidity.base.article.DiscreteStoredArticle;

@API(status = Status.EXPERIMENTAL)
public class ItemStorageServerDelegate implements DiscreteStorageListener {
	protected ServerPlayerEntity player;
	protected DiscreteStorage storage;
	protected boolean isFirstUpdate = true;
	protected boolean capacityChange = true;
	protected final Int2ObjectOpenHashMap<DiscreteStoredArticle> updates = new Int2ObjectOpenHashMap<>();

	public ItemStorageServerDelegate(ServerPlayerEntity player, DiscreteStorage storage) {
		this.player = player;
		this.storage = storage;
		storage.startListening(this);
	}

	@Override
	public void disconnect(Storage<?, DiscreteStorageListener> storage) {
		if(storage == this.storage) {
			player = null;
			this.storage = null;
		}
	}

	@Override
	public void onAccept(Storage<?, DiscreteStorageListener> storage, int handle, Article item, long delta, long newCount) {
		if(storage != null && storage == this.storage) {
			final DiscreteStoredArticle update = updates.get(handle);

			if(update == null) {
				updates.put(handle, DiscreteStoredArticle.of(item, newCount, handle));
			} else {
				update.prepare(item, newCount, handle);
			}
		}
	}

	@Override
	public void onSupply(Storage<?, DiscreteStorageListener> storage, int slot, Article item, long delta, long newCount) {
		onAccept(storage, slot, item, delta, newCount);
	}

	@Override
	public void onCapacityChange(Storage<?, DiscreteStorageListener> storage, long capacityDelta) {
		if(storage != null && storage == this.storage) {
			capacityChange = true;
		}
	}

	public void sendUpdates() {
		if(updates.isEmpty() && !(isFirstUpdate || capacityChange)) {
			return;
		}

		final PacketByteBuf buf = ItemStorageUpdateS2C.begin(updates.size());

		for(final DiscreteStoredArticle a : updates.values()) {
			ItemStorageUpdateS2C.append(buf, a.toStack(), a.count, a.handle);
		}

		if(isFirstUpdate) {
			ItemStorageUpdateS2C.sendFullRefresh(player, buf, storage.capacity());
			isFirstUpdate = false;
			capacityChange = false;
		} else if (capacityChange) {
			ItemStorageUpdateS2C.sendUpdateWithCapacity(player, buf, storage.capacity());
			capacityChange = false;
		} else {
			ItemStorageUpdateS2C.sendUpdate(player, buf);
		}

		updates.clear();
	}

	public void close(PlayerEntity playerEntity) {
		if(playerEntity == player && storage != null) {
			storage.stopListening(this);
			storage = null;
			player = null;
		}
	}
}
