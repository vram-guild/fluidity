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

package grondag.fluidity.impl;

import grondag.fluidity.api.discrete.DiscreteArticle;
import grondag.fluidity.api.discrete.DiscreteArticleView;
import grondag.fluidity.api.discrete.MutableDiscreteArticle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public abstract class AbstractDiscreteArticle<V> extends AbstractStoredArticle<V> implements DiscreteArticleView<V> {
	protected long count;
	
    protected AbstractDiscreteArticle(V article, long count) {
    	super(article);
    	this.count = count;
    }

	protected AbstractDiscreteArticle(PacketByteBuf buf) {
		super(buf);
		this.count = buf.readVarLong();
	}
	
	protected AbstractDiscreteArticle(CompoundTag tag) {
		super(tag);
		this.count = tag.getLong("count");
	}

    protected AbstractDiscreteArticle(DiscreteArticleView<V> template) {
        super(template.article());
        this.count = template.count();
    }
    
    @Override
    public long count() {
    	return count;
    }

    @Override
	public final void writeBuffer(PacketByteBuf buf) {
        super.writeBuffer(buf);
        buf.writeVarLong(count());
    }

    @Override
	public final void writeTag(CompoundTag tag) {
        super.writeTag(tag);
        tag.putLong("count", count());
    }

    /**
     * @return Self if already immutable, otherwise an immutable, exact and complete
     *         copy.
     */
    @Override
    public abstract DiscreteArticle<V> toImmutable();

    /**
     * @return New mutable instance that is an exact and complete copy of the
     *         current instance.
     */
    @Override
    public final MutableDiscreteArticle<V> mutableCopy() {
        return new MutableDiscreteArticle<V>(this);
    }
}
