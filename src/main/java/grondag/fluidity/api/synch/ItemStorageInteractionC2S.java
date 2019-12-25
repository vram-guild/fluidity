/*******************************************************************************
 * Copyright 2019 grondag
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
package grondag.fluidity.api.synch;

import io.netty.buffer.Unpooled;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;

import grondag.fluidity.Fluidity;
import grondag.fluidity.api.item.CommonItem;
import grondag.fluidity.api.storage.CommonStorage;
import grondag.fluidity.api.storage.DiscreteStorage;
import grondag.fluidity.api.storage.StorageSupplier;

/**
 * Sent when player interacts with the GUI of an IStorage (vs container slots).
 * IStorage has no concept of slots.
 */
@API(status = Status.EXPERIMENTAL)
public class ItemStorageInteractionC2S {
	public static final Identifier ID = new Identifier(Fluidity.MOD_ID, "posci");

	@Environment(EnvType.CLIENT)
	public static void sendPacket(StorageAction action, ItemDisplayDelegate target) {
		final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeEnumConstant(action);
		buf.writeInt(target == null ? -1 : target.handle());
		ClientSidePacketRegistry.INSTANCE.sendToServer(ID, buf);
	}

	public static void accept(PacketContext context, PacketByteBuf buf) {
		final StorageAction action = buf.readEnumConstant(StorageAction.class);
		final int handle = buf.readInt();
		final ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();

		if (context.getTaskQueue().isOnThread()) {
			acceptInner(action, handle, player);
		} else {
			context.getTaskQueue().execute(() -> acceptInner(action, handle, player));
		}
	}

	private static void acceptInner(StorageAction action, int handle, ServerPlayerEntity player) {
		if (player.container == null || !(player.container instanceof StorageSupplier)) {
			return;
		}

		final CommonStorage storage = ((StorageSupplier) player.container).getStorage();
		final CommonItem targetResource = handle == -1 ? null : storage.view(handle).item();

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

			final int toMove = (int) Math.max(1, Math.min(targetResource.getItem().getMaxCount() / 2, storage.countOf(targetResource)) / 2);
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

			final int toMove = (int) Math.min(targetResource.getItem().getMaxCount(), storage.countOf(targetResource));
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

			final int toTake = (int) Math.max(1, Math.min(targetResource.getItem().getMaxCount() / 2, storage.countOf(targetResource) / 2));
			doTake(toTake, player, targetResource, storage);
			return;
		}

		case TAKE_STACK: {
			if (targetResource == null) {
				return;
			}

			final int toTake = (int) Math.min(targetResource.getItem().getMaxCount(), storage.countOf(targetResource));
			doTake(toTake, player, targetResource, storage);
			return;
		}

		default:
			return;
		}
	}

	private static void doPut(boolean single, ServerPlayerEntity player, CommonStorage container) {
		final ItemStack cursorStack = player.inventory.getCursorStack();

		if (cursorStack != null && !cursorStack.isEmpty()) {
			final int added = (int) container.accept(cursorStack, single ? 1 : cursorStack.getCount(), false);

			if (added > 0){
				cursorStack.decrement(added);
				player.inventory.setCursorStack(cursorStack);
				player.inventory.markDirty();
				player.method_14241();
			}
		}
		return;
	}

	private static void doQuickMove(int howMany, ServerPlayerEntity player, CommonItem targetResource, CommonStorage listener) {
		if (howMany == 0 || targetResource == null || targetResource.isEmpty()) {
			return;
		}

		final int toMove = (int) listener.supply(targetResource, howMany, false);

		if (toMove == 0) {
			return;
		}

		final ItemStack newStack = targetResource.toStack(toMove);
		player.inventory.offerOrDrop(player.world, newStack);
		player.inventory.markDirty();
	}

	private static void doTake(int howMany, ServerPlayerEntity player, CommonItem targetResource, DiscreteStorage container) {
		if (howMany == 0 || targetResource == null || targetResource.isEmpty()) {
			return;
		}

		final ItemStack cursorStack = player.inventory.getCursorStack();

		if (cursorStack != null && !cursorStack.isEmpty()) {
			if (!targetResource.matches(cursorStack)) {
				return;
			}

			if (cursorStack.getCount() >= cursorStack.getMaxCount()) {
				return;
			}

			howMany = Math.min(howMany, cursorStack.getMaxCount() - cursorStack.getCount());
			final int toAdd = (int) container.supply(targetResource, howMany, false);
			cursorStack.increment(toAdd);
			player.inventory.setCursorStack(cursorStack);
			player.inventory.markDirty();
			player.method_14241();
		} else {
			howMany = Math.min(howMany, targetResource.getItem().getMaxCount());

			final int toAdd = (int) container.supply(targetResource, howMany, false);

			if (toAdd == 0) {
				return;
			}

			final ItemStack newStack = targetResource.toStack(toAdd);
			player.inventory.setCursorStack(newStack);
			player.inventory.markDirty();
			player.method_14241();
		}

	}
}
