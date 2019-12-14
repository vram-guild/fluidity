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
package grondag.fluidity.base.article;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.ItemStack;

import grondag.fluidity.api.article.ItemArticleView;
import grondag.fluidity.api.item.DiscreteItem;

@API(status = Status.EXPERIMENTAL)
public class DiscreteStackView implements ItemArticleView {
	protected DiscreteItem item;
	protected int count;
	protected int slot;

	public DiscreteStackView() {
	}

	public DiscreteStackView(ItemStack stack, int slot) {
		prepare(stack, slot);
	}

	public DiscreteStackView prepare(ItemStack stack, int slot) {
		item = DiscreteItem.of(stack);
		this.slot = slot;
		count = stack.getCount();
		return this;
	}

	@Override
	public DiscreteItem item() {
		return item;
	}

	@Override
	public long count() {
		return count;
	}

	@Override
	public int slot() {
		return slot;
	}

	public static DiscreteStackView of(ItemStack stack) {
		return new  DiscreteStackView().prepare(stack, 0);
	}
}
