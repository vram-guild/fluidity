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
/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grondag.fluidity.api.fluid.volume.fraction;

/**
 * To be exposed by containers that may not rely on a concrete rational number
 * implementation internally or to prevent external mutation of mutable
 * internals.
 */
public interface FractionView extends Comparable<FractionView> {
    long whole();

    long numerator();

    long divisor();

    /**
     * Intended for user display. Result may be approximate due to floating point
     * error.
     * 
     * @param units Fraction of one that counts as 1 in the result. Must be >= 1.
     * @return Current value scaled so that that 1.0 = one of the given units
     */
    default double toDouble(long units) {
        // start with unit scale
        double base = (double) numerator() / (double) divisor() + (double) whole();

        // scale to requested unit
        return units == 1 ? base : base / (double) units;
    }

    default double toDouble() {
        return toDouble(1);
    }

    /**
     * Returns the number of units that is less than or equal to the given unit.
     * Make be larger than this if value is not evenly divisible .
     * 
     * @param units Fraction of one bucket that counts as 1 in the result. Must be
     *              >= 1.
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

    @Override
    default int compareTo(FractionView o) {
        // Egregious hack because this implementation will not be sticking around
        return Double.compare(this.toDouble(1), o.toDouble(1));
    }

    default Fraction toImmutable() {
        return Fraction.of(whole(), numerator(), divisor());
    }
}
