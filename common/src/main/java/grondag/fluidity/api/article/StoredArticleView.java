/*
 * This file is part of Fluidity and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.fluidity.api.article;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.world.item.ItemStack;

import grondag.fluidity.api.fraction.Fraction;

/**
 * A view of an article in storage. The article may be of any type.
 *
 * <p>Most implementations will want to implement one of the accounting-specific
 * {@code StoredDiscreteArticleView} or {{@code StoredBulkArticleView} interfaces,
 * but all code using a {@code StoredArticleView} should depend on this more general interface.
 */
@Experimental
public interface StoredArticleView {
	/**
	 * The article being stored.  Should be {@link Article#NOTHING} if this view is empty.
	 *
	 * @return The article being stored
	 */
	Article article();

	/**
	 * Identifies this article and quantity exposed in this view within it's current store (which could be a virtual view).
	 * A {@code Store} will persist handle:article mappings even if all of the article is removed,
	 * for as long as there is any listener, or it will send explicit events to listeners to indicate
	 * a any change.  This means listeners can rely on handles to maintain a replica of contents
	 * and identify articles that have changed.
	 *
	 * @return integer identifying this article and quantity in it's current store or view.
	 */
	int handle();

	/**
	 * The quantity of the article as whole units. Will not contain any fractional portion.
	 *
	 * @return Quantity of the article as whole units
	 */
	long count();

	/**
	 * The quantity of the article as a fraction. The whole portion will match {@link #count()}.
	 *
	 * @return Quantity of the article as a fraction
	 */
	Fraction amount();

	/**
	 * Test if this view removed or depleted.  Should not be used to plan storage operations but
	 * is instead useful for filtering views from user display, or for displaying empty slots.
	 *
	 * @return {@code true} if this view has no content
	 */
	boolean isEmpty();

	/**
	 * Construct an {@code ItemStack} instance with the {@code Item} and quantity in this view.
	 * If the article has an associated NBT tag, it will be copied to the item stack.
	 * If the article does not represent an {@code Item}, or if this view is empty, will return {@link ItemStack#EMPTY}.
	 *
	 * <p>The count of the item stack will be the lesser of {@link #count()} and the maximum stack
	 * size defined by the item. Thus the stack may have a smaller quantity than this view.
	 *
	 * @return An {@code ItemStack} instance with the {@code Item} and quantity in this view
	 */
	default ItemStack toStack() {
		return article().toStack(count());
	}

	/**
	 * Special instance of {@code StoredArticleView} that represents that absence of any content.
	 * The handle of this instance will always be {@link #NO_HANDLE}.
	 */
	StoredArticleView EMPTY = new StoredArticleView() {
		@Override
		public Article article() {
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
		public Fraction amount() {
			return Fraction.ZERO;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}
	};

	/**
	 * Special value of {@link #handle()} to represent the absence of a defined handle.
	 * This value should never retrieve a non-empty article view - the result of using this
	 * handle should always be {@link #EMPTY}.
	 */
	int NO_HANDLE = -1;
}
