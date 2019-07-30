package grondag.fluidity;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.fluids.v1.transact.FluidTx;

public class Fluidity implements ModInitializer {
	@Override
	public void onInitialize() {
	   ServerStartCallback.EVENT.register(FluidTx::setThread);
	}
}
