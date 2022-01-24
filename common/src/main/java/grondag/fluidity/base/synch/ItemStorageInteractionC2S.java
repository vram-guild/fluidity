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

package grondag.fluidity.base.synch;

import dev.architectury.networking.NetworkManager.PacketContext;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.storage.Store;
import grondag.fluidity.impl.Fluidity;

/**
 * Sent when player interacts with the GUI of an IStorage (vs container slots).
 * IStorage has no concept of slots.
 */
@Experimental
public class ItemStorageInteractionC2S {
	public static final ResourceLocation ID = new ResourceLocation(Fluidity.MOD_ID, "posci");

	public static void accept(FriendlyByteBuf buf, PacketContext ctx) { //MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
		final ItemStorageAction action = buf.readEnum(ItemStorageAction.class);
		final int handle = buf.readInt();
		ctx.queue(() -> acceptInner(action, handle, (ServerPlayer) ctx.getPlayer()));
	}

	private static void acceptInner(ItemStorageAction action, int handle, ServerPlayer player) {
		if (player.containerMenu == null || !(player.containerMenu instanceof StorageContainer)) {
			return;
		}

		final Store storage = ((StorageContainer) player.containerMenu).getStorage();

		if (storage == null || !storage.isValid()) {
			return;
		}

		final Article targetResource = handle == -1 ? null : storage.view(handle).article();

		switch (action) {
			case PUT_ALL_HELD:
				doPut(false, player, storage);
				return;

			case PUT_ONE_HELD:
				doPut(true, player, storage);
				return;

			case QUICK_MOVE_HALF: {
				if (targetResource == null) {
					return;
				}

				final int toMove = (int) Math.max(1, Math.min(targetResource.toItem().getMaxStackSize() / 2, storage.countOf(targetResource)) / 2);
				doQuickMove(toMove, player, targetResource, storage);
				return;
			}

			case QUICK_MOVE_ONE:
				if (targetResource == null) {
					return;
				}

				doQuickMove(1, player, targetResource, storage);
				return;

			case QUICK_MOVE_STACK: {
				if (targetResource == null) {
					return;
				}

				final int toMove = (int) Math.min(targetResource.toItem().getMaxStackSize(), storage.countOf(targetResource));
				doQuickMove(toMove, player, targetResource, storage);
				return;
			}

			case TAKE_ONE:
				doTake(1, player, targetResource, storage);
				return;

			case TAKE_HALF: {
				if (targetResource == null) {
					return;
				}

				final int toTake = (int) Math.max(1, Math.min(targetResource.toItem().getMaxStackSize() / 2, storage.countOf(targetResource) / 2));
				doTake(toTake, player, targetResource, storage);
				return;
			}

			case TAKE_STACK: {
				if (targetResource == null) {
					return;
				}

				final int toTake = (int) Math.min(targetResource.toItem().getMaxStackSize(), storage.countOf(targetResource));
				doTake(toTake, player, targetResource, storage);
				return;
			}

			default:
		}
	}

	private static void doPut(boolean single, ServerPlayer player, Store container) {
		final ItemStack cursorStack = player.containerMenu.getCarried();

		if (cursorStack != null && !cursorStack.isEmpty()) {
			final int added = (int) container.getConsumer().apply(cursorStack, single ? 1 : cursorStack.getCount(), false);

			if (added > 0) {
				cursorStack.shrink(added);
				player.containerMenu.setCarried(cursorStack);
				player.getInventory().setChanged();
				player.containerMenu.sendAllDataToRemote();
			}
		}
	}

	private static void doQuickMove(int howMany, ServerPlayer player, Article targetResource, Store listener) {
		if (howMany == 0 || targetResource == null || targetResource.isNothing()) {
			return;
		}

		final int toMove = (int) listener.getSupplier().apply(targetResource, howMany, false);

		if (toMove == 0) {
			return;
		}

		final ItemStack newStack = targetResource.toStack(toMove);
		player.getInventory().placeItemBackInInventory(newStack);
		player.getInventory().setChanged();
	}

	private static void doTake(int howMany, ServerPlayer player, Article targetResource, Store container) {
		if (howMany == 0 || targetResource == null || targetResource.isNothing()) {
			return;
		}

		final ItemStack cursorStack = player.containerMenu.getCarried();

		if (cursorStack != null && !cursorStack.isEmpty()) {
			if (!targetResource.matches(cursorStack)) {
				return;
			}

			if (cursorStack.getCount() >= cursorStack.getMaxStackSize()) {
				return;
			}

			howMany = Math.min(howMany, cursorStack.getMaxStackSize() - cursorStack.getCount());
			final int toAdd = (int) container.getSupplier().apply(targetResource, howMany, false);
			cursorStack.grow(toAdd);
			player.containerMenu.setCarried(cursorStack);
			player.getInventory().setChanged();
			player.containerMenu.sendAllDataToRemote();
		} else {
			howMany = Math.min(howMany, targetResource.toItem().getMaxStackSize());

			final int toAdd = (int) container.getSupplier().apply(targetResource, howMany, false);

			if (toAdd == 0) {
				return;
			}

			final ItemStack newStack = targetResource.toStack(toAdd);
			player.containerMenu.setCarried(newStack);
			player.getInventory().setChanged();
			player.containerMenu.sendAllDataToRemote();
		}
	}
}
