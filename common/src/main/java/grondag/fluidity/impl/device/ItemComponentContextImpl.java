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

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import grondag.fluidity.api.device.ItemComponentContext;

@SuppressWarnings("rawtypes")
public final class ItemComponentContextImpl extends AbstractComponentContextImpl implements ItemComponentContext {
	private Supplier<ItemStack> stackGetter;
	private Consumer<ItemStack> stackSetter;
	private ServerPlayer player;

	@Override
	public Supplier<ItemStack> stackGetter() {
		return stackGetter;
	}

	@Override
	public Consumer<ItemStack> stackSetter() {
		return stackSetter;
	}

	@Override
	public ServerPlayer player() {
		return player;
	}

	@Override
	protected Level getWorldLazily() {
		return player == null ? null : player.level();
	}

	@SuppressWarnings("unchecked")
	private ItemComponentContextImpl prepare(DeviceComponentTypeImpl componentType, Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, Level world) {
		this.componentType = componentType;
		this.stackSetter = stackSetter;
		this.stackGetter = stackGetter;
		this.world = world;
		player = null;
		mapping = componentType.getMapping(stackGetter.get().getItem());
		return this;
	}

	@SuppressWarnings("unchecked")
	private ItemComponentContextImpl prepare(DeviceComponentTypeImpl componentType, Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, ServerPlayer player) {
		this.componentType = componentType;
		this.stackSetter = stackSetter;
		this.stackGetter = stackGetter;
		this.player = player;
		world = player.level();
		mapping = componentType.getMapping(stackGetter.get().getItem());
		return this;
	}

	private static final ThreadLocal<ItemComponentContextImpl> POOL = ThreadLocal.withInitial(ItemComponentContextImpl::new);

	static ItemComponentContextImpl get(DeviceComponentTypeImpl componentType, Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, Level world) {
		return POOL.get().prepare(componentType, stackGetter, stackSetter, world);
	}

	static ItemComponentContextImpl get(DeviceComponentTypeImpl componentType, Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, ServerPlayer player) {
		return POOL.get().prepare(componentType, stackGetter, stackSetter, player);
	}
}
