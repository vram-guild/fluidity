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
package grondag.fluidity.api.device;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@API(status = Status.EXPERIMENTAL)
public interface DeviceComponentType<T> {
	T absent();

	@SuppressWarnings("unchecked")
	default T cast(Object obj) {
		return (T) obj;
	}

	void addProvider(Function<BlockComponentContext, T> mapping, Block... blocks);

	@SuppressWarnings("unchecked")
	default void addProvider(Block... blocks) {
		addProvider(ctx -> (T) ctx.blockEntity(), blocks);
	}

	void addProvider(Function<ItemComponentContext, T> mapping, Item... items);

	void addAction(BiPredicate<ItemComponentContext, T> action, Item... items);

	DeviceComponent<T> get(World world, BlockPos pos);

	DeviceComponent<T>  get(World world, BlockPos pos, BlockState blockState);

	DeviceComponent<T> get(BlockEntity blockEntity);

	default DeviceComponent<T> getHeld(ServerPlayerEntity player) {
		return get(() -> player.getMainHandStack(), s -> player.setStackInHand(Hand.MAIN_HAND, s), player);
	}

	DeviceComponent<T> get(Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, ServerPlayerEntity player);

	/**
	 * Used when no player
	 * @param world
	 * @return
	 */
	DeviceComponent<T> get(Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, World world);

	default boolean applyActionsWithHeld(T target, ServerPlayerEntity player) {
		return applyActions(target, () -> player.getMainHandStack(), s -> player.setStackInHand(Hand.MAIN_HAND, s), player);
	}

	boolean applyActions(T target, Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, ServerPlayerEntity player);

	/**
	 * Used when no player
	 * @param world
	 * @return
	 */
	boolean applyActions(T target, Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, World world);


	DeviceComponent<T> getAbsent();
}
