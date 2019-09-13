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

package grondag.fluidity.api.fluid.container;

import grondag.fluidity.api.fluid.FluidVariant;
import grondag.fluidity.api.fluid.volume.FluidVolume;
import grondag.fluidity.api.fluid.volume.ImmutableFluidVolume;
import grondag.fluidity.api.fluid.volume.MutableFluidVolume;
import grondag.fluidity.api.fluid.volume.fraction.Fraction;
import grondag.fluidity.api.fluid.volume.fraction.FractionView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

/**
 * The thing that will be used to take fluid in or out of another thing.
 */
public interface FluidPort {
    static int NORMAL = 0;
    static int EXACT = 1;
    static int SIMULATE = 2;

    default Identifier id() {
        return FluidContainer.ANONYMOUS_ID;
    }

    default Direction side() {
        return null;
    }

    default boolean canFill() {
        return true;
    }

    default boolean canDrain() {
        return true;
    }

    FractionView fill(FluidVariant fluid, FractionView volume, int flags);

    default long fill(FluidVariant fluid, long volume, long units, int flags) {
        return fill(fluid, Fraction.of(volume, units), flags).toLong(units);
    }

    default ImmutableFluidVolume fill(FluidVolume volume, int flags) {
        return ImmutableFluidVolume.of(volume.fluid(), fill(volume.fluid(), volume.volume(), flags));
    }

    default boolean fillFrom(FluidVariant fluid, FractionView volume, int flags, MutableFluidVolume target) {
        if (target.fluid().equals(fluid) || target.volume().isZero()) {
            final FractionView result = fill(fluid, volume, flags);
            if (result.isZero()) {
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

    default boolean fillFrom(MutableFluidVolume target, int flags) {
        if (target.volume().isZero()) {
            return false;
        } else {
            final FractionView result = fill(target.fluid(), target.volume(), flags);
            if (result.isZero()) {
                return false;
            } else {
                target.volume().subtract(result);
                return true;
            }
        }
    }

    FractionView drain(FluidVariant fluid, FractionView volume, int flags);

    default long drain(FluidVariant fluid, long volume, long units, int flags) {
        return drain(fluid, Fraction.of(volume, units), flags).toLong(units);
    }

    default ImmutableFluidVolume drain(FluidVolume volume, int flags) {
        return ImmutableFluidVolume.of(volume.fluid(), drain(volume.fluid(), volume.volume(), flags));
    }

    default boolean drainTo(FluidVariant fluid, FractionView volume, int flags, MutableFluidVolume target) {
        if (target.fluid().equals(fluid) || target.volume().isZero()) {
            final FractionView result = drain(fluid, volume, flags);
            if (result.isZero()) {
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

    static FluidPort VOID = new FluidPort() {
        @Override
        public FractionView fill(FluidVariant fluid, FractionView volume, int flags) {
            return Fraction.ZERO;
        }

        @Override
        public FractionView drain(FluidVariant fluid, FractionView volume, int flags) {
            return Fraction.ZERO;
        }
    };
}
