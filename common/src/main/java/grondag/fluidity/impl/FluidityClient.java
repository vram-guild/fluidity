package grondag.fluidity.impl;

import dev.architectury.networking.NetworkManager;

import grondag.fluidity.base.synch.BulkStorageClientDelegate;
import grondag.fluidity.base.synch.BulkStorageUpdateS2C;
import grondag.fluidity.base.synch.DiscreteStorageClientDelegate;
import grondag.fluidity.base.synch.DiscreteStorageUpdateS2C;

public abstract class FluidityClient {
	private FluidityClient() { }

	public static void initialize() {
		NetworkManager.registerReceiver(NetworkManager.s2c(), DiscreteStorageUpdateS2C.ID_FULL_REFRESH, DiscreteStorageClientDelegate.INSTANCE::handleFullRefresh);
		NetworkManager.registerReceiver(NetworkManager.s2c(), DiscreteStorageUpdateS2C.ID_UPDATE, DiscreteStorageClientDelegate.INSTANCE::handleUpdate);
		NetworkManager.registerReceiver(NetworkManager.s2c(), DiscreteStorageUpdateS2C.ID_UPDATE_WITH_CAPACITY, DiscreteStorageClientDelegate.INSTANCE::handleUpdateWithCapacity);

		NetworkManager.registerReceiver(NetworkManager.s2c(), BulkStorageUpdateS2C.ID_FULL_REFRESH, BulkStorageClientDelegate.INSTANCE::handleFullRefresh);
		NetworkManager.registerReceiver(NetworkManager.s2c(), BulkStorageUpdateS2C.ID_UPDATE, BulkStorageClientDelegate.INSTANCE::handleUpdate);
		NetworkManager.registerReceiver(NetworkManager.s2c(), BulkStorageUpdateS2C.ID_UPDATE_WITH_CAPACITY, BulkStorageClientDelegate.INSTANCE::handleUpdateWithCapacity);
	}
}
