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

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.AbstractStoredArticle;

@API(status = Status.EXPERIMENTAL)
public abstract class AbstractStorageServerDelegate<T extends AbstractStoredArticle> implements StorageListener {
	protected ServerPlayerEntity player;
	protected Storage storage;
	protected boolean isFirstUpdate = true;
	protected boolean capacityChange = true;
	protected final Int2ObjectOpenHashMap<T> updates = new Int2ObjectOpenHashMap<>();

	public AbstractStorageServerDelegate(ServerPlayerEntity player, Storage storage) {
		this.player = player;
		this.storage = storage;
		storage.eventStream().startListening(this, true);
	}

	@Override
	public void disconnect(Storage storage, boolean didNotify, boolean isValid) {
		if(storage == this.storage) {
			player = null;
			this.storage = null;
		}
	}

	public abstract void sendUpdates();

	public void close(PlayerEntity playerEntity) {
		if(playerEntity == player && storage != null) {
			storage.eventStream().stopListening(this, false);
			storage = null;
			player = null;
		}
	}
}
