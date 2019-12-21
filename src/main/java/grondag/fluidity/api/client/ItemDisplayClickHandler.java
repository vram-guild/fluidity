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

package grondag.fluidity.api.client;

import javax.annotation.Nonnull;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.container.Container;
import net.minecraft.item.ItemStack;

import grondag.fluidity.api.client.OpenContainerStorageInteractionC2S.Action;

@API(status = Status.EXPERIMENTAL)
public class ItemDisplayClickHandler {
	public static final ItemDisplayClickHandler INSTANCE = new ItemDisplayClickHandler();
	public static final int MOUSE_LEFT = 0;
	public static final int MOUSE_MIDDLE = 2;
	public static final int MOUSE_RIGHT = 1;

	private ItemDisplayClickHandler() {
	}

	public void handle(MinecraftClient mc, int mouseButton, @Nonnull ItemDisplayDelegate target) {
		Action action = null;

		final boolean isShift = Screen.hasShiftDown();

		final ItemStack heldStack = mc.player.inventory.getMainHandStack();

		// if alt/right/middle clicking on same bulkResource, don't count that as a
		// deposit
		if (heldStack != null && !heldStack.isEmpty()
				&& !(Container.canStacksCombine(heldStack, target.displayStack()) && (Screen.hasAltDown() || mouseButton > 0))) {
			// putting something in
			if (mouseButton == MOUSE_LEFT && !Screen.hasAltDown()) {
				action = Action.PUT_ALL_HELD;
			} else {
				action = Action.PUT_ONE_HELD;
			}
		} else {
			if (mouseButton == MOUSE_LEFT && !Screen.hasAltDown()) {
				action = isShift ? Action.QUICK_MOVE_STACK : Action.TAKE_STACK;
			} else if (mouseButton == MOUSE_MIDDLE || Screen.hasAltDown()) {
				action = isShift ? Action.QUICK_MOVE_ONE : Action.TAKE_ONE;
			} else if (mouseButton == MOUSE_RIGHT) {
				action = isShift ? Action.QUICK_MOVE_HALF : Action.TAKE_HALF;
			}
		}

		if (action != null) {
			OpenContainerStorageInteractionC2S.sendPacket(action, target);
		}
	}
}