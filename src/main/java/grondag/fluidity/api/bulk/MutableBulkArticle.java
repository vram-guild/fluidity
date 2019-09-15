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
import grondag.fluidity.api.storage.Article;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.Transactor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public final class MutableBulkArticle extends AbstractBulkArticle<MutableFraction> implements Transactor {
    public MutableBulkArticle(CompoundTag tag) {
    	super(Article.fromTag(tag), new MutableFraction(tag));
    }

    public MutableBulkArticle(PacketByteBuf buffer) {
    	super(Article.fromBuffer(buffer), new MutableFraction(buffer));
    }

    public MutableBulkArticle(Article article, FractionView volume) {
    	super(article, new MutableFraction(volume));
    }

    public MutableBulkArticle(AbstractBulkArticle<?> template) {
    	super(template.article(), new MutableFraction(template.volume()));
    }

    public MutableBulkArticle(Article article, long buckets) {
    	super(article, new MutableFraction(buckets));
    }

    public MutableBulkArticle(Article article, long numerator, long divisor) {
        super(article, new MutableFraction(numerator, divisor));
    }

    public MutableBulkArticle(Article article, long buckets, long numerator, long divisor) {
        super(article, new MutableFraction(buckets, numerator, divisor));
    }

    public final void setArticle(Article resource) {
        this.article = resource;
    }

    public final void set(BulkArticleView template) {
        setArticle(template.article());
        volume().set(template.volume());
    }

    @Override
    public final MutableFraction volume() {
        return volume;
    }

    @Override
    public final BulkArticle toImmutable() {
        return new BulkArticle(this);
    }

    public final void readTag(CompoundTag tag) {
        this.article = Article.fromTag(tag);
        this.volume.readTag(tag);
    }

    public final void readBuffer(PacketByteBuf buffer) {
        this.article = Article.fromBuffer(buffer);
        this.volume.readBuffer(buffer);
    }

    public static MutableBulkArticle of(Article resource, FractionView volume) {
        return new MutableBulkArticle(resource, volume);
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
