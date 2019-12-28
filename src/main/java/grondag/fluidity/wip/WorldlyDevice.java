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
package grondag.fluidity.wip;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

@API(status = Status.EXPERIMENTAL)
public interface WorldlyDevice {
	int dimensionId();

	default DimensionType dimension() {
		return DimensionType.byRawId(dimensionId());
	}

	default World world() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
				? ((MinecraftClient)FabricLoader.getInstance().getGameInstance()).getServer().getWorld(dimension())
						: ((MinecraftDedicatedServer)FabricLoader.getInstance().getGameInstance()).getWorld(dimension());
	}
}
