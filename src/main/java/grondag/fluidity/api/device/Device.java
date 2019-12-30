package grondag.fluidity.api.device;

import javax.annotation.Nullable;

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

	default @Nullable Location getLocation() {
		return Location.NOWHERE;
	}

	default boolean hasLocation() {
		return getLocation() != Location.NOWHERE;
	}

	Device EMPTY = new Device() {};
}
