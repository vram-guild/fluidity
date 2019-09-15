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

import java.util.function.Consumer;

import grondag.fluidity.api.storage.Article;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.Transactor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public final class MutableDiscreteArticle extends AbstractDiscreteArticle implements Transactor {
    public MutableDiscreteArticle(CompoundTag tag) {
        super(tag);
    }

    public MutableDiscreteArticle(PacketByteBuf buffer) {
        super(buffer);
    }

    public MutableDiscreteArticle(Article article, long count) {
        super(article, count);
    }

    public MutableDiscreteArticle(AbstractDiscreteArticle template) {
        super(template);
    }


    public final void setArticle(Article article) {
        this.article = article;
    }
    
    public final void setCount(long count) {
    	//TODO: guards
    	this.count = count;
    }

    public final void increment(long count) {
    	//TODO: guards
    	this.count += count;
    }
    
    public final void decrement(long count) {
    	//TODO: guards
    	this.count -= count;
    }
    
    public final void set(AbstractDiscreteArticle template) {
        setArticle(template.article());
        count = template.count();
    }

    @Override
    public final DiscreteArticle toImmutable() {
        return new DiscreteArticle(this);
    }

    public static MutableDiscreteArticle of(Article resource, long count) {
        return new MutableDiscreteArticle(resource, count);
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
    public final long capacity() {
        return Long.MAX_VALUE;
    }
}
