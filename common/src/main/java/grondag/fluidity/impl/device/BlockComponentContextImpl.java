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

package grondag.fluidity.impl.device;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import grondag.fluidity.api.device.BlockComponentContext;

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

		if (result == null && blockEntity != null) {
			result = blockEntity.getBlockPos();
			pos = result;
		}

		return result;
	}

	@Override
	public BlockEntity blockEntity() {
		BlockEntity result = blockEntity;

		if (result == null && world != null) {
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
	private BlockComponentContextImpl prepare(DeviceComponentTypeImpl componentType, Level world, BlockPos pos, BlockState blockState) {
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

	static BlockComponentContextImpl get(DeviceComponentTypeImpl componentType, Level world, BlockPos pos, BlockState blockState) {
		return POOL.get().prepare(componentType, world, pos, blockState);
	}

	static BlockComponentContextImpl get(DeviceComponentTypeImpl componentType, BlockEntity blockEntity) {
		return POOL.get().prepare(componentType, blockEntity);
	}
}
