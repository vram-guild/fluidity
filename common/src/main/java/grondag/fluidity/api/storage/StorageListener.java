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
