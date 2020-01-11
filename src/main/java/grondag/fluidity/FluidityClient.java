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
package grondag.fluidity;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;

import grondag.fluidity.base.synch.BulkStorageClientDelegate;
import grondag.fluidity.base.synch.BulkStorageUpdateS2C;
import grondag.fluidity.base.synch.DiscreteStorageClientDelegate;
import grondag.fluidity.base.synch.DiscreteStorageUpdateS2C;

@API(status = Status.INTERNAL)

public class FluidityClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientSidePacketRegistry.INSTANCE.register(DiscreteStorageUpdateS2C.ID_FULL_REFRESH, DiscreteStorageClientDelegate.INSTANCE::handleFullRefresh);
		ClientSidePacketRegistry.INSTANCE.register(DiscreteStorageUpdateS2C.ID_UPDATE, DiscreteStorageClientDelegate.INSTANCE::handleUpdate);
		ClientSidePacketRegistry.INSTANCE.register(DiscreteStorageUpdateS2C.ID_UPDATE_WITH_CAPACITY, DiscreteStorageClientDelegate.INSTANCE::handleUpdateWithCapacity);

		ClientSidePacketRegistry.INSTANCE.register(BulkStorageUpdateS2C.ID_FULL_REFRESH, BulkStorageClientDelegate.INSTANCE::handleFullRefresh);
		ClientSidePacketRegistry.INSTANCE.register(BulkStorageUpdateS2C.ID_UPDATE, BulkStorageClientDelegate.INSTANCE::handleUpdate);
		ClientSidePacketRegistry.INSTANCE.register(BulkStorageUpdateS2C.ID_UPDATE_WITH_CAPACITY, BulkStorageClientDelegate.INSTANCE::handleUpdateWithCapacity);
	}
}
