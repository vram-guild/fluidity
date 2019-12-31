package grondag.fluidity.api.device;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@FunctionalInterface
public interface ComponentType<T> {
	default @Nullable T get(Object fromObject, Authorization auth, @Nullable Direction side, @Nullable Identifier id) {
		return fromObject instanceof Device ? ((Device) fromObject).getComponent(this, auth, side, id) : absent();
	}

	T absent();

	@SuppressWarnings("unchecked")
	default T cast(Object obj) {
		return (T) obj;
	}

	default @Nullable T get(Object fromObject, @Nullable Direction side, @Nullable Identifier id) {
		return get(fromObject, Authorization.PUBLIC, side, id);
	}

	default @Nullable T get(Object fromObject, @Nullable Direction side) {
		return get(fromObject, Authorization.PUBLIC, side, null);
	}

	default @Nullable T get(Object fromObject, @Nullable Identifier id) {
		return get(fromObject, Authorization.PUBLIC, null, id);
	}

	default @Nullable T get(Object fromObject, Authorization auth) {
		return get(fromObject, auth, null, null);
	}

	default @Nullable T get(World world, BlockPos pos, Authorization auth, @Nullable Direction side, @Nullable Identifier id) {
		return get(world.getBlockEntity(pos), auth, side, id);
	}

	default @Nullable T get(World world, BlockPos pos, @Nullable Direction side, @Nullable Identifier id) {
		return get(world, pos, Authorization.PUBLIC, side, id);
	}

	default @Nullable T get(World world, BlockPos pos, @Nullable Direction side) {
		return get(world, pos, Authorization.PUBLIC, side, null);
	}

	default @Nullable T get(World world, BlockPos pos, Authorization auth) {
		return get(world, pos, auth, null, null);
	}

	default @Nullable T get(World world, BlockPos pos, @Nullable Identifier id) {
		return get(world, pos, Authorization.PUBLIC, null, id);
	}

	default @Nullable T get(World world, BlockPos pos) {
		return get(world, pos, Authorization.PUBLIC, null, null);
	}

	default boolean acceptIfPresent(Object fromObject, Authorization auth, @Nullable Direction side, @Nullable Identifier id, Consumer<T> action) {
		final T svc = get(fromObject, auth, side, id);

		if(svc != absent()) {
			action.accept(svc);
			return true;
		}

		return false;
	}

	default boolean acceptIfPresent(Object fromObject, @Nullable Direction side, @Nullable Identifier id, Consumer<T> action) {
		return acceptIfPresent(fromObject, Authorization.PUBLIC, side, id, action);
	}

	default boolean acceptIfPresent(Object fromObject, @Nullable Direction side, Consumer<T> action) {
		return acceptIfPresent(fromObject, Authorization.PUBLIC, side, null, action);
	}

	default boolean acceptIfPresent(Object fromObject, @Nullable Identifier id, Consumer<T> action) {
		return acceptIfPresent(fromObject, Authorization.PUBLIC, null, id, action);
	}

	default boolean acceptIfPresent(Object fromObject, Authorization auth, Consumer<T> action) {
		return acceptIfPresent(fromObject, auth, null, null, action);
	}

	default boolean acceptIfPresent(Object fromObject, Consumer<T> action) {
		return acceptIfPresent(fromObject, Authorization.PUBLIC, null, null, action);
	}

	default boolean acceptIfPresent(World world, BlockPos pos, Authorization auth, @Nullable Direction side, @Nullable Identifier id, Consumer<T> action) {
		return acceptIfPresent(world.getBlockEntity(pos), auth, side, id, action);
	}

	default boolean acceptIfPresent(World world, BlockPos pos, @Nullable Direction side, @Nullable Identifier id, Consumer<T> action) {
		return acceptIfPresent(world, pos, Authorization.PUBLIC, side, id, action);
	}

	default boolean acceptIfPresent(World world, BlockPos pos, @Nullable Direction side, Consumer<T> action) {
		return acceptIfPresent(world, pos, Authorization.PUBLIC, side, null, action);
	}

	default boolean acceptIfPresent(World world, BlockPos pos, Authorization auth, Consumer<T> action) {
		return acceptIfPresent(world, pos, auth, null, null, action);
	}

	default boolean acceptIfPresent(World world, BlockPos pos, @Nullable Identifier id, Consumer<T> action) {
		return acceptIfPresent(world, pos, Authorization.PUBLIC, null, id, action);
	}

	default boolean acceptIfPresent(World world, BlockPos pos, Consumer<T> action) {
		return acceptIfPresent(world, pos, Authorization.PUBLIC, null, null, action);
	}

	default <V> V applyIfPresent(Object fromObject, Authorization auth, @Nullable Direction side, @Nullable Identifier id, Function<T, V> function) {
		final T svc = get(fromObject, auth, side, id);

		if(svc != absent()) {
			return function.apply(svc);
		}

		return null;
	}

	default <V> V applyIfPresent(Object fromObject, @Nullable Direction side, @Nullable Identifier id, Function<T, V> function) {
		return applyIfPresent(fromObject, Authorization.PUBLIC, side, id, function);
	}

	default <V> V applyIfPresent(Object fromObject, @Nullable Direction side, Function<T, V> function) {
		return applyIfPresent(fromObject, Authorization.PUBLIC, side, null, function);
	}

	default <V> V applyIfPresent(Object fromObject, @Nullable Identifier id, Function<T, V> function) {
		return applyIfPresent(fromObject, Authorization.PUBLIC, null, id, function);
	}

	default <V> V applyIfPresent(Object fromObject, Authorization auth, Function<T, V> function) {
		return applyIfPresent(fromObject, auth, null, null, function);
	}

	default <V> V applyIfPresent(World world, BlockPos pos, Authorization auth, @Nullable Direction side, @Nullable Identifier id, Function<T, V> function) {
		return applyIfPresent(world.getBlockEntity(pos), auth, side, id, function);
	}

	default <V> V applyIfPresent(World world, BlockPos pos, @Nullable Direction side, @Nullable Identifier id, Function<T, V> function) {
		return applyIfPresent(world, pos, Authorization.PUBLIC, side, id, function);
	}

	default <V> V applyIfPresent(World world, BlockPos pos, @Nullable Direction side, Function<T, V> function) {
		return applyIfPresent(world, pos, Authorization.PUBLIC, side, null, function);
	}

	default <V> V applyIfPresent(World world, BlockPos pos, Authorization auth, Function<T, V> function) {
		return applyIfPresent(world, pos, auth, null, null, function);
	}

	default <V> V applyIfPresent(World world, BlockPos pos, @Nullable Identifier id, Function<T, V> function) {
		return applyIfPresent(world, pos, Authorization.PUBLIC, null, id, function);
	}

	default <V> V applyIfPresent(World world, BlockPos pos, Function<T, V> function) {
		return applyIfPresent(world, pos, Authorization.PUBLIC, null, null, function);
	}
}
