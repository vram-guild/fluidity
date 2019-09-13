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
import grondag.fluidity.api.fluid.volume.ImmutableFluidVolume;
import grondag.fluidity.api.fluid.volume.MutableFluidVolume;
import grondag.fluidity.api.fluid.volume.fraction.Fraction;
import grondag.fluidity.api.fluid.volume.fraction.FractionView;

/**
 * For container views and queries. Volumes outside containers should use
 * concrete types.
 */
public interface ContainerFluidVolume {

    ContainerFluidVolume EMPTY = new ContainerFluidVolume() {
    };

    default FluidVariant fluid() {
        return FluidVariant.AIR;
    }

    default FractionView volume() {
        return Fraction.ZERO;
    }

    default FractionView capacity() {
        return volume();
    }

    default int slot() {
        return 0;
    }

    default ImmutableFluidVolume toImmutable() {
        return ImmutableFluidVolume.of(fluid(), volume());
    }

    default MutableFluidVolume mutableCopy() {
        return MutableFluidVolume.of(fluid(), volume());
    }
}
