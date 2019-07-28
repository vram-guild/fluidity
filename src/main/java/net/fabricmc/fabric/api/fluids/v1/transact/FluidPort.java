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

package net.fabricmc.fabric.api.fluids.v1.transact;

import net.fabricmc.fabric.api.fluids.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.fluids.v1.fraction.Fraction;
import net.fabricmc.fabric.api.fluids.v1.fraction.FractionView;
import net.fabricmc.fabric.api.fluids.v1.volume.FluidVolumeView;
import net.fabricmc.fabric.api.fluids.v1.volume.ImmutableFluidVolume;
import net.fabricmc.fabric.api.fluids.v1.volume.MutableFluidVolume;

/**
 * The thing that will be used to take fluid in or out of another thing.
 */
@FunctionalInterface
public interface FluidPort {
    static int NORMAL = 0;
    static int EXACT = 1;
    static int SIMULATE = 2;
    
    static FluidPort VOID = new FluidPort() {
        @Override
        public FractionView apply(FluidVariant fluid, FractionView volume, int flags) {
            return Fraction.ZERO;
        }
    };
    
    FractionView apply(FluidVariant fluid, FractionView volume, int flags);
    
    default long apply(FluidVariant fluid, long volume, long units, int flags) {
        return apply(fluid, Fraction.of(volume, units), flags).toLong(units);
    }
    
    default ImmutableFluidVolume apply(FluidVolumeView volume, int flags) {
        return ImmutableFluidVolume.of(volume.getFluid(), apply(volume.getFluid(), volume.volume(), flags));
    }
    
    default boolean applyAndAdd(FluidVariant fluid, FractionView volume, int flags, MutableFluidVolume target) {
        if(target.getFluid().equals(fluid) || target.volume().isZero()) {
            final FractionView result = apply(fluid, volume, flags);
            if(result.isZero()) {
                return false;
            } else {
                target.volume().add(result);
                target.setFluid(fluid);
                return true;
            }
        } else {
            return false;
        }
    }
    
    default boolean applyAndSubtract(FluidVariant fluid, FractionView volume, int flags, MutableFluidVolume target) {
        if(target.getFluid().equals(fluid) || target.volume().isZero()) {
            final FractionView result = apply(fluid, volume, flags);
            if(result.isZero()) {
                return false;
            } else {
                target.volume().subtract(result);
                target.setFluid(fluid);
                return true;
            }
        } else {
            return false;
        }
    }
    
    default boolean applyAndSubtract(MutableFluidVolume target, int flags) {
        if(target.volume().isZero()) {
            return false;
        } else {
            final FractionView result = apply(target.getFluid(), target.volume(), flags);
            if(result.isZero()) {
                return false;
            } else {
                target.volume().subtract(result);
                return true;
            }
        }
    }
}
