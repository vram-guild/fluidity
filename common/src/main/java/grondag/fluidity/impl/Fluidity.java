package grondag.fluidity.impl;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;

import grondag.fluidity.api.device.ItemActionHelper;
import grondag.fluidity.base.synch.ItemStorageInteractionC2S;
import grondag.fluidity.impl.article.ArticleTypeImpl;

public abstract class Fluidity {
	private Fluidity() { }

	public static final String MOD_ID = "fluidity";
	public static final Logger LOG = LogManager.getLogger("Fluidity");

	public static void trace(String message, Object... args) {
		LOG.info(String.format("[Fluidity] " + message, args));
	}

	public static void initialize() {
		FluidityConfig.init();
		ArticleTypeImpl.init();

		TickEvent.SERVER_POST.register(MultiBlockManagerImpl::tick);

		LifecycleEvent.SERVER_STARTED.register(s -> {
			TransactionImpl.setServerThread(s);
			MultiBlockManagerImpl.start(s);
		});

		NetworkManager.registerReceiver(NetworkManager.c2s(), ItemStorageInteractionC2S.ID, ItemStorageInteractionC2S::accept);

		ItemActionHelper.addPotionActions(Fluids.WATER, Potions.WATER);
		ItemActionHelper.addItemActions(Fluids.WATER, Items.BUCKET, Items.WATER_BUCKET);
		ItemActionHelper.addItemActions(Fluids.LAVA, Items.BUCKET, Items.LAVA_BUCKET);
	}
}
