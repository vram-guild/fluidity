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
package grondag.fluidity.api.storage;

import grondag.fluidity.api.bulk.BulkItem;
import grondag.fluidity.api.fraction.FractionView;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public interface ItemView {
	long count();
	
	FractionView volume();
	
	/**
	 * For stores with fixed slots, this represents a specific location within the store.
	 * In other cases, it is an abstract handle to a quantity of a specific article instance that will
	 * retain the slot:article mapping even if all of the article is removed, for as long as there is
	 * any listener.  This means listeners can always use slots to maintain a replicate of contents
	 * and reliably identify articles that have changed.
	 */
	int slot();
	
	boolean isEmpty();
	
	Item item();

	CompoundTag tag();

	ItemStack toStack();

	boolean isBulk();
	
	BulkItem toBulkItem();
	
    boolean isItemEqual(ItemStack stack);

	boolean hasTag();
}