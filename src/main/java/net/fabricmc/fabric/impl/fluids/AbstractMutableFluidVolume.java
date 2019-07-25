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

package net.fabricmc.fabric.impl.fluids;

import net.fabricmc.fabric.api.fluids.v1.MutableFluidVolume;
import net.minecraft.fluid.Fluid;

public abstract class AbstractMutableFluidVolume extends AbstractFluidVolume implements MutableFluidVolume {

    protected AbstractMutableFluidVolume(Fluid fluid) {
        super(fluid);
    }

    protected AbstractMutableFluidVolume(AbstractFluidVolume template) {
        super(template);
    }
    
    public MutableFluidVolume setCapacity(long capacity, long baseUnits) {
        this.capacity = capacity;
        this.baseUnit = baseUnits;
        return this;
    }

    public MutableFluidVolume setVolume(long volume, long units) {
        final long whole = volume / units;
        final long num = volume - whole * units;
        set(whole, num, units);
        return this;
    }

    protected long computeDrainResult(long volume, long units) {
        return Math.max(0, Math.min(volume, scaleTo(units)));
    }
    
    public long drain(long volume, long units, boolean simulate) {
        final long result = computeDrainResult(volume, units);
        if(!simulate) {
            subtract(result, units);
        }
        return result;
    }

    public boolean drainExactly(long volume, long units, boolean simulate) {
        final long result = computeDrainResult(volume, units);
        if(result == volume) {
            if(!simulate) {
                subtract(result, units);
            }
            return true;
        } else {
            return false;
        }
    }

    protected long computeFillResult(long volume, long units) {
        final long scaledCap = this.capacity * units;
        final long scaledUnits = this.baseUnit * units;
        final long scaledVolume = volume * this.baseUnit;
        final long availableSpace = scaledCap - scaleTo(scaledUnits);
        return Math.max(0, Math.min(scaledVolume, availableSpace));
    }
    
    public long fill(long volume, long units, boolean simulate) {
        final long result = computeFillResult(volume, units);
        if(!simulate) {
            add(result, units);
        }
        return result;
    }

    public boolean fillExactly(long volume, long units, boolean simulate) {
        final long result = computeFillResult(volume, units);
        if(result == volume) {
            if(!simulate) {
                add(result, units);
            }
            return true;
        } else {
            return false;
        }
    }
}
