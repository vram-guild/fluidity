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
		return player == null ? null : player.level;
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
	private ItemComponentContextImpl  prepare(DeviceComponentTypeImpl componentType, Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, ServerPlayer player) {
		this.componentType = componentType;
		this.stackSetter = stackSetter;
		this.stackGetter = stackGetter;
		this.player = player;
		world = player.level;
		mapping = componentType.getMapping(stackGetter.get().getItem());
		return this;
	}

	private static final ThreadLocal<ItemComponentContextImpl> POOL = ThreadLocal.withInitial(ItemComponentContextImpl::new);

	static ItemComponentContextImpl get(DeviceComponentTypeImpl componentType, Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, Level world) {
		return POOL.get().prepare(componentType, stackGetter, stackSetter, world);
	}

	static ItemComponentContextImpl  get(DeviceComponentTypeImpl componentType, Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, ServerPlayer player) {
		return POOL.get().prepare(componentType, stackGetter, stackSetter, player);
	}
}
