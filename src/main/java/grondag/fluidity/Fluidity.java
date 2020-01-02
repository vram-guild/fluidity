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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

import grondag.fluidity.base.synch.ItemStorageInteractionC2S;
import grondag.fluidity.impl.MultiBlockManagerImpl;
import grondag.fluidity.impl.TransactionImpl;

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
		ServerTickCallback.EVENT.register(MultiBlockManagerImpl::tick);

		ServerStartCallback.EVENT.register(s -> {
			TransactionImpl.setServerThread(s);
			MultiBlockManagerImpl.start(s);
		});

		ServerSidePacketRegistry.INSTANCE.register(ItemStorageInteractionC2S.ID, ItemStorageInteractionC2S::accept);
	}
}
