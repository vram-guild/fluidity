package grondag.fluidity.wip;

import grondag.fluidity.api.storage.AbstractItemStorage;
import net.minecraft.item.ItemStack;

public class InventoryAdapter extends AbstractItemStorage<Void>{

	@Override
	public int slotCount() {
		return this.getInvSize();
	}

	@Override
	protected ItemStack getStack(int slot) {
		return this.getInvStack(slot);
	}

	@Override
	protected void setStack(int slot, ItemStack stack) {
		// TODO Auto-generated method stub
		
	}

}
