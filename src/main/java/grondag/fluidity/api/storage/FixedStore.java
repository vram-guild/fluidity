/*******************************************************************************
 * Copyright 2019, 2020 grondag
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
package grondag.fluidity.api.storage;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

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
@API(status = Status.EXPERIMENTAL)
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
