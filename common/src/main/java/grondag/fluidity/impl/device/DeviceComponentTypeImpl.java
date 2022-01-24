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

import java.util.IdentityHashMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import grondag.fluidity.api.device.BlockComponentContext;
import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.api.device.EntityComponentContext;
import grondag.fluidity.api.device.ItemComponentContext;

public final class DeviceComponentTypeImpl<T> implements DeviceComponentType<T> {
	private final T absent;
	private final Function<BlockComponentContext, ?> defaultBlockMapping;
	private final Function<ItemComponentContext, ?> defaultItemMapping;
	private final Function<EntityComponentContext, ?> defaultEntityMapping;

	private final IdentityHashMap<Block, Function<BlockComponentContext, ?>> blockMappings = new IdentityHashMap<>();
	private final IdentityHashMap<Item, Function<ItemComponentContext, ?>> itemMappings = new IdentityHashMap<>();
	private final IdentityHashMap<EntityType<?>, Function<EntityComponentContext, ?>> entityMappings = new IdentityHashMap<>();
	private final IdentityHashMap<Item, ObjectArrayList<BiPredicate<ItemComponentContext, T>>> itemActions = new IdentityHashMap<>();

	private ObjectArrayList<Pair<Function<EntityComponentContext, T>, Predicate<EntityType<?>>>> deferedEntityMappings;

	private final AbsentDeviceComponent<T> absentComponent;

	DeviceComponentTypeImpl(T absent) {
		this.absent = absent;
		defaultBlockMapping = b -> absent;
		defaultItemMapping = i -> absent;
		defaultEntityMapping = e -> absent;
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
		for (final Block b : blocks) {
			blockMappings.put(b, mapping);
		}
	}

	Function<EntityComponentContext, ?> getMapping(Entity entity) {
		return entityMappings.getOrDefault(entity.getType(), defaultEntityMapping);
	}

	@Override
	public void registerProvider(Function<EntityComponentContext, T> mapping, EntityType<?>... entities) {
		for (final EntityType<?> e : entities) {
			entityMappings.put(e, mapping);
		}
	}

	Function<ItemComponentContext, ?> getMapping(Item item) {
		return itemMappings.getOrDefault(item, defaultItemMapping);
	}

	@Override
	public void registerProvider(Function<ItemComponentContext, T> mapping, Item... items) {
		for (final Item i : items) {
			itemMappings.put(i, mapping);
		}
	}

	@Override
	public void registerAction(BiPredicate<ItemComponentContext, T> action, Item... items) {
		for (final Item i : items) {
			ObjectArrayList<BiPredicate<ItemComponentContext, T>> list = itemActions.get(i);

			if (list == null) {
				list = new ObjectArrayList<>(4);
				itemActions.put(i, list);
			}

			list.add(action);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public DeviceComponentAccess<T> getAccess(Level world, BlockPos pos) {
		return BlockComponentContextImpl.get(this, world, pos);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DeviceComponentAccess<T> getAccess(Level world, BlockPos pos, BlockState blockState) {
		return BlockComponentContextImpl.get(this, world, pos, blockState);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DeviceComponentAccess<T> getAccess(BlockEntity blockEntity) {
		return BlockComponentContextImpl.get(this, blockEntity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> DeviceComponentAccess<T> getAccess(E entity) {
		applyDeferredEntityRegistrations();
		return EntityComponentContextImpl.get(this, entity);
	}

	@Override
	public DeviceComponentAccess<T> getAbsentAccess() {
		return absentComponent;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DeviceComponentAccess<T> getAccessForHeldItem(Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, ServerPlayer player) {
		return ItemComponentContextImpl.get(this, stackGetter, stackSetter, player);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DeviceComponentAccess<T> getAccess(Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, Level world) {
		return ItemComponentContextImpl.get(this, stackGetter, stackSetter, world);
	}

	@Override
	public boolean applyActions(T target, Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, ServerPlayer player) {
		final ObjectArrayList<BiPredicate<ItemComponentContext, T>> actions = itemActions.get(stackGetter.get().getItem());

		if (actions != null && !actions.isEmpty()) {
			return applyActions(target, actions, ItemComponentContextImpl.get(this, stackGetter, stackSetter, player));
		}

		return false;
	}

	@Override
	public boolean applyActions(T target, Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, Level world) {
		final ObjectArrayList<BiPredicate<ItemComponentContext, T>> actions = itemActions.get(stackGetter.get().getItem());

		if (actions != null && !actions.isEmpty()) {
			return applyActions(target, actions, ItemComponentContextImpl.get(this, stackGetter, stackSetter, world));
		}

		return false;
	}

	private boolean applyActions(T target, ObjectArrayList<BiPredicate<ItemComponentContext, T>> actions, ItemComponentContext ctx) {
		for (final BiPredicate<ItemComponentContext, T> action : actions) {
			if (action.test(ctx, target)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void registerProvider(Function<EntityComponentContext, T> mapping, Predicate<EntityType<?>> predicate) {
		if (deferedEntityMappings == null) {
			deferedEntityMappings = new ObjectArrayList<>();
		}

		deferedEntityMappings.add(Pair.of(mapping, predicate));
	}

	private void applyDeferredEntityRegistrations() {
		if (deferedEntityMappings != null) {
			for (final Pair<Function<EntityComponentContext, T>, Predicate<EntityType<?>>> pair : deferedEntityMappings) {
				Registry.ENTITY_TYPE.forEach(e -> {
					if (pair.getRight().test(e)) {
						registerProvider(pair.getLeft(), e);
					}
				});
			}

			deferedEntityMappings = null;
		}
	}
}
