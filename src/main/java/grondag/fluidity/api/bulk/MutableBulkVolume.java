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

import java.util.function.Consumer;

import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.Transactor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public final class MutableBulkVolume extends BulkVolume implements Transactor {

    private final MutableFraction volume;

    public MutableBulkVolume(CompoundTag tag) {
        this.resource = BulkResource.REGISTRY.get(new Identifier(tag.getString("fluid")));
        this.volume = new MutableFraction(tag);
    }

    public MutableBulkVolume(PacketByteBuf buffer) {
        this.resource = BulkResource.REGISTRY.get(buffer.readVarInt());
        this.volume = new MutableFraction(buffer);
    }

    public MutableBulkVolume(BulkResource resource, FractionView volume) {
        this.resource = resource;
        this.volume = new MutableFraction(volume);
    }

    public MutableBulkVolume(BulkVolume template) {
        this.resource = template.resource;
        this.volume = new MutableFraction(template.volume());
    }

    public MutableBulkVolume(BulkResource resource, long buckets) {
        this.resource = resource;
        this.volume = new MutableFraction(buckets);
    }

    public MutableBulkVolume(BulkResource resource, long numerator, long divisor) {
        this.resource = resource;
        this.volume = new MutableFraction(numerator, divisor);
    }

    public MutableBulkVolume(BulkResource resource, long buckets, long numerator, long divisor) {
        this.resource = resource;
        this.volume = new MutableFraction(buckets, numerator, divisor);
    }

    public final void setResource(BulkResource resource) {
        this.resource = resource;
    }

    public final void set(BulkVolumeView template) {
        setResource(template.resource());
        volume().set(template.volume());
    }

    @Override
    public final MutableFraction volume() {
        return volume;
    }

    @Override
    public final ImmutableBulkVolume toImmutable() {
        return new ImmutableBulkVolume(this);
    }

    public final void readTag(CompoundTag tag) {
        this.resource = BulkResource.REGISTRY.get(new Identifier(tag.getString("resource")));
        this.volume.readTag(tag);
    }

    public final void readBuffer(PacketByteBuf buffer) {
        this.resource = BulkResource.REGISTRY.get(buffer.readVarInt());
        this.volume.readBuffer(buffer);
    }

    public static MutableBulkVolume of(BulkResource resource, FractionView volume) {
        return new MutableBulkVolume(resource, volume);
    }

    @Override
    public Consumer<TransactionContext> prepareTx(TransactionContext context) {
        context.setState(this.toImmutable());
        return this::handleTx;
    }

    private void handleTx(TransactionContext context) {
        if (!context.isCommited()) {
            this.set(context.getState());
        }
    }

    @Override
    public final FractionView capacity() {
        return Fraction.MAX_VALUE;
    }
}
