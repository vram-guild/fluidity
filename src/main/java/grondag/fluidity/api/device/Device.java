package grondag.fluidity.api.device;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.storage.Storage;

@API(status = Status.EXPERIMENTAL)
public interface Device {
	default @Nullable Storage getStorage() {
		return null;
	}

	default boolean hasStorage() {
		return getStorage() != null;
	}

	default Storage getStorage(Object connection) {
		return getStorage();
	}

	/**
	 * Component members may elect to return the compound storage instance from calls to
	 * {@link StorageDevice#getStorage()}. This method offers an unambiguous way to
	 * reference the storage of this component device specifically.
	 *
	 * <p>Also used by and necessary for aggregate storage implementations for the same reason.
	 *
	 * @return {@link Storage} of this compound member device.
	 */
	default Storage getLocalStorage() {
		return getStorage();
	}

	default @Nullable Location getLocation() {
		return null;
	}

	default boolean hasLocation() {
		return getLocation() != null;
	}
}
