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
package grondag.fluidity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

import grondag.fluidity.api.synch.ItemStorageInteractionC2S;
import grondag.fluidity.impl.TransactionImpl;
import grondag.fluidity.wip.CompoundDeviceManager;

/**

 * TODO: how do pipes work?
 * TODO: make a pump
 * TODO: make a tank
 * TODO: controller/access block
 * TODO: crafting block
 *
 * TODO: java docs
 * TODO: wiki
 *
 * TODO: explicit device disconnect handling to allow retaining storage instances without wrapping
 * TODO: testing switch
 * TODO: implement multiblock limits
 * TODO: throttle tick times
 * TODO: article metadata loader
 *
 */
@API(status = Status.INTERNAL)
public class Fluidity implements ModInitializer {
	public static final String MOD_ID = "fluidity";
	public static final Logger LOG = LogManager.getLogger("Fluidity");

	public static void trace(String message, Object... args) {
		LOG.info(String.format("[Fluidity] " + message, args));
	}

	@Override
	public void onInitialize() {
		FluidityConfig.init();
		ServerTickCallback.EVENT.register(CompoundDeviceManager::tick);

		ServerStartCallback.EVENT.register(s -> {
			TransactionImpl.setServerThread(s);
			CompoundDeviceManager.start(s);
		});

		ServerSidePacketRegistry.INSTANCE.register(ItemStorageInteractionC2S.ID, ItemStorageInteractionC2S::accept);
	}
}
