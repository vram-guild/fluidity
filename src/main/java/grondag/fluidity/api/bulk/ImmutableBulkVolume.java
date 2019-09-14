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

package grondag.fluidity.api.bulk;

import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public final class ImmutableBulkVolume extends BulkVolume {
    private final Fraction volume;

    public ImmutableBulkVolume(CompoundTag tag) {
        this.resource = BulkResource.REGISTRY.get(new Identifier(tag.getString("resource")));
        this.volume = new Fraction(tag);
    }

    public ImmutableBulkVolume(PacketByteBuf buffer) {
        this.resource = BulkResource.REGISTRY.get(buffer.readVarInt());
        this.volume = new Fraction(buffer);
    }

    public ImmutableBulkVolume(BulkResource resource, FractionView volume) {
        this.resource = resource;
        this.volume = new Fraction(volume);
    }

    public ImmutableBulkVolume(BulkVolume template) {
        this.resource = template.resource;
        this.volume = new Fraction(template.volume());
    }

    public ImmutableBulkVolume(BulkResource resource, long buckets) {
        this.resource = resource;
        this.volume = new Fraction(buckets);
    }

    public ImmutableBulkVolume(BulkResource resource, long numerator, long divisor) {
        this.resource = resource;
        this.volume = new Fraction(numerator, divisor);
    }

    public ImmutableBulkVolume(BulkResource resource, long buckets, long numerator, long divisor) {
        this.resource = resource;
        this.volume = new Fraction(buckets, numerator, divisor);
    }

    @Override
    public final Fraction volume() {
        return volume;
    }

    @Override
    public final ImmutableBulkVolume toImmutable() {
        return this;
    }

    public static ImmutableBulkVolume of(BulkResource resource, FractionView volume) {
        return new ImmutableBulkVolume(resource, volume);
    }

    @Override
    public FractionView capacity() {
        return volume();
    }
}
