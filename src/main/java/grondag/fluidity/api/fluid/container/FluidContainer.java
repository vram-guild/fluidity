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

import java.util.Iterator;
import java.util.function.Predicate;

import com.google.common.base.Predicates;

import grondag.fluidity.api.fluid.container.FluidContainerListener.StopNotifier;
import grondag.fluidity.api.fluid.transact.FluidTxActor;
import grondag.fluidity.api.fluid.volume.fraction.FractionView;
import net.minecraft.util.Identifier;

public interface FluidContainer extends FluidTxActor {
    Identifier ANONYMOUS_ID = new Identifier("fabric:anon");
    int NO_SLOT = -1;
    
    boolean isEmpty();
    
    FractionView totalCapacity();
    
    /**
     * True when can contain more than one fluid.
     */
    default boolean isCompound() {
        return false;
    }
    
    /**
     * True when has ports that can only be accessed from certain sides.
     */
    default boolean isSided() {
        return false;
    }

    /**
     * True when container is a view of other containers. This means the 
     * contents of this container could be visible in other containers.
     */
    default boolean isVirtual() {
        return false;
    }
    
    Iterable<FluidPort> ports(PortFilter portFilter);

    default Iterable<FluidPort> ports() {
        return ports(PortFilter.ALL);
    }
    
    default FluidPort firstPort(PortFilter portFilter) {
        Iterator<FluidPort> it = ports(portFilter).iterator();
        return it.hasNext() ? it.next() : FluidPort.VOID;
    }
  
    default FluidPort firstPort() {
        return firstPort(PortFilter.ALL);
    }

    /**
     * For containers with named slots, finds slot using named-spaced identifier.
     * @param id
     * @return integer identifier of first matching slot found or {@code NO_SLOT} if no match
     * @implNote Should be overridden if container has named slots
     * @see ContainerFluidVolume#slot()
     */
    default int slotFromId(Identifier id) {
        return NO_SLOT;
    }

    default Identifier idForSlot(int slot) {
        return ANONYMOUS_ID;
    }
    
    Iterable<ContainerFluidVolume> volumes(PortFilter portFilter, Predicate<ContainerFluidVolume> fluidFilter);

    ContainerFluidVolume volumeForSlot(int slot);
    
    default ContainerFluidVolume volumeForId(Identifier id) {
        final int slot = slotFromId(id);
        return slot >= 0 ? volumeForSlot(slot) : ContainerFluidVolume.EMPTY;
    }

    default Iterable<ContainerFluidVolume> volumes(PortFilter portFilter) {
        return volumes(portFilter, Predicates.alwaysTrue());
    }
    
    default Iterable<ContainerFluidVolume> volumes(Predicate<ContainerFluidVolume> fluidFilter) {
        return volumes(PortFilter.ALL, fluidFilter);
    }

    default Iterable<ContainerFluidVolume> volumes() {
        return volumes(PortFilter.ALL, Predicates.alwaysTrue());
    }

    default ContainerFluidVolume firstVolume(PortFilter portFilter, Predicate<ContainerFluidVolume> fluidFilter) {
        Iterator<ContainerFluidVolume> it = volumes(portFilter, fluidFilter).iterator();
        return it.hasNext() ? it.next() : null;
    }
    
    default ContainerFluidVolume firstVolume(PortFilter portFilter) {
        return firstVolume(portFilter, Predicates.alwaysTrue());
    }
    
    default ContainerFluidVolume firstVolume(Predicate<ContainerFluidVolume> fluidFilter) {
        return firstVolume(PortFilter.ALL, fluidFilter);
    }

    default ContainerFluidVolume firstVolume() {
        return firstVolume(PortFilter.ALL, Predicates.alwaysTrue());
    }
    
    StopNotifier startListening(FluidContainerListener listener, PortFilter portFilter, Predicate<ContainerFluidVolume> filter);
}
