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
import grondag.fluidity.api.storage.Article;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public final class BulkArticle extends AbstractBulkArticle<Fraction> {
    public BulkArticle(CompoundTag tag) {
    	super(Article.fromTag(tag), new Fraction(tag));
    }

    public BulkArticle(PacketByteBuf buffer) {
    	super(Article.fromBuffer(buffer), new Fraction(buffer));
    }

    public BulkArticle(Article article, FractionView volume) {
    	super(article, new Fraction(volume));
    }

    public BulkArticle(AbstractBulkArticle<?> template) {
    	super(template.article(), new Fraction(template.volume()));
    }

    public BulkArticle(Article article, long buckets) {
    	super(article, new Fraction(buckets));
    }

    public BulkArticle(Article article, long numerator, long divisor) {
    	super(article, new Fraction(numerator, divisor));
    }

    public BulkArticle(Article article, long buckets, long numerator, long divisor) {
    	super(article, new Fraction(buckets, numerator, divisor));
    }

    @Override
    public final Fraction volume() {
        return volume;
    }

    @Override
    public final BulkArticle toImmutable() {
        return this;
    }

    public static BulkArticle of(Article resource, FractionView volume) {
        return new BulkArticle(resource, volume);
    }

    @Override
    public FractionView capacity() {
        return volume();
    }
}
