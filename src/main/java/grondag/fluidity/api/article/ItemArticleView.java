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
package grondag.fluidity.api.article;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

@API(status = Status.EXPERIMENTAL)
@FunctionalInterface
public interface ItemArticleView extends ArticleView {
	default Item item()  {
		return toStack().getItem();
	}

	@Nullable default CompoundTag tag() {
		return toStack().getTag();
	}

	@Override
	default int slot() {
		return 0;
	}

	@Override
	default boolean isEmpty() {
		return toStack().isEmpty();
	}

	ItemStack toStack();

	default long count() {
		return toStack().getCount();
	}

	default boolean isItemEqual(ItemStack stack)  {
		return toStack().isItemEqual(stack);
	}

	default boolean hasTag() {
		return toStack().hasTag();
	}

	@Override
	default boolean isItem() {
		return true;
	}

	@Override
	@Nullable
	default ItemArticleView toItemView() {
		return this;
	}
}
