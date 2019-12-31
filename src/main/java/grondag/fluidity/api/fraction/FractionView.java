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
package grondag.fluidity.api.fraction;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * To be exposed by containers that may not rely on a concrete rational number
 * implementation internally or to discourage external changes to mutable internals.
 */
@API(status = Status.EXPERIMENTAL)
public interface FractionView extends Comparable<FractionView> {
	int BUCKET = 1;
	int KILOLITER = 1;
	int BLOCK = 1;
	int SLAB = 2;
	int BOTTLE = 3;
	int INGOT = 9;
	int NUGGET = INGOT * 9;
	int LITER = 1000;

	long whole();

	long numerator();

	long divisor();

	/**
	 * Intended for user display. Result may be approximate due to floating point error.
	 *
	 * @param units Fraction of one that counts as 1 in the result. Must be >= 1.
	 * @return Current value scaled so that that 1.0 = one of the given units
	 */
	default double toDouble(long units) {
		// start with unit scale
		final double base = (double) numerator() / (double) divisor() + whole();

		// scale to requested unit
		return units == 1 ? base : base / units;
	}

	default double toDouble() {
		return toDouble(1);
	}

	/**
	 * Returns the number of units that is less than or equal to the given unit.
	 * Make be larger than this if value is not evenly divisible .
	 *
	 * @param units Fraction of one bucket that counts as 1 in the result. Must be >= 1.
	 * @return Number of units within current volume.
	 */
	default long toLong(long divisor) {
		if (divisor < 1) {
			throw new IllegalArgumentException("RationalNumber divisor must be >= 1");
		}

		final long base = whole() * divisor;

		if (numerator() == 0) {
			return base;
		} else if (divisor() == divisor) {
			return base + numerator();
		} else {
			return base + numerator() * divisor / divisor();
		}
	}

	default boolean isZero() {
		return whole() == 0 && numerator() == 0;
	}

	default boolean isNegative() {
		return whole() < 0 || (whole() == 0 && numerator() < 0);
	}

	@Override
	default int compareTo(FractionView o) {
		final int result = Long.compare(o.whole(), whole());
		return result == 0 ? Long.compare(o.numerator() * divisor(), numerator() * o.divisor()) : result;
	}

	default boolean isGreaterThan(FractionView other) {
		return compareTo(other) > 0;
	}

	default boolean isGreaterThankOrEqual(FractionView other) {
		return compareTo(other) >= 0;
	}

	default boolean isLessThan(FractionView other) {
		return compareTo(other) < 0;
	}

	default boolean isLessThankOrEqual(FractionView other) {
		return compareTo(other) <= 0;
	}

	default Fraction toImmutable() {
		return Fraction.of(whole(), numerator(), divisor());
	}
}
