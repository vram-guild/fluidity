package grondag.fluidity.api.device;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

@API(status = Status.EXPERIMENTAL)
@FunctionalInterface
public interface Device {
	@Nullable <T> T getComponent(ComponentType<T> componentType, Authorization auth, @Nullable Direction side, @Nullable Identifier id);

	@Nullable default <T> T getComponent(ComponentType<T> componentType, @Nullable Direction side, @Nullable Identifier id) {
		return getComponent(componentType, Authorization.PUBLIC, side, id);
	}

	@Nullable default <T> T getComponent(ComponentType<T> componentType,@Nullable  Direction side) {
		return getComponent(componentType, Authorization.PUBLIC, side, null);
	}

	@Nullable default <T> T getComponent(ComponentType<T> componentType) {
		return getComponent(componentType, Authorization.PUBLIC, null, null);
	}

	Device EMPTY = new Device() {
		@Override
		public <T> T getComponent(ComponentType<T> componentType, Authorization auth, Direction side, Identifier id) {
			return componentType.absent();
		}
	};
}
