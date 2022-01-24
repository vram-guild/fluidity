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

package grondag.fluidity.base.article;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.fraction.Fraction;

/**
 * A view of an article stored in a container. (Could be an discrete item or bulk item.)
 *
 * <p>Containers (especially virtual ones) could contain both types of article.
 * Most containers will not need this and should instead use the specific view type for their content.
 */
@Experimental
public interface StoredDiscreteArticleView extends StoredArticleView {
	@Override
	default Fraction amount() {
		return Fraction.of(count());
	}
}
