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

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;

/**
 * Defines an observer that can subscribe to a {@link StorageEventStream}.
 *
 * @see <a href="https://github.com/grondag/fluidity#store-and-its-variants">https://github.com/grondag/fluidity#store-and-its-variants</a>
 */
@Experimental
public interface StorageListener {
	/**
	 * Called when store being listened to becomes unavailable.
	 * Stores that are disconnecting due to error or unexpected conditions
	 * should, at a minimum, call with {@code didNotify = false} and
	 * {@code isValid = false} so aggregate listeners know to reconstruct their views.
	 *
	 * @param storage Storage that was being observed.
	 * @param didNotify True if storage called {@code onSupply} before disconnecting. (Preferred)
	 * @param isValid True if storage state is currently valid and could be used to update listener.
	 */
	void disconnect(Store storage, boolean didNotify, boolean isValid);

	void onAccept(Store storage, int handle, Article item, long delta, long newCount);

	void onSupply(Store storage, int handle, Article item, long delta, long newCount);

	void onCapacityChange(Store storage, long capacityDelta);

	void onAccept(Store storage, int handle, Article item, Fraction delta, Fraction newVolume);

	void onSupply(Store storage, int handle, Article item, Fraction delta, Fraction newVolume);

	void onCapacityChange(Store storage, Fraction capacityDelta);
}
