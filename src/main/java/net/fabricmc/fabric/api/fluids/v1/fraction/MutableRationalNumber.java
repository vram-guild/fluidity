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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.PacketByteBuf;

/**
 * The math bits of this are placeholders - Player has one coming along that looks to be
 * quite nice so no need to duplicate his efforts.  Thus, there are none of the 
 * various checks and optimizations that ideally would be here. Also very little testing.
 */
public final class MutableRationalNumber extends AbstractRationalNumber {
    public MutableRationalNumber() {
        super();
    }
    
    public MutableRationalNumber(long whole) {
        super(whole, 0, 0);
    }
    
    public MutableRationalNumber(long numerator, long divisor) {
        super(numerator, divisor);
    }
    
    public MutableRationalNumber(long whole, long numerator, long divisor) {
        super(whole, numerator, divisor);
    }
    
    public MutableRationalNumber(RationalNumberView template) {
        super(template.whole(), template.numerator(), template.divisor());
    }
    
    public MutableRationalNumber(Tag tag) {
        readTag((CompoundTag) tag);
    }
    
    public MutableRationalNumber(PacketByteBuf buf) {
        readBuffer(buf);
    }
    public final void set(long whole) {
        this.set(whole, 0, 1);
    }
    
    public final void set(long numerator, long divisor) {
        this.whole = numerator / divisor;
        this.numerator = numerator - whole * divisor;
        this.divisor = divisor;
    }
    
    public final void set(long whole, long numerator, long divisor) {
        this.whole = whole;
        this.numerator = numerator;
        this.divisor = divisor;
    }
    
    public final void set(RationalNumber template) {
        this.whole = template.whole;
        this.numerator = template.numerator;
        this.divisor = template.divisor;
    }
   
    public final void add(RationalNumber val) {
        add(val.whole, val.numerator, val.divisor);
    }
    
    public final void add(long whole, long numerator, long divisor) {
        this.whole += whole;
        final long n = this.numerator * divisor + numerator * this.divisor;
        if(n != 0) {
            final long d = divisor * this.divisor;
            this.numerator = n;
            this.divisor = d;
            simplify();
            normalize();
        }
    }
    
    public final void subtract(RationalNumber val) {
        subtract(val.whole, val.numerator, val.divisor);
    }
    
    public final void subtract(long whole, long numerator, long divisor) {
        this.whole -= whole;
        
        final long n = this.numerator * divisor - numerator * this.divisor;
        if(n != 0) {
            final long d = divisor * this.divisor;
            this.numerator = n;
            if(this.numerator < 0) {
                this.numerator += d;
                this.whole -= 1;
            }
            this.divisor = d;
            simplify();
            normalize();
        }
    }
    
    private void simplify() {
        // remove powers of two bitwise
        final int twos = Long.numberOfTrailingZeros(numerator | divisor);
        if(twos > 0) {
            numerator >>= twos;
            divisor >>= twos;
        }
        
        // use conventional gcd for rest
        long gcd = gcd(numerator, divisor);
        if(gcd != divisor) {
            numerator /= gcd;
            divisor /= gcd;
        }
    }
    
    private void normalize() {
        if(numerator > divisor) {
            final long n = numerator / divisor;
            whole += n;
            numerator -= n * divisor;
        }
    }
    
    private long gcd(long a, long b) {
        while(b != 0) {
           long t = b; 
           b = a % b; 
           a = t; 
        }
        return a;
    }
    
    @Override
    public final void readBuffer(PacketByteBuf buf) {
        whole = buf.readVarLong();
        numerator = buf.readVarLong();
        divisor = buf.readVarLong();
    }
    
    @Override
    public final void readTag(CompoundTag tag) {
        whole = tag.getLong("wholeUnits");
        numerator = tag.getLong("numerator");
        divisor = tag.getLong("denominator");
    }
}
