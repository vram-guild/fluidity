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

import net.fabricmc.fabric.api.fluids.v1.fraction.MutableRationalNumber;
import net.fabricmc.fabric.api.fluids.v1.fraction.RationalNumberView;
import net.fabricmc.fabric.api.fluids.v1.substance.VolumetricSubstance;

public final class MutableSubstanceVolume extends SubstanceVolume {
    private final MutableRationalNumber volume;
    
    public MutableSubstanceVolume(VolumetricSubstance substance, RationalNumberView volume) {
        this.substance = substance;
        this.volume = new MutableRationalNumber(volume);
    }
    
    public MutableSubstanceVolume(SubstanceVolume template) {
        this.substance = template.substance;
        this.volume = new MutableRationalNumber(template.volume());
    }
    
    public MutableSubstanceVolume(VolumetricSubstance substance, long buckets) {
        this.substance = substance;
        this.volume = new MutableRationalNumber(buckets);
    }
    
    public MutableSubstanceVolume(VolumetricSubstance substance, long numerator, long divisor) {
        this.substance = substance;
        this.volume = new MutableRationalNumber(numerator, divisor);
    }
    
    public MutableSubstanceVolume(VolumetricSubstance substance, long buckets, long numerator, long divisor) {
        this.substance = substance;
        this.volume = new MutableRationalNumber(buckets, numerator, divisor);
    }
    
    public final void setSubstance(VolumetricSubstance substance) {
        this.substance = substance;
    }
 
    @Override
    public final MutableRationalNumber volume() {
        return volume;
    }

    @Override
    public final ImmutableSubstanceVolume toImmutable() {
        return new ImmutableSubstanceVolume(this);
    }

}
