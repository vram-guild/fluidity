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

import net.minecraft.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;

import grondag.fluidity.Fluidity;
import grondag.fluidity.api.item.DiscreteItem;
import grondag.fluidity.api.storage.DiscreteStorage;
import grondag.fluidity.api.storage.DiscreteStorageSupplier;

/**
 * Sent when player interacts with the GUI of an IStorage (vs container slots).
 * IStorage has no concept of slots.
 */
@API(status = Status.EXPERIMENTAL)
public class OpenContainerStorageInteractionC2S {
	//TODO: these names are inaccurate
	public enum Action {
		/** move targeted stack to player's inventory */
		QUICK_MOVE_STACK,

		/** move half of targeted item, up to half a stack, to player's inventory */
		QUICK_MOVE_HALF,

		/** move one of targeted item to player's inventory */
		QUICK_MOVE_ONE,

		/** if player has an empty hand or holds the target item, add one to held */
		TAKE_ONE,

		/**
		 * if player has an empty hand, take half of targeted item, up to half a stack
		 */
		TAKE_HALF,

		/** if player has an empty hand, take full stack of targeted item */
		TAKE_STACK,

		/**
		 * if player holds a stack, deposit one of it into storage. target is
		 * ignored/can be null
		 */
		PUT_ONE_HELD,

		/**
		 * if player holds a stack, deposit all of it into storage. target is
		 * ignored/can be null
		 */
		PUT_ALL_HELD
	}

	public static final Identifier ID = new Identifier(Fluidity.MOD_ID, "posci");

	@Environment(EnvType.CLIENT)
	public static void sendPacket(Action action, ItemDisplayDelegate target) {
		final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeEnumConstant(action);
		buf.writeInt(target.handle());
		ClientSidePacketRegistry.INSTANCE.sendToServer(ID, buf);
	}

	public static void accept(PacketContext context, PacketByteBuf buf) {
		final Action action = buf.readEnumConstant(Action.class);
		final int resourceHandle = buf.readInt();
		final ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();

		if (player.container == null || !(player.container instanceof DiscreteStorageSupplier)) {
			return;
		}

		final DiscreteStorage container = ((DiscreteStorageSupplier) player.container).getDiscreteStorage();

		if(!container.isHandleValid(resourceHandle)) {
			return;
		}

		final DiscreteItem targetResource = container.view(resourceHandle).item();

		switch (action) {
		case PUT_ALL_HELD:
			doPut(false, player, container);
			return;

		case PUT_ONE_HELD:
			doPut(true, player, container);
			return;

		case QUICK_MOVE_HALF: {
			if (targetResource == null) {
				return;
			}

			final int toMove = (int) Math.max(1, Math.min(targetResource.getItem().getMaxCount() / 2, container.countOf(targetResource)) / 2);
			doQuickMove(toMove, player, targetResource, container);
			return;
		}

		case QUICK_MOVE_ONE:
			if (targetResource == null) {
				return;
			}
			doQuickMove(1, player, targetResource, container);
			return;

		case QUICK_MOVE_STACK: {
			if (targetResource == null) {
				return;
			}

			final int toMove = (int) Math.min(targetResource.getItem().getMaxCount(), container.countOf(targetResource));
			doQuickMove(toMove, player, targetResource, container);
			return;
		}

		case TAKE_ONE:
			doTake(1, player, targetResource, container);
			return;

		case TAKE_HALF: {
			if (targetResource == null) {
				return;
			}

			final int toTake = (int) Math.max(1, Math.min(targetResource.getItem().getMaxCount() / 2, container.countOf(targetResource) / 2));
			doTake(toTake, player, targetResource, container);
			return;
		}

		case TAKE_STACK: {
			if (targetResource == null) {
				return;
			}

			final int toTake = (int) Math.min(targetResource.getItem().getMaxCount(), container.countOf(targetResource));
			doTake(toTake, player, targetResource, container);
			return;
		}

		default:
			return;
		}
	}

	private static void doPut(boolean single, ServerPlayerEntity player, DiscreteStorage container) {
		final ItemStack heldStack = player.inventory.getMainHandStack();

		if (heldStack != null && !heldStack.isEmpty()) {
			final int added = (int) container.accept(heldStack, single ? 1 : heldStack.getCount(), false);

			if (added > 0)
			{
				heldStack.decrement(added);
				((Container) container).sendContentUpdates();
			}
		}
		return;
	}

	private static void doQuickMove(int howMany, ServerPlayerEntity player, DiscreteItem targetResource, DiscreteStorage listener) {
		if (howMany == 0) {
			return;
		}

		final int toMove = (int) listener.supply(targetResource, howMany, false);

		if (toMove == 0) {
			return;
		}

		final ItemStack newStack = targetResource.toStack(toMove);
		player.inventory.offerOrDrop(player.world, newStack);
	}

	/**
	 * Note: assumes player held item is empty and does not check for this.
	 */
	private static void doTake(int howMany, ServerPlayerEntity player, DiscreteItem targetResource, DiscreteStorage container) {
		if (howMany == 0) {
			return;
		}

		final ItemStack heldStack = player.inventory.getMainHandStack();

		if (heldStack != null && !heldStack.isEmpty()) {
			final boolean heldStackMatchesTarget = targetResource.matches(heldStack);

			if (!heldStackMatchesTarget) {
				return;
			}

			if (heldStack.getCount() >= heldStack.getMaxCount()) {
				return;
			}

			howMany = Math.min(howMany, heldStack.getMaxCount() - heldStack.getCount());
		} else {
			howMany = Math.min(howMany, targetResource.getItem().getMaxCount());
		}

		final int finalHowMany = howMany;

		final int toAdd = (int) container.supply(targetResource, finalHowMany, false);

		if (toAdd == 0) {
			return;
		}

		if (heldStack != null && !heldStack.isEmpty()) {
			heldStack.increment(toAdd);
			((Container) container).sendContentUpdates();
		} else {
			final ItemStack newStack = targetResource.toStack(toAdd);
			player.setStackInHand(Hand.MAIN_HAND, newStack);
		}
	}
}
