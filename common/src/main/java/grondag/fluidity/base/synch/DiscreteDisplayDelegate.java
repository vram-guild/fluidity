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

package grondag.fluidity.base.synch;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.impl.DiscreteDisplayDelegateImpl;

/**
 * Client-side representation of server inventory that supports
 * very large quantities and slotless/virtual containers via handles.
 */
@Experimental
public interface DiscreteDisplayDelegate extends DisplayDelegate {
	@Override
	long getCount();

	void setCount(long count);

	DiscreteDisplayDelegate set(Article article, long count, int handle);

	default DiscreteDisplayDelegate set(DiscreteDisplayDelegate from) {
		return set(from.article(), from.getCount(), from.handle());
	}

	DiscreteDisplayDelegate EMPTY = new DiscreteDisplayDelegateImpl(Article.NOTHING, 0, -1);

	static DiscreteDisplayDelegate create(Article stack, long count, int handle) {
		return new DiscreteDisplayDelegateImpl(stack, count, handle);
	}
}
