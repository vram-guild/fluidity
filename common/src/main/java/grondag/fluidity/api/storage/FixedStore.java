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

package grondag.fluidity.api.storage;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.impl.storage.CreativeStore;
import grondag.fluidity.impl.storage.EmptyStore;
import grondag.fluidity.impl.storage.VoidStore;

/**
 * Store with fixed handles - operations on the store can accept handles
 * and operations will return zero if the article associated with that handle
 * is different from what is expected. Use for stores with fixed storage locations
 * that need to be explicitly specified and/or visible to the player.
 *
 * @see <a href="https://github.com/grondag/fluidity#store-and-its-variants">https://github.com/grondag/fluidity#store-and-its-variants</a>
 */
@Experimental
public interface FixedStore extends Store {
	@Override
	default FixedArticleFunction getConsumer() {
		return FixedArticleFunction.ALWAYS_RETURN_ZERO;
	}

	@Override
	default FixedArticleFunction getSupplier() {
		return FixedArticleFunction.ALWAYS_RETURN_ZERO;
	}

	FixedStore EMPTY = EmptyStore.INSTANCE;
	FixedStore VOID = VoidStore.INSTANCE;
	FixedStore CREATIVE = CreativeStore.INSTANCE;
}
