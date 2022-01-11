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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus.Experimental;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.api.storage.Store;
import grondag.fluidity.base.article.AbstractStoredArticle;

@Experimental
public abstract class AbstractStorageServerDelegate<T extends AbstractStoredArticle> implements StorageListener {
	protected ServerPlayer player;
	protected Store storage;
	protected boolean isFirstUpdate = true;
	protected boolean capacityChange = true;
	protected final Int2ObjectOpenHashMap<T> updates = new Int2ObjectOpenHashMap<>();

	public AbstractStorageServerDelegate(ServerPlayer player, Store storage) {
		this.player = player;
		this.storage = storage;
		storage.eventStream().startListening(this, true);
	}

	@Override
	public void disconnect(Store storage, boolean didNotify, boolean isValid) {
		if(storage == this.storage) {
			player = null;
			this.storage = null;
		}
	}

	public abstract void sendUpdates();

	public void close(Player playerEntity) {
		if(playerEntity == player && storage != null) {
			storage.eventStream().stopListening(this, false);
			storage = null;
			player = null;
		}
	}
}
