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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.ItemStack;

import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;

/**
 * A view of an article stored in a container. (Could be an discrete item or bulk item.) <p>
 *
 * Containers (especially virtual ones) could contain both types of article.
 * Most containers will not need this and should instead use the specific view type for their content.
 */
@API(status = Status.EXPERIMENTAL)
public interface StoredArticleView {
	Article item();

	/**
	 * An abstract handle to a quantity of a specific article instance that will
	 * retain the handle:article mapping even if all of the article is removed, for as long as there is
	 * any listener.  This means listeners can always use slots to maintain a replicate of contents
	 * and reliably identify articles that have changed.
	 */
	int handle();


	long count();

	FractionView volume();

	/**
	 * Item is removed/depleted.  Of use when viewed fixed-slot containers or views of
	 * virtual storage systems that are emulating a fixed slot arrangement for client display.
	 */
	default boolean isEmpty() {
		return count() == 0;
	}

	default ItemStack toStack() {
		return item().toStack(count());
	}

	StoredArticleView EMPTY = new StoredArticleView() {
		@Override
		public Article item() {
			return Article.NOTHING;
		}

		@Override
		public int handle() {
			return NO_HANDLE;
		}

		@Override
		public long count() {
			return 0;
		}

		@Override
		public FractionView volume() {
			return Fraction.ZERO;
		}
	};

	int NO_HANDLE = -1;
}
