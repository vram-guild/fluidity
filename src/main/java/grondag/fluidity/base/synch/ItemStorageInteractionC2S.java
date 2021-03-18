/*******************************************************************************
 * Copyright 2019, 2020 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.fluidity.base.synch;

import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

import grondag.fluidity.Fluidity;
import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.storage.Store;

/**
 * Sent when player interacts with the GUI of an IStorage (vs container slots).
 * IStorage has no concept of slots.
 */
@Experimental
public class ItemStorageInteractionC2S {
	public static final Identifier ID = new Identifier(Fluidity.MOD_ID, "posci");

	@Environment(EnvType.CLIENT)
	public static void sendPacket(ItemStorageAction action, DiscreteDisplayDelegate target) {
		if (MinecraftClient.getInstance().getNetworkHandler() != null) {
			final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeEnumConstant(action);
			buf.writeInt(target == null ? -1 : target.handle());
			ClientPlayNetworking.send(ID, buf);
		}
	}

	public static void accept(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		final ItemStorageAction action = buf.readEnumConstant(ItemStorageAction.class);
		final int handle = buf.readInt();

		if (server.isOnThread()) {
			acceptInner(action, handle, player);
		} else {
			server.execute(() -> acceptInner(action, handle, player));
		}
	}

	private static void acceptInner(ItemStorageAction action, int handle, ServerPlayerEntity player) {
		if (player.currentScreenHandler == null || !(player.currentScreenHandler instanceof StorageContainer)) {
			return;
		}

		final Store storage = ((StorageContainer) player.currentScreenHandler).getStorage();

		if(storage == null || !storage.isValid()) {
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

			final int toMove = (int) Math.max(1, Math.min(targetResource.toItem().getMaxCount() / 2, storage.countOf(targetResource)) / 2);
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

			final int toMove = (int) Math.min(targetResource.toItem().getMaxCount(), storage.countOf(targetResource));
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

			final int toTake = (int) Math.max(1, Math.min(targetResource.toItem().getMaxCount() / 2, storage.countOf(targetResource) / 2));
			doTake(toTake, player, targetResource, storage);
			return;
		}

		case TAKE_STACK: {
			if (targetResource == null) {
				return;
			}

			final int toTake = (int) Math.min(targetResource.toItem().getMaxCount(), storage.countOf(targetResource));
			doTake(toTake, player, targetResource, storage);
			return;
		}

		default:
			return;
		}
	}

	private static void doPut(boolean single, ServerPlayerEntity player, Store container) {
		final ItemStack cursorStack = player.currentScreenHandler.getCursorStack();

		if (cursorStack != null && !cursorStack.isEmpty()) {
			final int added = (int) container.getConsumer().apply(cursorStack, single ? 1 : cursorStack.getCount(), false);

			if (added > 0){
				cursorStack.decrement(added);
				player.currentScreenHandler.setCursorStack(cursorStack);
				player.getInventory().markDirty();
				player.currentScreenHandler.syncState();
			}
		}
		return;
	}

	private static void doQuickMove(int howMany, ServerPlayerEntity player, Article targetResource, Store listener) {
		if (howMany == 0 || targetResource == null || targetResource.isNothing()) {
			return;
		}

		final int toMove = (int) listener.getSupplier().apply(targetResource, howMany, false);

		if (toMove == 0) {
			return;
		}

		final ItemStack newStack = targetResource.toStack(toMove);
		player.getInventory().offerOrDrop(newStack);
		player.getInventory().markDirty();
	}

	private static void doTake(int howMany, ServerPlayerEntity player, Article targetResource, Store container) {
		if (howMany == 0 || targetResource == null || targetResource.isNothing()) {
			return;
		}

		final ItemStack cursorStack = player.currentScreenHandler.getCursorStack();

		if (cursorStack != null && !cursorStack.isEmpty()) {
			if (!targetResource.matches(cursorStack)) {
				return;
			}

			if (cursorStack.getCount() >= cursorStack.getMaxCount()) {
				return;
			}

			howMany = Math.min(howMany, cursorStack.getMaxCount() - cursorStack.getCount());
			final int toAdd = (int) container.getSupplier().apply(targetResource, howMany, false);
			cursorStack.increment(toAdd);
			player.currentScreenHandler.setCursorStack(cursorStack);
			player.getInventory().markDirty();
			player.currentScreenHandler.syncState();
		} else {
			howMany = Math.min(howMany, targetResource.toItem().getMaxCount());

			final int toAdd = (int) container.getSupplier().apply(targetResource, howMany, false);

			if (toAdd == 0) {
				return;
			}

			final ItemStack newStack = targetResource.toStack(toAdd);
			player.currentScreenHandler.setCursorStack(newStack);
			player.getInventory().markDirty();
			player.currentScreenHandler.syncState();
		}

	}
}
