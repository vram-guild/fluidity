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

import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.api.item.Article;
import grondag.fluidity.api.storage.Storage;

@API(status = Status.EXPERIMENTAL)
public class BulkStoredArticle extends AbstractStoredArticle {
	protected MutableFraction fraction;
	protected int handle;

	public BulkStoredArticle() {
	}

	public BulkStoredArticle(ItemStack stack, int handle) {
		prepare(stack, handle);
	}

	public BulkStoredArticle prepare(ItemStack stack, int handle) {
		final Item item = stack.getItem();
		final CompoundTag tag = stack.getTag();

		this.handle = handle;

		if(item instanceof Article) {
			this.item = (Article) item;
			fraction.readTag(tag);
			fraction.multiply(stack.getCount());
		} else  {
			this.item = Article.NOTHING;
			fraction.set(Fraction.ZERO);
		}

		return this;
	}

	@Override
	public boolean isEmpty() {
		return fraction.isZero();
	}

	@Override
	public void zero() {
		fraction.set(0);
	}

	@Override
	public FractionView volume() {
		return fraction;
	}

	public static BulkStoredArticle of(ItemStack stack) {
		return new  BulkStoredArticle().prepare(stack, 0);
	}

	@Override
	public void addStore(Storage store) {
		// TODO Auto-generated method stub

	}

	@Override
	public long count() {
		// TODO Auto-generated method stub
		return 0;
	}
}
