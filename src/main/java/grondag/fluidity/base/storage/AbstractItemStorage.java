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
package grondag.fluidity.base.storage;

import java.util.function.Predicate;

import com.google.common.base.Predicates;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import grondag.fluidity.api.article.DiscreteArticleView;
import grondag.fluidity.api.item.DiscreteItem;
import grondag.fluidity.api.storage.DiscreteStorage;
import grondag.fluidity.api.storage.DiscreteStorageListener;
import grondag.fluidity.base.article.DiscreteArticle;

@API(status = Status.EXPERIMENTAL)
public abstract class AbstractItemStorage extends AbstractLazyRollbackStorage<DiscreteArticleView,  DiscreteStorageListener, DiscreteItem> implements DiscreteStorage {
	protected Predicate<DiscreteItem> filter = Predicates.alwaysTrue();

	public void filter(Predicate<DiscreteItem> filter) {
		this.filter = filter == null ? Predicates.alwaysTrue() : filter;
	}

	//TODO: make this lazy
	protected final FlexibleArticleManager<DiscreteItem, DiscreteArticle> articles;

	AbstractItemStorage(int startingHandleCount, Predicate<DiscreteItem> filter) {
		articles = new FlexibleArticleManager<>(startingHandleCount, DiscreteArticle::new);
		filter(filter);
	}

	@Override
	public CompoundTag writeTag() {
		final CompoundTag result = new CompoundTag();

		if(!isEmpty()) {
			final ListTag list = new ListTag();
			final int limit = articles.handleCount();

			for (int i = 0; i < limit; i++) {
				final DiscreteArticle a = articles.get(i);

				if(!a.isEmpty()) {
					list.add(a.toTag());
				}
			}

			result.put(TAG_ITEMS, list);
		}

		return result;
	}

	@Override
	public void readTag(CompoundTag tag) {
		clear();

		if(tag.contains(TAG_ITEMS)) {
			final ListTag list = tag.getList(TAG_ITEMS, 10);
			final int limit = list.size();
			final DiscreteArticle lookup = new DiscreteArticle();

			for(int i = 0; i < limit; i++) {
				lookup.readTag(list.getCompound(i));

				if(!lookup.isEmpty()) {
					accept(lookup.item(), lookup.count, false);
				}
			}
		}
	}

	@Override
	public int handleCount() {
		return articles.handleCount();
	}

	@Override
	public DiscreteArticleView view(int handle) {
		return articles.get(handle);
	}
}