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

import org.jetbrains.annotations.ApiStatus.Internal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import grondag.fluidity.base.synch.BulkStorageClientDelegate;
import grondag.fluidity.base.synch.BulkStorageUpdateS2C;
import grondag.fluidity.base.synch.DiscreteStorageClientDelegate;
import grondag.fluidity.base.synch.DiscreteStorageUpdateS2C;

@Internal
public class FluidityClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(DiscreteStorageUpdateS2C.ID_FULL_REFRESH, DiscreteStorageClientDelegate.INSTANCE::handleFullRefresh);
		ClientPlayNetworking.registerGlobalReceiver(DiscreteStorageUpdateS2C.ID_UPDATE, DiscreteStorageClientDelegate.INSTANCE::handleUpdate);
		ClientPlayNetworking.registerGlobalReceiver(DiscreteStorageUpdateS2C.ID_UPDATE_WITH_CAPACITY, DiscreteStorageClientDelegate.INSTANCE::handleUpdateWithCapacity);

		ClientPlayNetworking.registerGlobalReceiver(BulkStorageUpdateS2C.ID_FULL_REFRESH, BulkStorageClientDelegate.INSTANCE::handleFullRefresh);
		ClientPlayNetworking.registerGlobalReceiver(BulkStorageUpdateS2C.ID_UPDATE, BulkStorageClientDelegate.INSTANCE::handleUpdate);
		ClientPlayNetworking.registerGlobalReceiver(BulkStorageUpdateS2C.ID_UPDATE_WITH_CAPACITY, BulkStorageClientDelegate.INSTANCE::handleUpdateWithCapacity);
	}
}
