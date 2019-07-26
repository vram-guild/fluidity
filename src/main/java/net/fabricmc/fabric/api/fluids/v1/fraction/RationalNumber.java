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

public final class RationalNumber extends AbstractRationalNumber {
    public RationalNumber() {
        super();
    }
    
    public RationalNumber(long whole) {
        super(whole, 0, 0);
    }
    
    public RationalNumber(long numerator, long divisor) {
        super(numerator, divisor);
    }
    
    public RationalNumber(long whole, long numerator, long divisor) {
        super(whole, numerator, divisor);
    }
    
    public RationalNumber(RationalNumberView template) {
        super(template.whole(), template.numerator(), template.divisor());
    }
    
    public RationalNumber(Tag tag) {
        readTag((CompoundTag) tag);
    }
    
    public RationalNumber(PacketByteBuf buf) {
        readBuffer(buf);
    }
}
