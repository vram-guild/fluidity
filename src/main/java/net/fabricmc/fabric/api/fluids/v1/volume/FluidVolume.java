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
import net.fabricmc.fabric.api.fluids.v1.fraction.AbstractFraction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.PacketByteBuf;

public abstract class FluidVolume implements FluidVolumeView {
    // Common units
    public static final int BUCKET = 1;
    public static final int KILOLITER = 1;
    public static final int LITER = 1000;
    public static final int BLOCK = 1;
    public static final int SLAB = 2;
    public static final int BOTTLE = 3;
    public static final int QUARTER = 4;
    public static final int INGOT = 9;
    public static final int NUGGET = 81;
    
    protected FluidVariant substance;
    
    @Override
    public final FluidVariant getFluid() {
        return substance;
    }
    
    @Override
    public abstract AbstractFraction volume();

    /**
     * Serializes content to buffer. Recreate instance with {@link #fromBuffer(PacketByteBuf)}.
     * Suitable only for network traffic - assumes raw fluid ID's match on both sides.
     * 
     * @param buf
     */
    public final void writeBuffer(PacketByteBuf buf) {
        buf.writeVarInt(substance.rawId());
        volume().writeBuffer(buf);
    }

    public final void writeTag(CompoundTag tag) {
        tag.putString("substance", substance.id().toString());
        volume().writeTag(tag);
    }
    
    /**
     * Serializes content to NBT tag. Recreate instance with {@link #fromTag(Tag)}.
     * Suitable for world saves. Fluid is serialized as an identifier.
     * 
     * @return NBT tag with contents of instance.
     */
    public final CompoundTag toTag() {
        CompoundTag result = new CompoundTag();
        writeTag(result);
        return result;
    }
    
    /**
     * @return Self if already immutable, otherwise an immutable, exact and complete copy.
     */
    @Override
    public abstract ImmutableFluidVolume toImmutable();
    
    /**
     * @return New mutable instance that is an exact and complete copy of the current instance.
     */
    @Override
    public final MutableFluidVolume mutableCopy() {
        return new MutableFluidVolume(this);
    }
}
