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

import java.util.function.Consumer;

import net.fabricmc.fabric.api.fluids.v1.container.ContainerFluidVolume;
import net.fabricmc.fabric.api.fluids.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.fluids.v1.transact.FluidTx.Context;
import net.fabricmc.fabric.api.fluids.v1.transact.FluidTxActor;
import net.fabricmc.fabric.api.fluids.v1.volume.fraction.Fraction;
import net.fabricmc.fabric.api.fluids.v1.volume.fraction.FractionView;
import net.fabricmc.fabric.api.fluids.v1.volume.fraction.MutableFraction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public final class MutableFluidVolume extends FluidVolume implements FluidTxActor {
    
    private final MutableFraction volume;
    
    public MutableFluidVolume(CompoundTag tag) {
        this.substance = FluidVariant.fromId(new Identifier(tag.getString("substance")));
        this.volume = new MutableFraction(tag);
    }
    
    public MutableFluidVolume(PacketByteBuf buffer) {
        this.substance = FluidVariant.fromRawId(buffer.readVarInt());
        this.volume = new MutableFraction(buffer);
    }
    
    public MutableFluidVolume(FluidVariant substance, FractionView volume) {
        this.substance = substance;
        this.volume = new MutableFraction(volume);
    }
    
    public MutableFluidVolume(FluidVolume template) {
        this.substance = template.substance;
        this.volume = new MutableFraction(template.volume());
    }
    
    public MutableFluidVolume(FluidVariant substance, long buckets) {
        this.substance = substance;
        this.volume = new MutableFraction(buckets);
    }
    
    public MutableFluidVolume(FluidVariant substance, long numerator, long divisor) {
        this.substance = substance;
        this.volume = new MutableFraction(numerator, divisor);
    }
    
    public MutableFluidVolume(FluidVariant substance, long buckets, long numerator, long divisor) {
        this.substance = substance;
        this.volume = new MutableFraction(buckets, numerator, divisor);
    }
    
    public final void setFluid(FluidVariant substance) {
        this.substance = substance;
    }
 
    public final void set(ContainerFluidVolume template) {
        setFluid(template.getFluid());
        volume().set(template.volume());
    }
    
    @Override
    public final MutableFraction volume() {
        return volume;
    }

    @Override
    public final ImmutableFluidVolume toImmutable() {
        return new ImmutableFluidVolume(this);
    }

    public final void readTag(CompoundTag tag) {
        this.substance = FluidVariant.fromId(new Identifier(tag.getString("substance")));
        this.volume.readTag(tag);
    }
    
    public final void readBuffer(PacketByteBuf buffer) {
        this.substance = FluidVariant.fromRawId(buffer.readVarInt());
        this.volume.readBuffer(buffer);
    }
    
    public static MutableFluidVolume of(FluidVariant substance, FractionView volume) {
        return new MutableFluidVolume(substance, volume);
    }

    @Override
    public Consumer<Context> prepareTx(Context context) {
        context.setState(this.toImmutable());
        return this::handleTx;
    }
    
    private void handleTx(Context context) {
        if(!context.isCommited()) {
            this.set(context.getState());
        }
    }

    @Override
    public final FractionView capacity() {
        return Fraction.MAX_VALUE;
    }

}
