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

import net.fabricmc.fabric.api.fluids.v1.MutableRationalNumber;

//TODO: a bit sloppy and rushed - needs input checks and better handling of edge cases

public abstract class AbstractRationalNumber implements MutableRationalNumber {
    private long whole;
    private long numerator;
    private long divisor;
    
    @Override
    public long wholeUnits() {
        return whole;
    }
    
    @Override
    public long numerator() {
        return numerator;
    }
    
    @Override
    public long divisor() {
        return divisor;
    }
    
    @Override
    public void set(long whole, long numerator, long divisor) {
        this.whole = whole;
        this.numerator = numerator;
        this.divisor = divisor;
    }
    
    protected void add(long numeratorIn, long divisorIn) {
        //TODO: handle negative inputs, zeros, possibly combine with subtract
        final long w = numeratorIn / divisorIn;
        if(w != 0) {
            numeratorIn -= w * divisorIn;
            whole += w;
        }
        
        final long n = this.numerator * divisorIn + numeratorIn * this.divisor;
        if(n != 0) {
            final long d = divisorIn * this.divisor;
            this.numerator = n;
            this.divisor = d;
            simplify();
            normalize();
        }
    }
    
    protected void subtract(long numeratorIn, long divisorIn) {
        //TODO: handle negative inputs, zeros, possibly combine with add
        final long w = numeratorIn / divisorIn;
        if(w != 0) {
            numeratorIn -= w * divisorIn;
            whole -= w;
        }
        
        final long n = this.numerator * divisorIn - numeratorIn * this.divisor;
        if(n != 0) {
            final long d = divisorIn * this.divisor;
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
    
    protected long scaleTo(long divisorIn) {
        divisorIn = Math.abs(divisorIn);
        final long base = whole * divisorIn;
        
        if(numerator == 0) {
            return base;
        } else if(divisor == divisorIn) {
            return base + numerator / divisor;
        } else {
            return base + numerator * divisorIn / divisor;
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
}
