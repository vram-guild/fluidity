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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.BulkArticleView;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.api.item.BulkItem;

@API(status = Status.EXPERIMENTAL)
public class BulkArticle implements BulkArticleView {
	protected BulkItem item;
	protected MutableFraction fraction;
	protected int handle;

	public BulkArticle() {
	}

	public BulkArticle(ItemStack stack, int handle) {
		prepare(stack, handle);
	}

	public BulkArticle prepare(ItemStack stack, int handle) {
		final Item item = stack.getItem();
		final CompoundTag tag = stack.getTag();

		this.handle = handle;
		if(item instanceof BulkItem) {
			this.item = (BulkItem) item;
			fraction.readTag(tag);
			fraction.multiply(stack.getCount());
		} else  {
			this.item = BulkItem.NOTHING;
			fraction.set(Fraction.ZERO);
		}

		return this;
	}

	@Override
	public BulkItem item() {
		return item;
	}

	@Override
	public int handle() {
		return handle;
	}

	@Override
	public boolean isEmpty() {
		return fraction.isZero();
	}

	@Override
	public FractionView volume() {
		return fraction;
	}

	public static BulkArticle of(ItemStack stack) {
		return new  BulkArticle().prepare(stack, 0);
	}
}
