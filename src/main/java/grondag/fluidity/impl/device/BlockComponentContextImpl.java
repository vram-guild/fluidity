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

import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import grondag.fluidity.api.device.Authorization;
import grondag.fluidity.api.device.BlockComponentContext;
import grondag.fluidity.api.device.DeviceComponent;
import grondag.fluidity.api.device.DeviceComponentType;

@SuppressWarnings("rawtypes")
public final class BlockComponentContextImpl implements BlockComponentContext, DeviceComponent {
	private DeviceComponentTypeImpl componentType;
	private Function<BlockComponentContext, ?> mapping;
	private Block block;
	private World world;
	private Identifier id;
	private Direction  side;
	private Authorization auth;
	private BlockPos pos;
	private BlockState blockState;
	private BlockEntity blockEntity;

	@Override
	public Object get(Authorization auth, Direction side, Identifier id) {
		this.auth = auth;
		this.side = side;
		this.id = id;
		return mapping.apply(this);
	}

	@Override
	public DeviceComponentType componentType() {
		return componentType;
	}

	@Override
	public Identifier id() {
		return id;
	}

	@Override
	public Direction side() {
		return side;
	}

	@Override
	public Authorization auth() {
		return auth;
	}

	@Override
	public Block block() {
		return block;
	}

	@Override
	public BlockPos pos() {
		BlockPos result = pos;

		if(result == null && blockEntity != null) {
			result = blockEntity.getPos();
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
	public World world() {
		World result = world;

		if(result == null && blockEntity != null) {
			result = blockEntity.getWorld();
			world = result;
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private BlockComponentContextImpl prepare(DeviceComponentTypeImpl componentType, World world, BlockPos pos) {
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
	private BlockComponentContextImpl  prepare(DeviceComponentTypeImpl componentType, World world, BlockPos pos, BlockState blockState) {
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
		blockState = blockEntity.getCachedState();
		block = blockState.getBlock();
		mapping = componentType.getMapping(block);
		return this;
	}

	private static final ThreadLocal<BlockComponentContextImpl> POOL = ThreadLocal.withInitial(BlockComponentContextImpl::new);

	static BlockComponentContextImpl get(DeviceComponentTypeImpl componentType, World world, BlockPos pos) {
		return POOL.get().prepare(componentType, world, pos);
	}

	static BlockComponentContextImpl  get(DeviceComponentTypeImpl componentType, World world, BlockPos pos, BlockState blockState) {
		return POOL.get().prepare(componentType, world, pos, blockState);
	}

	static BlockComponentContextImpl get(DeviceComponentTypeImpl componentType, BlockEntity blockEntity) {
		return POOL.get().prepare(componentType, blockEntity);
	}
}
