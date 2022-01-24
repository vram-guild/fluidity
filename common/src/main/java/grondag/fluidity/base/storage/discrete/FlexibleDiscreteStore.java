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

package grondag.fluidity.base.storage.discrete;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.base.article.StoredDiscreteArticle;
import grondag.fluidity.base.storage.helper.FlexibleArticleManager;

@Experimental
public class FlexibleDiscreteStore extends AbstractDiscreteStore<FlexibleDiscreteStore> {
	public FlexibleDiscreteStore(int startingHandleCount, long capacity) {
		super(startingHandleCount, capacity, new FlexibleArticleManager<>(startingHandleCount, StoredDiscreteArticle::new));
	}

	public FlexibleDiscreteStore(long capacity) {
		this(32, capacity);
	}
}
