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

package net.fabricmc.fabric.api.fluids.v1.container;

import net.fabricmc.fabric.api.fluids.v1.fraction.RationalNumber;
import net.minecraft.fluid.Fluid;

public abstract class AbstractMutableFluidVolume extends AbstractFluidVolume implements MutableFluidVolume {

    protected AbstractMutableFluidVolume(Fluid fluid) {
        super(fluid);
    }

    protected AbstractMutableFluidVolume(AbstractFluidVolume template) {
        super(template);
    }
    
    @Override
    public AbstractMutableFluidVolume setCapacity(long whole, long fraction, long baseUnits) {
        this.capacity.set(whole, fraction, baseUnits);
        return this;
    }

    @Override
    public AbstractMutableFluidVolume set(long whole, long fraction, long units) {
        //TODO: add capacity check
        super.set(whole, fraction, units);
    }

    protected long computeDrainResult(long volume, long units) {
        return Math.max(0, Math.min(volume, scaleTo(units)));
    }
    
    @Override
    public long drain(long volume, long units, boolean simulate) {
        final long result = computeDrainResult(volume, units);
        if(!simulate) {
            subtract(result, units);
        }
        return result;
    }

    @Override
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
    
    @Override
    public long fill(long volume, long units, boolean simulate) {
        final long result = computeFillResult(volume, units);
        if(!simulate) {
            add(result, units);
        }
        return result;
    }

    @Override
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

    @Override
    public MutableFluidVolume set(RationalNumber val) {
        // TODO Auto-generated method stub
        return MutableFluidVolume.super.set(val);
    }
}
