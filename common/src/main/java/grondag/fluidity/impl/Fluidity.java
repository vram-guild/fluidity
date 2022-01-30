/*
 * This file is part of Fluidity and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
			TransactionImpl.setServerThread(s.getRunningThread());
			MultiBlockManagerImpl.start(s);
		});

		NetworkManager.registerReceiver(NetworkManager.c2s(), ItemStorageInteractionC2S.ID, ItemStorageInteractionC2S::accept);

		ItemActionHelper.addPotionActions(Fluids.WATER, Potions.WATER);
		ItemActionHelper.addItemActions(Fluids.WATER, Items.BUCKET, Items.WATER_BUCKET);
		ItemActionHelper.addItemActions(Fluids.LAVA, Items.BUCKET, Items.LAVA_BUCKET);
	}
}
