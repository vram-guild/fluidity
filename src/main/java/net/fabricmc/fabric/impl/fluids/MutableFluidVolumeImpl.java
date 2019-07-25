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

import net.fabricmc.fabric.api.fluids.v1.ImmutableFluidVolume;
import net.fabricmc.fabric.api.fluids.v1.MutableFluidVolume;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

public class MutableFluidVolumeImpl extends AbstractMutableFluidVolume implements MutableFluidVolume {
    public static MutableFluidVolume create(Fluid fluid, long volume, long capacity, long units) {
        return new MutableFluidVolumeImpl(fluid, volume, capacity, units);
    }
    
    public static MutableFluidVolume fromBuffer(PacketByteBuf buf) {
        return new MutableFluidVolumeImpl(buf);
    }
    
    public static MutableFluidVolume fromTag(Tag tag) {
        return new MutableFluidVolumeImpl((CompoundTag)tag);
    }
    
    protected MutableFluidVolumeImpl(AbstractFluidVolume template) {
        super(template);
    }
    
    protected MutableFluidVolumeImpl(Fluid fluid, long volume, long capacity, long units) {
        super(fluid);
        this.capacity = capacity;
        this.baseUnit = units;
        this.setVolume(volume, units);
    }
    
    protected MutableFluidVolumeImpl(PacketByteBuf buf) {
        super(Registry.FLUID.get(buf.readVarInt()));
        this.capacity = buf.readVarLong();
        this.baseUnit = buf.readVarLong();
        this.set(buf.readVarLong(), buf.readVarLong(), buf.readVarLong());
    }
    
    protected MutableFluidVolumeImpl(CompoundTag tag) {
        super(Registry.FLUID.get(new Identifier(tag.getString("fluid"))));
        this.capacity = tag.getLong("capacity");
        this.baseUnit = tag.getLong("baseUnit");
        this.set(tag.getLong("wholeUnits"), tag.getLong("numerator"), tag.getLong("denominator"));
    }
    
    @Override
    public ImmutableFluidVolume toImmutable() {
        return new ImmutableFluidVolumeImpl(this);
    }

    @Override
    public MutableFluidVolume mutableCopy() {
        return new MutableFluidVolumeImpl(this);
    }
}
