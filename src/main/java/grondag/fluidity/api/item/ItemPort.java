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

package grondag.fluidity.api.item;

import grondag.fluidity.api.storage.Port;
import net.minecraft.item.ItemStack;


public interface ItemPort extends Port {
    ItemStack accept(ItemStack stack, int limit, int flags);

    default ItemStack accept(ItemStack stack, int flags) {
    	return accept(stack, stack.getCount(), flags);
    }

    ItemStack supply(ItemStack stack, int limit, int flags);
    
    default ItemStack supply(ItemStack stack, int flags) {
    	return supply(stack, stack.getCount(), flags);
    }

    static ItemPort VOID = new ItemPort() {
		@Override
		public ItemStack accept(ItemStack stack, int limit, int flags) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack supply(ItemStack stack, int limit, int flags) {
			return ItemStack.EMPTY;
		}
    };
}
