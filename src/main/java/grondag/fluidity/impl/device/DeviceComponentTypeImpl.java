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
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import grondag.fluidity.api.device.BlockComponentContext;
import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.api.device.ItemComponentContext;

public final class DeviceComponentTypeImpl<T> implements DeviceComponentType<T>{
	private final T absent;
	private final Function<BlockComponentContext, ?> defaultBlockMapping;
	private final Function<ItemComponentContext, ?> defaultItemMapping;
	private final IdentityHashMap<Block, Function<BlockComponentContext, ?>> blockMappings = new IdentityHashMap<>();
	private final IdentityHashMap<Item, Function<ItemComponentContext, ?>> itemMappings = new IdentityHashMap<>();
	private final IdentityHashMap<Item, ObjectArrayList<BiPredicate<ItemComponentContext, T>>> itemActions = new IdentityHashMap<>();
	private final AbsentDeviceComponent<T> absentComponent;

	DeviceComponentTypeImpl(T absent) {
		this.absent = absent;
		defaultBlockMapping = b -> absent;
		defaultItemMapping = i -> absent;
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
	public void registerProvider(Function<BlockComponentContext, T> mapping, Block... blocks) {
		for(final Block b : blocks) {
			blockMappings.put(b, mapping);
		}
	}

	Function<ItemComponentContext, ?> getMapping(Item item) {
		return itemMappings.getOrDefault(item, defaultItemMapping);
	}

	@Override
	public void registerProvider(Function<ItemComponentContext, T> mapping, Item... items) {
		for(final Item i : items) {
			itemMappings.put(i, mapping);
		}
	}

	@Override
	public void registerAction(BiPredicate<ItemComponentContext, T> action, Item... items) {
		for(final Item i : items) {
			ObjectArrayList<BiPredicate<ItemComponentContext, T>> list = itemActions.get(i);

			if(list == null) {
				list = new ObjectArrayList<>(4);
				itemActions.put(i, list);
			}

			list.add(action);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public DeviceComponentAccess<T> getAccess(World world, BlockPos pos) {
		return BlockComponentContextImpl.get(this, world, pos);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DeviceComponentAccess<T> getAccess(World world, BlockPos pos, BlockState blockState) {
		return BlockComponentContextImpl.get(this, world, pos, blockState);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DeviceComponentAccess<T> getAccess(BlockEntity blockEntity) {
		return BlockComponentContextImpl.get(this, blockEntity);
	}

	@Override
	public DeviceComponentAccess<T> getAbsentAccess() {
		return absentComponent;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DeviceComponentAccess<T> getAccessForHeldItem(Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, ServerPlayerEntity player) {
		return ItemComponentContextImpl.get(this, stackGetter, stackSetter, player);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DeviceComponentAccess<T> getAccess(Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, World world) {
		return ItemComponentContextImpl.get(this, stackGetter, stackSetter, world);
	}

	@Override
	public boolean applyActions(T target, Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, ServerPlayerEntity player) {
		final ObjectArrayList<BiPredicate<ItemComponentContext, T>> actions = itemActions.get(stackGetter.get().getItem());

		if(actions != null && !actions.isEmpty()) {
			return applyActions(target, actions, ItemComponentContextImpl.get(this, stackGetter, stackSetter, player));
		}

		return false;
	}

	@Override
	public boolean applyActions(T target, Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, World world) {
		final ObjectArrayList<BiPredicate<ItemComponentContext, T>> actions = itemActions.get(stackGetter.get().getItem());

		if(actions != null && !actions.isEmpty()) {
			return applyActions(target, actions, ItemComponentContextImpl.get(this, stackGetter, stackSetter, world));
		}

		return false;
	}

	private boolean applyActions(T target, ObjectArrayList<BiPredicate<ItemComponentContext, T>> actions, ItemComponentContext ctx) {
		for(final BiPredicate<ItemComponentContext, T> action : actions) {
			if(action.test(ctx, target)) {
				return true;
			}
		}

		return false;
	}
}
