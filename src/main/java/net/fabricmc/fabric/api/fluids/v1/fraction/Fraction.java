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

public final class Fraction extends AbstractFraction {
    public static final Fraction ZERO = new Fraction(0, 0, 1);
    
    public static Fraction of(long whole, long numerator, long divisor) {
        return new Fraction(whole, numerator, divisor);
    }
    
    public static Fraction of(long numerator, long divisor) {
        return new Fraction(numerator, divisor);
    }
    
    public Fraction() {
        super();
    }
    
    public Fraction(long whole) {
        super(whole, 0, 0);
    }
    
    public Fraction(long numerator, long divisor) {
        super(numerator, divisor);
    }
    
    public Fraction(long whole, long numerator, long divisor) {
        super(whole, numerator, divisor);
    }
    
    public Fraction(FractionView template) {
        super(template.whole(), template.numerator(), template.divisor());
    }
    
    public Fraction(Tag tag) {
        readTag((CompoundTag) tag);
    }
    
    public Fraction(PacketByteBuf buf) {
        readBuffer(buf);
    }
}
