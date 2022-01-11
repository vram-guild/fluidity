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
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus.Experimental;

/**
 * Describes and provides access to "device component" instances that may be retrieved
 * for blocks or items in the world. <p>
 *
 * This interface should never be implemented by mod authors. Create new instances
 * using {@link DeviceComponentRegistry#createComponent(net.minecraft.resources.ResourceLocation, Object)}.<p>
 *
 * @param <T> Type parameter identifying the {@code Class} of component instances of this component type
 *
 * @see <a href="https://github.com/grondag/fluidity#device-components">https://github.com/grondag/fluidity#device-components</a>
 */
@Experimental
public interface DeviceComponentType<T> {
	/**
	 * Component value that will be returned when a component is not present in a device.
	 *
	 * @return value to be returned when a component is not present in a device
	 */
	T absent();

	/**
	 * An automatically constructed, immutable and non-allocating {@code DeviceComponentAccess} instance
	 * that will always return the {@link #absent()} value.  Useful as default return value for access requests.
	 *
	 * @return an immutable, non-allocating {@code DeviceComponentAccess} instance that will always return {@link #absent()}
	 */
	DeviceComponentAccess<T> getAbsentAccess();

	/**
	 * Casts the input parameter to the component class associated with this component type.
	 *
	 * @param obj the object to be cast
	 * @return the input object cast to the component type
	 *
	 * @throws ClassCastException if the input object cannot be cast to the component class
	 */
	@SuppressWarnings("unchecked")
	default T cast(Object obj) {
		return (T) obj;
	}

	/**
	 * Retrieves a {@code DeviceComponentAccess} to access device components of this type
	 * that may be present at the given location. <p>
	 *
	 * The instance that is returned may be thread-local and should never be retained.<p>
	 *
	 * Note that {@link #getAccess(Level, BlockPos, BlockState)} may be more performant
	 * if 1) you know this component type requires block state and 2) the block state
	 * and the given position is already on the call stack.<p>
	 *
	 * @param world the server world where the device component may be located
	 * @param pos the block position where the device component may be located
	 * @return a {@code DeviceComponentAccess} to access device components of this type
	 * that may be present at the given location
	 */
	DeviceComponentAccess<T> getAccess(Level world, BlockPos pos);

	/**
	 * Retrieves a {@code DeviceComponentAccess} to access device components of this type
	 * that may be present at the given location. <p>
	 *
	 * The instance that is returned may be thread-local and should never be retained.
	 *
	 * @param world the server world where the device component may be located
	 * @param pos the block position where the device component may be located
	 * @param blockState the current block state at the given position within the world
	 * @return a {@code DeviceComponentAccess} to access device components of this type
	 * that may be present at the given location
	 */
	DeviceComponentAccess<T>  getAccess(Level world, BlockPos pos, BlockState blockState);

	/**
	 * Retrieves a {@code DeviceComponentAccess} to access device components of this type
	 * that may be present at the given location. <p>
	 *
	 * The instance that is returned may be thread-local and should never be retained.
	 *
	 * @param world the server world where the device component may be located
	 * @param pos the block position where the device component may be located
	 * @param blockState the current block state at the given position within the world
	 * @return a {@code DeviceComponentAccess} to access device components of this type
	 * that may be present at the given location
	 */
	DeviceComponentAccess<T> getAccess(BlockEntity blockEntity);

	/**
	 * Retrieves a {@code DeviceComponentAccess} to access device components of this type
	 * that may be present in the given entity.<p>
	 *
	 * The instance that is returned may be thread-local and should never be retained.
	 *
	 * @param <E>  concrete type of the entity
	 * @param entity entity to provide component access if available
	 * @return a {@code DeviceComponentAccess} to access device components of this type
	 * that may be present in the given entity
	 */
	<E extends Entity> DeviceComponentAccess<T> getAccess(E entity);

	/**
	 * Retrieves a {@code DeviceComponentAccess} to access device components of this type
	 * that may be present in the held in the main hand of the given player. <p>
	 *
	 * The instance that is returned may be thread-local and should never be retained.
	 *
	 * @param player the player that may be holding an item containing a device component of this type
	 * @return a {@code DeviceComponentAccess} to access device components of this type
	 * that may be present in the held item
	 */
	default DeviceComponentAccess<T> getAccessForHeldItem(ServerPlayer player) {
		return getAccessForHeldItem(() -> player.getMainHandItem(), s -> player.setItemInHand(InteractionHand.MAIN_HAND, s), player);
	}

	/**
	 * Retrieves a {@code DeviceComponentAccess} to access device components of this type
	 * that may be present in an item stack held by a player. <p>
	 *
	 * The instance that is returned may be thread-local and should never be retained.
	 *
	 * @param stackGetter function that will be be used to retrieve item stack for component state
	 * @param stackSetter function that will be be used to persist item stack for component state
	 * @param player the player that is holding the item stack
	 * @return a {@code DeviceComponentAccess} to access device components of this type
	 * that may be present in the item stack
	 */
	DeviceComponentAccess<T> getAccessForHeldItem(Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, ServerPlayer player);

	/**
	 * Retrieves a {@code DeviceComponentAccess} to access device components of this type
	 * that may be present in an item stack not held by a player. <p>
	 *
	 * The instance that is returned may be thread-local and should never be retained.
	 *
	 * @param stackGetter function that will be be used to retrieve item stack for component state
	 * @param stackSetter function that will be be used to persist item stack for component state
	 * @param world the world where the item stack is located
	 * @return a {@code DeviceComponentAccess} to access device components of this type
	 * that may be present in the item stack
	 */
	DeviceComponentAccess<T> getAccess(Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, Level world);

	/**
	 * Applies registered item actions to a component instance of this type until one is successful.
	 * Returns true if one action was successful, false if no actions were found or none succeeded.
	 *
	 * @param target the component instance to which actions will be applied
	 * @param player the player using an item on the component
	 * @return {@code true} if one action was successfully applied
	 *
	 * @see <a href="https://github.com/grondag/fluidity#item-actions">https://github.com/grondag/fluidity#item-actions</a>
	 */
	default boolean applyActionsWithHeld(T target, ServerPlayer player) {
		return applyActions(target, () -> player.getMainHandItem(), s -> player.setItemInHand(InteractionHand.MAIN_HAND, s), player);
	}

	/**
	 * Applies registered item actions to a component instance of this type until one is successful.
	 * Returns true if one action was successful, false if no actions were found or none succeeded.
	 *
	 * @param target the component instance to which actions will be applied
	 * @param stackGetter function to retrieve the item stack that is being used
	 * @param stackSetter function to update the item stack that is being used
	 * @param player the player using an item on the component
	 * @return {@code true} if one action was successfully applied
	 *
	 * @see <a href="https://github.com/grondag/fluidity#item-actions">https://github.com/grondag/fluidity#item-actions</a>
	 */
	boolean applyActions(T target, Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, ServerPlayer player);

	/**
	 * Applies registered item actions to a component instance of this type until one is successful.
	 * Returns true if one action was successful, false if no actions were found or none succeeded.<p>
	 *
	 * This version is used when the item stack is not held by a player.
	 *
	 * @param target the component instance to which actions will be applied
	 * @param stackGetter function to retrieve the item stack that is being used
	 * @param stackSetter function to update the item stack that is being used
	 * @param world the world in which the item stack and target are located
	 * @return {@code true} if one action was successfully applied
	 *
	 * @see <a href="https://github.com/grondag/fluidity#item-actions">https://github.com/grondag/fluidity#item-actions</a>
	 */
	boolean applyActions(T target, Supplier<ItemStack> stackGetter, Consumer<ItemStack> stackSetter, Level world);

	/**
	 * Causes the given blocks to provide device component instances of this type
	 * by application of the given mapping function. Use this version for blocks that
	 * may provide a component without the presence of a {@code BlockEntity}.<p>
	 *
	 * The mapping function should return {@link #absent()} if no component is available.<p>
	 *
	 * @param mapping function that derives a component instance from an access context
	 * @param blocks one or more blocks for which the function will apply
	 */
	void registerProvider(Function<BlockComponentContext, T> mapping, Block... blocks);

	/**
	 * Causes the given blocks to provide device component instances of this type
	 * via block entities associated with the given blocks.<p>
	 *
	 * Use this version for blocks where the {@code BlockEntity} *is* the component instance.<p>
	 *
	 * @param blocks one or more blocks that will provide components in this way
	 */
	@SuppressWarnings("unchecked")
	default void addProvider(Block... blocks) {
		registerProvider(ctx -> (T) ctx.blockEntity(), blocks);
	}

	/**
	 * Causes the given entity types to provide device component instances of this type
	 * by application of the given mapping function.<p>
	 *
	 * This will override any previous mapping of the same component type and only one
	 * result per entity is possible.  For the reason, mod authors are advised to create
	 * distinct component types for their use cases instead of using standard component types.<p>
	 *
	 * @param mapping mapping function that derives a component instance from an access context
	 * @param entities one or more entities for which the function will apply
	 */
	void registerProvider(Function<EntityComponentContext, T> mapping, EntityType<?>... entities);

	/**
	 * Same as {@link #registerProvider(Function, EntityType...)} but matches all entities that
	 * match the given predicate instead of providing specific entity types.  Use this to register
	 * a provider for all entities that implement {@code LivingEntity}, for example. <p>
	 *
	 * This method may be called at any point during mod initialization (and not after) but the
	 * predicate will only be applied after all registration is complete. Mod registration
	 * order does not matter.<p>
	 *
	 * @param mapping mapping function that derives a component instance from an access context
	 * @param predicate Mapping will apply to all entity types that match this test
	 */
	void registerProvider(Function<EntityComponentContext, T> mapping, Predicate<EntityType<?>> predicate);

	/**
	 * Causes the given items to provide device component instances of this type
	 * by application of the given mapping function to item stacks having the given items.<p>
	 *
	 * The mapping function should return {@link #absent()} if no component is available.
	 *
	 * @param mapping mapping function that derives a component instance from an access context
	 * @param items one or more items for which the function will apply
	 */
	void registerProvider(Function<ItemComponentContext, T> mapping, Item... items);

	/**
	 * Adds an function that will be applied when one of the given items is used
	 * on a device component of this type.  Application is not automatic - implementations
	 * must call {@link #applyActions()} or {@link #applyActionsWithHeld()} in response
	 * to game events.
	 *
	 * @param action Function to be applied, must return {@code true} if the action was successful and no more actions should be tried.
	 * @param items One or more items to which this action will be applied
	 */
	void registerAction(BiPredicate<ItemComponentContext, T> action, Item... items);
}
