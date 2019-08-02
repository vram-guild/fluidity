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

package grondag.fluidity.api.fluid.volume.fraction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.PacketByteBuf;

/**
 * Should be accurate but not necessarily performant and does not offer advanced features. 
 * Player has one coming along that looks to be quite nice so no need to duplicate his efforts.  Thus, there are none of the 
 */
public final class MutableFraction extends AbstractFraction {
    public static MutableFraction of(long whole, long numerator, long divisor) {
        return new MutableFraction(whole, numerator, divisor);
    }
    
    public static MutableFraction of(long numerator, long divisor) {
        return new MutableFraction(numerator, divisor);
    }
    
    public static MutableFraction of(long whole) {
        return new MutableFraction(whole);
    }
    
    public MutableFraction() {
        super();
    }
    
    public MutableFraction(long whole) {
        super(whole, 0, 1);
    }
    
    public MutableFraction(long numerator, long divisor) {
        super(numerator, divisor);
    }
    
    public MutableFraction(long whole, long numerator, long divisor) {
        super(whole, numerator, divisor);
    }
    
    public MutableFraction(FractionView template) {
        super(template.whole(), template.numerator(), template.divisor());
    }
    
    public MutableFraction(Tag tag) {
        readTag((CompoundTag) tag);
    }
    
    public MutableFraction(PacketByteBuf buf) {
        readBuffer(buf);
    }
    public final void set(long whole) {
        this.set(whole, 0, 1);
    }
    
    public final void set(long numerator, long divisor) {
        validate(0, numerator, divisor);
        this.whole = numerator / divisor;
        this.numerator = numerator - whole * divisor;
        this.divisor = divisor;
    }
    
    public final void set(long whole, long numerator, long divisor) {
        validate(whole, numerator, divisor);
        this.whole = whole;
        this.numerator = numerator;
        this.divisor = divisor;
    }
    
    public final void set(FractionView template) {
        this.whole = template.whole();
        this.numerator = template.numerator();
        this.divisor = template.divisor();
    }
   
    public final void add(FractionView val) {
        add(val.whole(), val.numerator(), val.divisor());
    }
    
    public final void add(long whole) {
        add(whole, 0, 1);
    }
    
    public final void add(long numerator, long divisor) {
        add(0, numerator, divisor);
    }
    
    public final void add(long whole, long numerator, long divisor) {
        validate(whole, numerator, divisor);
        this.whole += whole;
        
        if(Math.abs(numerator) >= divisor) {
            final long w = numerator / divisor;
            this.whole += w;
            numerator -= w * divisor;
        }
        
        final long n = this.numerator * divisor + numerator * this.divisor;
        if(n != 0) {
            this.numerator = n;
            this.divisor = divisor * this.divisor;
            normalize();
        }
    }
    
    public final void subtract(FractionView val) {
        add(-val.whole(), -val.numerator(), val.divisor());
    }
    
    public final void subtract(long whole) {
        add(-whole, 0, 1);
    }
    
    public final void subtract(long numerator, long divisor) {
        add(0, -numerator, divisor);
    }
    
    @Override
    public final void readBuffer(PacketByteBuf buffer) {
        whole = buffer.readVarLong();
        numerator = buffer.readVarLong();
        divisor = buffer.readVarLong();
    }
    
    @Override
    public final void readTag(CompoundTag tag) {
        whole = tag.getLong("wholeUnits");
        numerator = tag.getLong("numerator");
        divisor = tag.getLong("denominator");
    }
}
