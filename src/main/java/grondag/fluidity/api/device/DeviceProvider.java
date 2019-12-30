package grondag.fluidity.api.device;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(status = Status.EXPERIMENTAL)
@FunctionalInterface
public interface DeviceProvider {
	Device getDevice();

	//	public interface InventoryProvider {
	//		   SidedInventory getInventory(BlockState blockState, IWorld iWorld, BlockPos blockPos);
	//		}

	static Device get(Object obj) {
		return obj instanceof DeviceProvider ? ((DeviceProvider)obj).getDevice() : Device.EMPTY;
	}
}
