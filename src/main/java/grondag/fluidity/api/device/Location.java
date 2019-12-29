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
package grondag.fluidity.api.device;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

@API(status = Status.EXPERIMENTAL)
public interface Location extends BlockPointer {
	default BlockPos pos() {
		return BlockPos.fromLong(packedPos());
	}

	long packedPos();

	int dimensionId();

	default DimensionType dimension() {
		return DimensionType.byRawId(dimensionId());
	}

	default World world() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
				? ((MinecraftClient)FabricLoader.getInstance().getGameInstance()).getServer().getWorld(dimension())
						: ((MinecraftDedicatedServer)FabricLoader.getInstance().getGameInstance()).getWorld(dimension());
	}

	@Override
	default World getWorld() {
		return world();
	}

	@Override
	default double getX() {
		return pos().getX();
	}

	@Override
	default double getY() {
		return pos().getY();
	}

	@Override
	default double getZ() {
		return pos().getZ();
	}

	@Override
	default BlockPos getBlockPos() {
		return pos();
	}

	@Override
	default BlockState getBlockState() {
		return world().getBlockState(pos());
	}

	@SuppressWarnings("unchecked")
	@Override
	default <T extends BlockEntity> T getBlockEntity() {
		return (T) world().getBlockEntity(pos());
	}
}