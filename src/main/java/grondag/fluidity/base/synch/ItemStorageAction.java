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

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Possible interactions with remote storage. Distinct from vanilla container interactions.
 */
@API(status = Status.EXPERIMENTAL)
public enum ItemStorageAction {
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
	PUT_ALL_HELD;


	private static final int MOUSE_LEFT = 0;
	private static final int MOUSE_MIDDLE = 2;
	private static final int MOUSE_RIGHT = 1;

	@Environment(EnvType.CLIENT)
	public static ItemStorageAction select(int mouseButton, @Nullable DiscreteDisplayDelegate target) {
		ItemStorageAction result = null;

		final boolean isShift = Screen.hasShiftDown();

		final ItemStack cursorStack = MinecraftClient.getInstance().player.inventory.getCursorStack();

		// if alt/right/middle clicking on same item, don't count that as a deposit
		if (cursorStack != null && !cursorStack.isEmpty()
				&& !(target != null && ScreenHandler.canStacksCombine(cursorStack, target.article().toStack()) && (Screen.hasAltDown() || mouseButton > 0))) {

			// putting something in
			if (mouseButton == MOUSE_LEFT && !Screen.hasAltDown()) {
				result = ItemStorageAction.PUT_ALL_HELD;
			} else {
				result = ItemStorageAction.PUT_ONE_HELD;
			}
		} else if(target != null) {
			// taking something out
			if (mouseButton == MOUSE_LEFT && !Screen.hasAltDown()) {
				result = isShift ? ItemStorageAction.QUICK_MOVE_STACK : ItemStorageAction.TAKE_STACK;
			} else if (mouseButton == MOUSE_MIDDLE || Screen.hasAltDown()) {
				result = isShift ? ItemStorageAction.QUICK_MOVE_ONE : ItemStorageAction.TAKE_ONE;
			} else if (mouseButton == MOUSE_RIGHT) {
				result = isShift ? ItemStorageAction.QUICK_MOVE_HALF : ItemStorageAction.TAKE_HALF;
			}
		}

		return result;
	}

	@Environment(EnvType.CLIENT)
	public static boolean selectAndSend(int mouseButton, @Nullable DiscreteDisplayDelegate target) {
		final ItemStorageAction action = select(mouseButton, target);

		if (action != null) {
			ItemStorageInteractionC2S.sendPacket(action, target);
			return true;
		} else {
			return false;
		}
	}
}
