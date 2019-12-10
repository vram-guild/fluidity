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

import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.item.base.BulkItem;

public interface BulkStorage extends Storage {
	/**
	 * Adds up to  {@code volume} units of the bulk item to this storage and
	 * returns the number of units added.  The denominator of the result *may*
	 * be different from the denominator of the input fraction.
	 *
	 * Storage containers or pipes that only deal in certain units (for example,
	 * the vanilla cauldron) should return zero or a lesser amount for requests
	 * that would result in an invalid state.<p>
	 *
	 * @param item  The stuff to add
	 * @param volume How much to add
	 * @param simulate If true, forecasts the result without making any changes
	 * @return How much stuff was added
	 */
	FractionView accept(BulkItem item, FractionView volume, boolean simulate);

	/**
	 * Removes up to  {@code volume} units of the bulk item to this storage and
	 * returns the number of units removed.  The denominator of the result *may*
	 * be different from the denominator of the input fraction.
	 *
	 * Storage containers or pipes that only deal in certain units (for example,
	 * the vanilla cauldron) should return zero or a lesser amount for requests
	 * that would result in an invalid state.<p>
	 *
	 * @param item  The stuff to remove
	 * @param volume How much to remove
	 * @param simulate If true, forecasts the result without making any changes
	 * @return How much stuff was removed
	 */
	FractionView supply(BulkItem item, FractionView volume, boolean simulate);

	/**
	 * As with {@link #accept(BulkItem, FractionView, boolean)} BUT the result
	 * will always be an even multiple of the input denominator.  So, for example,
	 * if you call with {@link FractionView#BOTTLE} as the denominator, you will
	 * only get whole bottles as the result.<p>
	 *
	 * Storage containers or pipes that only deal in certain units (for example,
	 * the vanilla cauldron) should return zero or a lesser amount for requests
	 * that would result in an invalid state.<p>
	 *
	 * @param item The stuff to add
	 * @param numerator Fractional units to add. Can be zero.
	 * @param divisor Denominator of units to add. Must be >= 1.
	 * @return How much was added, in units of given denominator.
	 */
	long accept(BulkItem item, long numerator, long divisor);

	/**
	 * As with {@link #supply(BulkItem, FractionView, boolean)} BUT the result
	 * will always be an even multiple of the input denominator.  So, for example,
	 * if you call with {@link FractionView#BOTTLE} as the denominator, you will
	 * only get whole bottles as the result.<p>
	 *
	 * Storage containers or pipes that only deal in certain units (for example,
	 * the vanilla cauldron) should return zero or a lesser amount for requests
	 * that would result in an invalid state.<p>
	 *
	 * @param item The stuff to remove
	 * @param numerator Fractional units to remove. Can be zero.
	 * @param divisor Denominator of units to add. Must be >= 1.
	 * @return How much was removed, in units of given denominator.
	 */
	long supply(BulkItem item, long numerator, long divisor);
}
