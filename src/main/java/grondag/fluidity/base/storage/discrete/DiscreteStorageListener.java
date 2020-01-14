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
package grondag.fluidity.base.storage.discrete;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.api.storage.Store;

@API(status = Status.EXPERIMENTAL)
public interface DiscreteStorageListener extends StorageListener {
	@Override
	default void onCapacityChange(Store storage, Fraction capacityDelta) {
		onCapacityChange(storage, capacityDelta.whole());
	}

	@Override
	default void onAccept(Store storage, int handle, Article item, Fraction delta, Fraction newVolume) {
		onAccept(storage, handle, item, delta.whole(), newVolume.whole());
	}

	@Override
	default void onSupply(Store storage, int handle, Article item, Fraction delta, Fraction newVolume) {
		onSupply(storage, handle, item, delta.whole(), newVolume.whole());
	}
}
