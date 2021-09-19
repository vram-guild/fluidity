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
package grondag.fluidity.impl.device;

import grondag.fluidity.api.device.BlockComponentContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("rawtypes")
public final class BlockComponentContextImpl extends AbstractComponentContextImpl implements BlockComponentContext {
	private Block block;
	private BlockPos pos;
	private BlockState blockState;
	private BlockEntity blockEntity;

	@Override
	public Block block() {
		return block;
	}

	@Override
	public BlockPos pos() {
		BlockPos result = pos;

		if(result == null && blockEntity != null) {
			result = blockEntity.getBlockPos();
			pos = result;
		}

		return result;
	}

	@Override
	public BlockEntity blockEntity() {
		BlockEntity result = blockEntity;

		if(result == null && world != null) {
			result = world.getBlockEntity(pos);
			blockEntity = result;
		}

		return result;
	}

	@Override
	public BlockState blockState() {
		return blockState;
	}

	@Override
	protected Level getWorldLazily() {
		return blockEntity == null ? null : blockEntity.getLevel();
	}

	@SuppressWarnings("unchecked")
	private BlockComponentContextImpl prepare(DeviceComponentTypeImpl componentType, Level world, BlockPos pos) {
		this.componentType = componentType;
		this.world = world;
		this.pos = pos;
		blockEntity = null;
		blockState = world.getBlockState(pos);
		block = blockState.getBlock();
		mapping = componentType.getMapping(block);
		return this;
	}

	@SuppressWarnings("unchecked")
	private BlockComponentContextImpl  prepare(DeviceComponentTypeImpl componentType, Level world, BlockPos pos, BlockState blockState) {
		this.componentType = componentType;
		this.world = world;
		this.pos = pos;
		blockEntity = null;
		this.blockState = blockState;
		block = blockState.getBlock();
		mapping = componentType.getMapping(block);
		return this;
	}

	@SuppressWarnings("unchecked")
	private BlockComponentContextImpl prepare(DeviceComponentTypeImpl componentType, BlockEntity blockEntity) {
		this.componentType = componentType;
		world = null;
		pos = null;
		this.blockEntity = blockEntity;
		blockState = blockEntity.getBlockState();
		block = blockState.getBlock();
		mapping = componentType.getMapping(block);
		return this;
	}

	private static final ThreadLocal<BlockComponentContextImpl> POOL = ThreadLocal.withInitial(BlockComponentContextImpl::new);

	static BlockComponentContextImpl get(DeviceComponentTypeImpl componentType, Level world, BlockPos pos) {
		return POOL.get().prepare(componentType, world, pos);
	}

	static BlockComponentContextImpl  get(DeviceComponentTypeImpl componentType, Level world, BlockPos pos, BlockState blockState) {
		return POOL.get().prepare(componentType, world, pos, blockState);
	}

	static BlockComponentContextImpl get(DeviceComponentTypeImpl componentType, BlockEntity blockEntity) {
		return POOL.get().prepare(componentType, blockEntity);
	}
}
