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

import net.fabricmc.fabric.impl.fluids.MutableFluidVolumeImpl;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.Tag;
import net.minecraft.util.PacketByteBuf;

/**
 * The fluid analog of ItemStack
 */
public interface FluidVolume extends RationalNumber {
    // Common units
    int BUCKET = 1;
    int KILOLITER = 1;
    int LITER = 1000;
    int BLOCK = 1;
    int SLAB = 2;
    int BOTTLE = 3;
    int QUARTER = 4;
    int INGOT = 9;
    int NUGGET = 81;
    
    static MutableFluidVolume create(Fluid fluid, long volume, long capacity, long units) {
        return MutableFluidVolumeImpl.create(fluid, volume, capacity, units);
    }
    
    /**
     * Recreates instance from buffer populated via {@link #toBuffer(PacketByteBuf)}.
     * Suitable only for network traffic - assumes raw fluid ID's match on both sides.
     */
    static MutableFluidVolume fromBuffer(PacketByteBuf buf) {
        return MutableFluidVolumeImpl.fromBuffer(buf);
    }
    
    /**
     * Recreates instance from buffer populated via {@link #toTag()}.
     * Suitable for world saves. Fluid is serialized as an identifier.
     */
    static MutableFluidVolume fromTag(Tag tag) {
        return MutableFluidVolumeImpl.fromTag(tag);
    }
    
    Fluid fluid();
    
    /** 
     * Intended for user display. Result may be approximate due to floating point error.  
     * 
     * @param units Fraction of one bucket that counts as 1 in the result. Must be >= 1.
     * @return Current amount in stack scaled so that that 1.0 = one of the given units
     */
    double volumeForDisplay(long units);
    
    /**
     * Returns the number of units that is less than or equal to contents.
     * Make contain more than this if contents are not evenly divisible . 
     * 
     * @param units Fraction of one bucket that counts as 1 in the result. Must be >= 1.
     * @return Number of units within current volume.
     */
    long getVolume(long units);
    
    /** 
     * Use to know if the volume is <em>exactly</em> zero.
     * 
     * @return True if volume is absolutely empty.
     */
    boolean isEmpty();
    
    /** 
     * Use to know if volume is practically zero.
     * 
     * @param units Fraction of one bucket that counts as 1 in the result. Must be >= 1.
     * @return True if filled volume is less than the given unit.
     */
    boolean isEmpty(long units);
    
    /**
     * Max fill volume measured in {@link #getBaseUnit()} units.  
     * 
     * @return Max number of units that can be stored in this volume, irrespective
     * of current contents.
     */
    long getCapacity();
    
    /**
     * The unit in which capacity is measured.<p>
     * 
     * If {@link #isBaseUnitsOnly()} is true, then this volume will only
     * allow transfer and storage of amounts that can be expressed as whole
     * numbers in terms of the base unit.
     * 
     * @return  Current base unit for this volume.
     */
    long getBaseUnit();
    
    /**
     * Indicates transfers and storage amounts are quantized to a given unit.
     * See {@link #getBaseUnit()}.
     * 
     * @return True if amounts must conform to the base unit.
     */
    default boolean isBaseUnitsOnly() {
        return false;
    }
    
    /**
     * @return Self if already immutable, otherwise an immutable, exact and complete copy.
     */
    ImmutableFluidVolume toImmutable();
    
    /**
     * @return New mutable instance that is an exact and complete copy of the current instance.
     */
    MutableFluidVolume mutableCopy();
    
    /**
     * Serializes content to buffer. Recreate instance with {@link #fromBuffer(PacketByteBuf)}.
     * Suitable only for network traffic - assumes raw fluid ID's match on both sides.
     * 
     * @param buf
     */
    void toBuffer(PacketByteBuf buf);
    
    /**
     * Serializes content to NBT tag. Recreate instance with {@link #fromTag(Tag)}.
     * Suitable for world saves. Fluid is serialized as an identifier.
     * 
     * @return NBT tag with contents of instance.
     */
    Tag toTag();
}
