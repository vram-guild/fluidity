package grondag.fluidity.api.device;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(status = Status.EXPERIMENTAL)
public interface Device {
	default StorageProvider getStorageProvider() {
		return StorageProvider.EMPTY;
	}

	default boolean  hasStorage() {
		return getStorageProvider() != StorageProvider.EMPTY;
	}

	Device EMPTY = new Device() {};
}
