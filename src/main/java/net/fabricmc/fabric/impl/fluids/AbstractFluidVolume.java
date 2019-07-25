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

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

public abstract class AbstractFluidVolume extends AbstractRationalNumber {
    protected final Fluid fluid;
    protected long baseUnit = 1;
    protected long capacity = 1;
    
    protected AbstractFluidVolume(Fluid fluid) {
        this.fluid = fluid;
    }
    
    protected AbstractFluidVolume(AbstractFluidVolume template) {
        this.fluid = template.fluid;
        this.baseUnit = template.baseUnit;
        this.capacity = template.capacity;
        this.set(wholeUnits(), numerator(), divisor());
    }
    
    public Fluid fluid() {
        return fluid;
    }

    public double volumeForDisplay(long units) {
        //start with unit scale
        double base = (double)numerator() / (double)divisor() + (double)wholeUnits();
        
        //scale to requested unit
        return units == 1 ? base : base / (double)units;
    }

    public long getVolume(long units) {
        return scaleTo(units);
    }

    public boolean isEmpty() {
        return wholeUnits() == 0 && numerator() == 0;
    }

    public boolean isEmpty(long units) {
        return scaleTo(units) == 0;
    }

    public long getCapacity() {
        return capacity;
    }

    public long getBaseUnit() {
        return baseUnit;
    }
    
    public void toBuffer(PacketByteBuf buf) {
        buf.writeVarInt(Registry.FLUID.getRawId(fluid));
        buf.writeVarLong(capacity);
        buf.writeVarLong(baseUnit);
        buf.writeVarLong(wholeUnits());
        buf.writeVarLong(numerator());
        buf.writeVarLong(divisor());
    }

    public Tag toTag() {
        CompoundTag result = new CompoundTag();
        result.putString("fluid", Registry.FLUID.getId(fluid).toString());
        result.putLong("capacity", capacity);
        result.putLong("baseUnit", baseUnit);
        result.putLong("wholeUnits", wholeUnits());
        result.putLong("numerator", numerator());
        result.putLong("denominator", divisor());
        return result;
    }
}
