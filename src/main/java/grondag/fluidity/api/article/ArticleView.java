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

import grondag.fluidity.api.item.ArticleItem;

/**
 * A view of an article stored in a container. (Could be an discrete item or bulk item.) <p>
 *
 * Containers (especially virtual ones) could contain both types of article.
 * Most containers will not need this and should instead use the specific view type for their content.
 */
@API(status = Status.EXPERIMENTAL)
public interface ArticleView<I extends ArticleItem> {
	I item();

	/**
	 * For stores with fixed slots, this represents a specific location within the store.
	 * In other cases, it is an abstract handle to a quantity of a specific article instance that will
	 * retain the slot:article mapping even if all of the article is removed, for as long as there is
	 * any listener.  This means listeners can always use slots to maintain a replicate of contents
	 * and reliably identify articles that have changed.
	 */
	int slot();

	/**
	 * Item is removed/depleted.  Of use when viewed fixed-slot containers or views of
	 * virtual storage systems that are emulating a fixed slot arrangement for client display.
	 */
	boolean isEmpty();

	default boolean isBulk() {
		return toBulkView() != null;
	}

	@Nullable
	default BulkArticleView toBulkView() {
		return null;
	}

	default boolean isItem() {
		return toItemView() != null;
	}

	@Nullable
	default ItemArticleView toItemView() {
		return null;
	}
}
