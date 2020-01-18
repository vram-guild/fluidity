package grondag.fluidity.impl.device;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import grondag.fluidity.api.device.Authorization;
import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentType;

public class AbsentDeviceComponent<T> implements DeviceComponentAccess<T> {
	protected final DeviceComponentType<T> componentType;

	AbsentDeviceComponent(DeviceComponentType<T> componentType) {
		this.componentType = componentType;
	}

	@Override
	public T get(Authorization auth, Direction side, Identifier id) {
		return componentType.absent();
	}

	@Override
	public DeviceComponentType<T> componentType() {
		return componentType;
	}
}
