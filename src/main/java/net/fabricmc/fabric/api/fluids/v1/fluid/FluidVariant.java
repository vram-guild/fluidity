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

package net.fabricmc.fabric.api.fluids.v1.fluid;


import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;

/**
 * Purely a WIP stub at this point - represents a game resource measured by volume.
 * Could be a gas, liquid, or flowable solid (powders, dusts, etc.)
 * May or may not have an associated in-world fluid.<p>
 * 
 * This will be a concrete, final type when finished, not an interface.
 */
public interface FluidVariant {
    
    public static final FluidVariant AIR = new FluidVariant() {
        @Override
        public int rawId() {
            return 0;
        }

        @Override
        public Identifier id() {
            return new Identifier("mock");
        }
    };
    
    default Fluid toFluid() {
        return null;
    }
    
    default Tag tag() {
        return null;
    }
    
    default boolean hasTag() {
        return tag() == null;
    }
    
    int rawId();
    
    Identifier id();
    
    static FluidVariant fromRawId(int rawId) {
        return AIR;
    }
    
    static FluidVariant fromId(Identifier id) {
        return AIR;
    }
}
