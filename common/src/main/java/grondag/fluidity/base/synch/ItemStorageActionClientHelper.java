package grondag.fluidity.base.synch;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/** Isolates client-side code. */
public class ItemStorageActionClientHelper {
	private static final int MOUSE_LEFT = 0;
	private static final int MOUSE_MIDDLE = 2;
	private static final int MOUSE_RIGHT = 1;

	public static ItemStorageAction select(int mouseButton, @Nullable DiscreteDisplayDelegate target) {
		ItemStorageAction result = null;

		final boolean isShift = Screen.hasShiftDown();

		@SuppressWarnings("resource")
		final ItemStack cursorStack = Minecraft.getInstance().player.containerMenu.getCarried();

		// if alt/right/middle clicking on same item, don't count that as a deposit
		if (cursorStack != null && !cursorStack.isEmpty()
		&& !(target != null && ItemStack.isSameItemSameTags(cursorStack, target.article().toStack()) && (Screen.hasAltDown() || mouseButton > 0))) {

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

	public static boolean selectAndSend(int mouseButton, @Nullable DiscreteDisplayDelegate target) {
		final ItemStorageAction action = select(mouseButton, target);

		if (action != null) {
			sendPacket(action, target);
			return true;
		} else {
			return false;
		}
	}

	private static void sendPacket(ItemStorageAction action, DiscreteDisplayDelegate target) {
		if (Minecraft.getInstance().getConnection() != null) {
			final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
			buf.writeEnum(action);
			buf.writeInt(target == null ? -1 : target.handle());
			NetworkManager.sendToServer(ItemStorageInteractionC2S.ID, buf);
		}
	}
}
