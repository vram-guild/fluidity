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

import grondag.fluidity.base.synch.ItemStorageClientDelegate;
import grondag.fluidity.base.synch.ItemStorageUpdateS2C;

@API(status = Status.INTERNAL)

public class FluidityClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientSidePacketRegistry.INSTANCE.register(ItemStorageUpdateS2C.ID_FULL_REFRESH, ItemStorageClientDelegate::handleFullRefresh);
		ClientSidePacketRegistry.INSTANCE.register(ItemStorageUpdateS2C.ID_UPDATE, ItemStorageClientDelegate::handleUpdate);
		ClientSidePacketRegistry.INSTANCE.register(ItemStorageUpdateS2C.ID_UPDATE_WITH_CAPACITY, ItemStorageClientDelegate::handleUpdateWithCapacity);
	}
}
