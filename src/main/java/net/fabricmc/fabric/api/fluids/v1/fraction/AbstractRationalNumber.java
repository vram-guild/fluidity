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

package net.fabricmc.fabric.api.fluids.v1.fraction;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.PacketByteBuf;

public abstract class AbstractRationalNumber implements RationalNumberView {
    long whole;
    long numerator;
    long divisor;
    
    AbstractRationalNumber() {
        this(0, 0, 1);
    }
    
    AbstractRationalNumber(long whole, long numerator, long divisor) {
        this.whole = whole;
        this.numerator = numerator;
        this.divisor = divisor;
    }
    
    AbstractRationalNumber(long numerator, long divisor) {
        this.whole = numerator / divisor;
        this.numerator = numerator - whole * divisor;
        this.divisor = divisor;
    }
    
    @Override
    public final long whole() {
        return whole;
    }
    
    @Override
    public final long numerator() {
        return numerator;
    }
    
    @Override
    public final long divisor() {
        return divisor;
    }
    
    public final void toBuffer(PacketByteBuf buf) {
        buf.writeVarLong(whole);
        buf.writeVarLong(numerator);
        buf.writeVarLong(divisor);
    }

    public final void writeTag(CompoundTag tag) {
        tag.putLong("wholeUnits", whole);
        tag.putLong("numerator", numerator);
        tag.putLong("denominator", divisor);
    }
    
    public final Tag toTag() {
        CompoundTag result = new CompoundTag();
        writeTag(result);
        return result;
    }
    
    void readBuffer(PacketByteBuf buf) {
        whole = buf.readVarLong();
        numerator = buf.readVarLong();
        divisor = buf.readVarLong();
    }
    
    void readTag(CompoundTag tag) {
        whole = tag.getLong("wholeUnits");
        numerator = tag.getLong("numerator");
        divisor = tag.getLong("denominator");
    }
    
    @Override
    public final boolean equals(Object val) {
        if(val == null || !(val instanceof AbstractRationalNumber)) {
            return false;
        }
        AbstractRationalNumber other = (AbstractRationalNumber)val;
        return other.whole() == this.whole
                && other.numerator() == this.numerator
                && other.divisor() == this.divisor;
    }
    
    @Override
    public final int hashCode() {
        return (int) (HashCommon.mix(whole) ^ HashCommon.mix(numerator ^ divisor));
    }
}
