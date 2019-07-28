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

package net.fabricmc.fabric.api.fluids.v1.volume;

import net.fabricmc.fabric.api.fluids.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.fluids.v1.fraction.FractionView;

/**
 * For views and queries.  Transactions should use concrete types.
 */
public interface FluidVolumeView {

    FluidVariant getFluid();

    FractionView volume();

    default ImmutableFluidVolume toImmutable() {
        return ImmutableFluidVolume.of(getFluid(), volume());
    }

    default MutableFluidVolume mutableCopy() {
        return MutableFluidVolume.of(getFluid(), volume());
    }
}
