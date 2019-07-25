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

package net.fabricmc.fabric.api.fluids.v1;

public interface MutableFluidVolume extends FluidVolume, MutableRationalNumber {
    /**
     * Sets max fill volume and base units for this instance.<p>
     * 
     * See {@link #getBaseUnit()} and {@link #getCapacity()}.
     * 
     * @param capacity  Max fill volume in given unit.
     * @param units Fraction of one bucket that becomes the base unit for this instance. Must be >= 1.
     */
    MutableFluidVolume setCapacity(long capacity, long baseUnits);
    
    /**
     * Sets volume exactly.  Must be less than {@link #getMaxBuckets()} or
     * an exception will be thrown.
     * 
     * @param volume New volume to be set. Must be <= capacity.
     * @param units Fraction of one bucket that counts as 1 in the result. Must be >= 1.
     */
    MutableFluidVolume setVolume(long volume, long units);
    
    /**
     * Removes up to the given number of units and returns that number.
     * 
     * @param volume Amount to remove.
     * @param units Fraction of one bucket that counts as 1 in the result. Must be >= 1.
     * @param simulate If true, state is not changed and result indicates what would happen.
     * @return Amount removed.
     */
    long drain(long volume, long units, boolean simulate);
    
    /**
     * Removes up to the given number of units and returns that number.
     * 
     * @param volume Amount to remove.
     * @param units Fraction of one bucket that counts as 1 in the result. Must be >= 1.
     * @return Amount removed.
     */
    default long drain(long volume, long units) {
        return drain(volume, units, false);
    }
    
    /**
     * Removes the given number of units or does nothing if that is not possible.
     * 
     * @param volume Amount to remove.
     * @param units Fraction of one bucket that counts as 1 in the result. Must be >= 1.
     * @param simulate If true, state is not changed and result indicates what would happen.
     * @return True if exact amount was removed.
     */
    boolean drainExactly(long volume, long units, boolean simulate);
    
    /**
     * Removes the given number of units or does nothing if that is not possible.
     * 
     * @param volume Amount to remove.
     * @param units Fraction of one bucket that counts as 1 in the result. Must be >= 1.
     * @return True if exact amount was removed.
     */
    default boolean drainExactly(long volume, long units) {
        return drainExactly(volume, units);
    }
    
    /**
     * Adds up to the given number of units and returns that number.
     * 
     * @param volume Amount to add.
     * @param units Fraction of one bucket that counts as 1 in the result. Must be >= 1.
     * @param simulate If true, state is not changed and result indicates what would happen.
     * @return Amount added.
     */
    long fill(long volume, long units, boolean simulate);
    
    /**
     * Adds up to the given number of units and returns that number.
     * 
     * @param volume Amount to add.
     * @param units Fraction of one bucket that counts as 1 in the result. Must be >= 1.
     * @return Amount added.
     */
    default long fill(long volume, long units) {
        return fill(volume, units, false);
    }
    
    /**
     * Adds the given number of units or does nothing if that is not possible.
     * 
     * @param volume Amount to remove.
     * @param units Fraction of one bucket that counts as 1 in the result. Must be >= 1.
     * @param simulate If true, state is not changed and result indicates what would happen.
     * @return True if exact amount was removed.
     */
    boolean fillExactly(long volume, long units, boolean simulate);
    
    /**
     * Adds the given number of units or does nothing if that is not possible.
     * 
     * @param volume Amount to remove.
     * @param units Fraction of one bucket that counts as 1 in the result. Must be >= 1.
     * @return True if exact amount was removed.
     */
    default boolean fillExactly(long volume, long units) {
        return fillExactly(volume, units, false);
    }
}
