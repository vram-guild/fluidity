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

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

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
		if (storage == this.storage) {
			player = null;
			this.storage = null;
		}
	}

	public abstract void sendUpdates();

	public void close(Player playerEntity) {
		if (playerEntity == player && storage != null) {
			storage.eventStream().stopListening(this, false);
			storage = null;
			player = null;
		}
	}
}
