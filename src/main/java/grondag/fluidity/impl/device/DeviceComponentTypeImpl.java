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

import java.util.IdentityHashMap;
import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import grondag.fluidity.api.device.BlockComponentContext;
import grondag.fluidity.api.device.DeviceComponent;
import grondag.fluidity.api.device.DeviceComponentType;

public final class DeviceComponentTypeImpl<T> implements DeviceComponentType<T>{
	private final T absent;
	private final Function<BlockComponentContext, ?> defaultBlockMapping;
	private final IdentityHashMap<Block, Function<BlockComponentContext, ?>> blockMappings = new IdentityHashMap<>();
	private final IdentityHashMap<Item, Function<ItemStack, ?>> itemMappings = new IdentityHashMap<>();
	private final AbsentDeviceComponent<T> absentComponent;

	DeviceComponentTypeImpl(T absent) {
		this.absent = absent;
		defaultBlockMapping = b -> absent;
		absentComponent = new AbsentDeviceComponent<>(this);
	}

	@Override
	public T absent() {
		return absent;
	}

	Function<BlockComponentContext, ?> getMapping(Block block) {
		return blockMappings.getOrDefault(block, defaultBlockMapping);
	}

	@Override
	public void addProvider(Function<BlockComponentContext, T> mapping, Block... blocks) {
		for(final Block b : blocks) {
			blockMappings.put(b, mapping);
		}
	}

	@Override
	public void addProvider(Function<ItemStack, T> mapping, Item... items) {
		for(final Item i : items) {
			itemMappings.put(i, mapping);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public DeviceComponent<T> get(World world, BlockPos pos) {
		return BlockComponentContextImpl.get(this, world, pos);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DeviceComponent<T> get(World world, BlockPos pos, BlockState blockState) {
		return BlockComponentContextImpl.get(this, world, pos, blockState);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DeviceComponent<T> get(BlockEntity blockEntity) {
		return BlockComponentContextImpl.get(this, blockEntity);
	}

	@Override
	public DeviceComponent<T> getAbsent() {
		return absentComponent;
	}
}
