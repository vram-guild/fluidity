//TODO: reimplement

///*******************************************************************************
// * Copyright 2019 grondag
// *
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License.  You may obtain a copy
// * of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// * License for the specific language governing permissions and limitations under
// * the License.
// ******************************************************************************/
//package grondag.fluidity.base.compat;
//
//import org.apiguardian.api.API;
//import org.apiguardian.api.API.Status;
//
//import net.minecraft.inventory.Inventory;
//import net.minecraft.item.ItemStack;
//
//import grondag.fluidity.api.article.ArticleView;
//import grondag.fluidity.api.storage.InventoryStorage;
//import grondag.fluidity.base.article.ItemStackView;
//import grondag.fluidity.base.storage.AbstractLazyRollbackStorage;
//import grondag.fluidity.base.transact.TransactionHelper;
//
//@API(status = Status.EXPERIMENTAL)
//public class InventoryAdapter extends AbstractLazyRollbackStorage implements InventoryStorage {
//	protected Inventory inventory;
//	protected final ItemStackView view = new ItemStackView();
//
//	public InventoryAdapter(Inventory inventory) {
//		this.inventory = inventory;
//	}
//
//	public InventoryAdapter prepare(Inventory inventory) {
//		this.inventory = inventory;
//		return this;
//	}
//
//	@Override
//	public boolean isEmpty() {
//		return inventory.isInvEmpty();
//	}
//
//	@Override
//	public boolean hasDynamicSlots() {
//		return false;
//	}
//
//	@Override
//	public int slotCount() {
//		return inventory.getInvSize();
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public <T extends ArticleView> T view(int slot) {
//		return (T) view.prepare(getInvStack(slot), slot);
//	}
//
//	@Override
//	public ItemStack getInvStack(int slot) {
//		return inventory.getInvStack(slot);
//	}
//
//	@Override
//	public ItemStack takeInvStack(int slot, int count) {
//		final ItemStack result = inventory.takeInvStack(slot, count);
//		notifyListeners(slot);
//		return result;
//	}
//
//	@Override
//	public ItemStack removeInvStack(int slot) {
//		final ItemStack result = inventory.removeInvStack(slot);
//		notifyListeners(slot);
//		return result;
//	}
//
//	@Override
//	public void setInvStack(int slot, ItemStack itemStack) {
//		inventory.setInvStack(slot, itemStack);
//		notifyListeners(slot);
//	}
//
//	@Override
//	public void markDirty() {
//		inventory.markDirty();
//	}
//
//	@Override
//	protected Object createRollbackState() {
//		return TransactionHelper.prepareInventoryRollbackState(this);
//	}
//
//	@Override
//	protected void applyRollbackState(Object state) {
//		TransactionHelper.applyInventoryRollbackState(state, this);
//	}
//}
