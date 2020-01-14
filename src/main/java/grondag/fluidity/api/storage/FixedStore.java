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
 * Store with fixed handles - similar to slots but they don't have aribtrary limits
 * and request to accept or supply incompatible with existing content is rejected.
 */
@API(status = Status.EXPERIMENTAL)
public interface FixedStore extends Store {
	@Override
	default FixedArticleFunction getConsumer() {
		return FixedArticleFunction.FULL;
	}

	@Override
	default FixedArticleFunction getSupplier() {
		return FixedArticleFunction.EMPTY;
	}

	FixedStore EMPTY = EmptyStore.INSTANCE;
	FixedStore VOID = VoidStore.INSTANCE;
	FixedStore CREATIVE = CreativeStore.INSTANCE;
}
