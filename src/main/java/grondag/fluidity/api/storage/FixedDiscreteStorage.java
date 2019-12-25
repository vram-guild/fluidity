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
package grondag.fluidity.api.storage;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.item.StorageItem;

/**
 * Storage with fixed handles - similar to slots but they don't have aribtrary limits
 * and request to accept or supply incompatible with existing content is rejected.
 */
@API(status = Status.EXPERIMENTAL)
public interface FixedDiscreteStorage extends DiscreteStorage {
	/**
	 * Will return zero if handle slot is already occupied and different.
	 * @param item
	 * @param count
	 * @param simulate
	 * @param handle
	 * @return
	 */
	long accept(int handle, StorageItem item, long count, boolean simulate);

	/**
	 *
	 * Will return zero if handle slot is not occupied by the requested item.
	 *
	 * @param handle
	 * @param item
	 * @param count
	 * @param simulate
	 * @return
	 */
	long supply(int handle, StorageItem item, long count, boolean simulate);
}
