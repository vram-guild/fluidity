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
package grondag.fluidity.impl;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;

import grondag.fluidity.api.client.ItemDisplayDelegate;

@API(status = Status.INTERNAL)
public class ItemDisplayDelegateImpl implements ItemDisplayDelegate {
	ItemStack stack = ItemStack.EMPTY;
	long count;
	int handle;
	String localizedName;
	String lowerCaseLocalizedName;

	public ItemDisplayDelegateImpl(ItemStack stack, long count, int handle) {
		set(stack, count, handle);
	}

	@Override
	public ItemDisplayDelegateImpl set (ItemStack stack, long count, int handle) {
		if(stack == null) {
			stack = ItemStack.EMPTY;
		}

		this.count = count;
		this.handle = handle;

		if(!(ItemStack.areItemsEqual(stack, this.stack) && ItemStack.areTagsEqual(stack, this.stack))) {
			this.stack = stack;
			localizedName = I18n.translate(stack.getTranslationKey());
			lowerCaseLocalizedName = localizedName.toLowerCase();
		}

		return this;
	}

	@Override
	public ItemDisplayDelegateImpl clone() {
		return new ItemDisplayDelegateImpl(stack, count, handle);
	}

	@Override
	public int handle() {
		return handle;
	}

	@Override
	public ItemStack displayStack() {
		return stack;
	}

	@Override
	public long count() {
		return count;
	}

	@Override
	public String localizedName() {
		return localizedName;
	}

	@Override
	public String lowerCaseLocalizedName() {
		return lowerCaseLocalizedName;
	}
}
