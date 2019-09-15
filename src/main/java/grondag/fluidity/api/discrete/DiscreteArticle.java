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

package grondag.fluidity.api.discrete;

import grondag.fluidity.api.storage.Article;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public final class DiscreteArticle extends AbstractDiscreteArticle {
    public DiscreteArticle(CompoundTag tag) {
        super(tag);
    }

    public DiscreteArticle(PacketByteBuf buffer) {
        super(buffer);
    }

    public DiscreteArticle(Article article, long count) {
        super(article, count);
    }

    public DiscreteArticle(AbstractDiscreteArticle template) {
        super(template);
    }

    @Override
    public final DiscreteArticle toImmutable() {
        return this;
    }

    public static DiscreteArticle of(Article article, long count) {
        return new DiscreteArticle(article, count);
    }

    @Override
    public long capacity() {
        return count();
    }
}
