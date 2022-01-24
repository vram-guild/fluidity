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

package grondag.fluidity.impl;

import dev.architectury.networking.NetworkManager;

import grondag.fluidity.base.synch.BulkStorageClientDelegate;
import grondag.fluidity.base.synch.BulkStorageUpdateS2C;
import grondag.fluidity.base.synch.DiscreteStorageClientDelegate;
import grondag.fluidity.base.synch.DiscreteStorageUpdateS2C;

public abstract class FluidityClient {
	private FluidityClient() { }

	public static void initialize() {
		NetworkManager.registerReceiver(NetworkManager.s2c(), DiscreteStorageUpdateS2C.ID_FULL_REFRESH, DiscreteStorageClientDelegate.INSTANCE::handleFullRefresh);
		NetworkManager.registerReceiver(NetworkManager.s2c(), DiscreteStorageUpdateS2C.ID_UPDATE, DiscreteStorageClientDelegate.INSTANCE::handleUpdate);
		NetworkManager.registerReceiver(NetworkManager.s2c(), DiscreteStorageUpdateS2C.ID_UPDATE_WITH_CAPACITY, DiscreteStorageClientDelegate.INSTANCE::handleUpdateWithCapacity);

		NetworkManager.registerReceiver(NetworkManager.s2c(), BulkStorageUpdateS2C.ID_FULL_REFRESH, BulkStorageClientDelegate.INSTANCE::handleFullRefresh);
		NetworkManager.registerReceiver(NetworkManager.s2c(), BulkStorageUpdateS2C.ID_UPDATE, BulkStorageClientDelegate.INSTANCE::handleUpdate);
		NetworkManager.registerReceiver(NetworkManager.s2c(), BulkStorageUpdateS2C.ID_UPDATE_WITH_CAPACITY, BulkStorageClientDelegate.INSTANCE::handleUpdateWithCapacity);
	}
}
