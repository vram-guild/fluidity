package grondag.fluidity.impl.device;

import grondag.fluidity.api.device.Authorization;
import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class AbsentDeviceComponent<T> implements DeviceComponentAccess<T> {
	protected final DeviceComponentType<T> componentType;

	AbsentDeviceComponent(DeviceComponentType<T> componentType) {
		this.componentType = componentType;
	}

	@Override
	public T get(Authorization auth, Direction side, ResourceLocation id) {
		return componentType.absent();
	}

	@Override
	public DeviceComponentType<T> componentType() {
		return componentType;
	}
}
