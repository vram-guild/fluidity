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

import net.fabricmc.fabric.api.fluids.v1.fraction.RationalNumber;
import net.fabricmc.fabric.api.fluids.v1.fraction.RationalNumberView;
import net.fabricmc.fabric.api.fluids.v1.substance.VolumetricSubstance;

public final class ImmutableSubstanceVolume extends SubstanceVolume {
    private final RationalNumber volume;
    
    public ImmutableSubstanceVolume(VolumetricSubstance substance, RationalNumberView volume) {
        this.substance = substance;
        this.volume = new RationalNumber(volume);
    }
    
    public ImmutableSubstanceVolume(SubstanceVolume template) {
        this.substance = template.substance;
        this.volume = new RationalNumber(template.volume());
    }
    
    public ImmutableSubstanceVolume(VolumetricSubstance substance, long buckets) {
        this.substance = substance;
        this.volume = new RationalNumber(buckets);
    }
    
    public ImmutableSubstanceVolume(VolumetricSubstance substance, long numerator, long divisor) {
        this.substance = substance;
        this.volume = new RationalNumber(numerator, divisor);
    }
    
    public ImmutableSubstanceVolume(VolumetricSubstance substance, long buckets, long numerator, long divisor) {
        this.substance = substance;
        this.volume = new RationalNumber(buckets, numerator, divisor);
    }
    
    @Override
    public RationalNumber volume() {
        return volume;
    }

    @Override
    public ImmutableSubstanceVolume toImmutable() {
        return this;
    }
}
