package grondag.fluidity.api.device;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import grondag.fluidity.api.storage.Storage;

@API(status = Status.EXPERIMENTAL)
@FunctionalInterface
public interface StorageProvider {
	default @Nullable Storage getStorage() {
		return getStorage(null, null);
	}

	default boolean hasStorage() {
		return getStorage(null, null) != Storage.EMPTY;
	}

	default boolean hasStorage(@Nullable Direction side) {
		return getStorage(side, null) != Storage.EMPTY;
	}

	default @Nullable Storage getStorage(@Nullable Direction side) {
		return getStorage(side, null);
	}

	default boolean hasStorage(@Nullable Identifier id) {
		return getStorage(null, id) != Storage.EMPTY;
	}

	default @Nullable Storage getStorage(@Nullable Identifier id) {
		return getStorage(null, id);
	}

	default boolean hasStorage(@Nullable Direction side, @Nullable Identifier id) {
		return getStorage(side, id) != Storage.EMPTY;
	}

	Storage getStorage(@Nullable Direction side, @Nullable Identifier id);

	default boolean isStorageSided() {
		return false;
	}

	default boolean isStorageIdentified() {
		return false;
	}

	default boolean hasStorage(PlayerEntity player) {
		return getStorage(player) != null;
	}

	default @Nullable Storage getStorage(PlayerEntity player) {
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
		return getLocalStorage(null, null);
	}

	default Storage getLocalStorage(@Nullable Direction side, @Nullable Identifier id) {
		return getStorage(side, id);
	}

	default @Nullable Storage getLocalStorage(@Nullable Direction side) {
		return getLocalStorage(side, null);
	}

	default @Nullable Storage getLocalStorage(@Nullable Identifier id) {
		return getLocalStorage(null, id);
	}

	StorageProvider EMPTY = (d, i) -> Storage.EMPTY;
	StorageProvider VOID = (d, i) -> Storage.VOID;
	StorageProvider CREATIVE = (d, i) -> Storage.CREATIVE;
}
