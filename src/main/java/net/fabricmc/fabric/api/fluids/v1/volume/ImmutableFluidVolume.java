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
import net.fabricmc.fabric.api.fluids.v1.fraction.Fraction;
import net.fabricmc.fabric.api.fluids.v1.fraction.FractionView;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public final class ImmutableFluidVolume extends FluidVolume {
    private final Fraction volume;
    
    public ImmutableFluidVolume(CompoundTag tag) {
        this.substance = FluidVariant.fromId(new Identifier(tag.getString("substance")));
        this.volume = new Fraction(tag);
    }
    
    public ImmutableFluidVolume(PacketByteBuf buffer) {
        this.substance = FluidVariant.fromRawId(buffer.readVarInt());
        this.volume = new Fraction(buffer);
    }
    
    public ImmutableFluidVolume(FluidVariant substance, FractionView volume) {
        this.substance = substance;
        this.volume = new Fraction(volume);
    }
    
    public ImmutableFluidVolume(FluidVolume template) {
        this.substance = template.substance;
        this.volume = new Fraction(template.volume());
    }
    
    public ImmutableFluidVolume(FluidVariant substance, long buckets) {
        this.substance = substance;
        this.volume = new Fraction(buckets);
    }
    
    public ImmutableFluidVolume(FluidVariant substance, long numerator, long divisor) {
        this.substance = substance;
        this.volume = new Fraction(numerator, divisor);
    }
    
    public ImmutableFluidVolume(FluidVariant substance, long buckets, long numerator, long divisor) {
        this.substance = substance;
        this.volume = new Fraction(buckets, numerator, divisor);
    }
    
    @Override
    public final Fraction volume() {
        return volume;
    }

    @Override
    public final ImmutableFluidVolume toImmutable() {
        return this;
    }
    
    public static ImmutableFluidVolume of(FluidVariant substance, FractionView volume) {
        return new ImmutableFluidVolume(substance, volume);
    }
}
