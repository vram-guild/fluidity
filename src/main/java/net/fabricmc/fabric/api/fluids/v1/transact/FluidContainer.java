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

import java.util.function.Consumer;

import net.fabricmc.fabric.api.fluids.v1.volume.FluidVolumeView;
import net.fabricmc.fabric.api.fluids.v1.volume.ImmutableFluidVolume;

/**
 * Very WIP - not happy with current state.
 */
public interface FluidContainer extends FluidTxActor {
    
    boolean isEmpty();
    
    default FluidPort output() {
        return FluidPort.VOID;
    }
    
    default FluidPort input() {
        return FluidPort.VOID;
    }

    ImmutableFluidVolume[] startListening(Consumer<ImmutableFluidVolume> listener);
    
    void stopListening(Consumer<ImmutableFluidVolume> listener);
    
    void forEach(Consumer<FluidVolumeView> consumer);
}
