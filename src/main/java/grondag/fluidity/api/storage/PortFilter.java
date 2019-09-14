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
package grondag.fluidity.api.storage;

import java.util.Set;
import java.util.function.Predicate;

import grondag.fluidity.impl.PortFilterImpl;
import net.minecraft.util.math.Direction;

public interface PortFilter extends Predicate<Port> {
    Set<Direction> sides();

    boolean includeSupply();

    boolean includeAccept();

    static PortFilter ALL = builder().build();
    static PortFilter UP = builder().includeSide(Direction.UP).build();
    static PortFilter DOWN = builder().includeSide(Direction.DOWN).build();
    static PortFilter NORTH = builder().includeSide(Direction.NORTH).build();
    static PortFilter SOUTH = builder().includeSide(Direction.SOUTH).build();
    static PortFilter EAST = builder().includeSide(Direction.EAST).build();
    static PortFilter WEST = builder().includeSide(Direction.WEST).build();
    static PortFilter ALL_SUPPLY = builder().excludeAccept().build();
    static PortFilter ALL_ACCEPT = builder().excludeSupply().build();

    static interface Builder {
        Builder includeSide(Direction side);

        Builder excludeSide(Direction side);

        Builder excludeSupply();

        Builder excludeAccept();

        PortFilter build();
    }

    static Builder builder() {
        return PortFilterImpl.builder();
    }
}
