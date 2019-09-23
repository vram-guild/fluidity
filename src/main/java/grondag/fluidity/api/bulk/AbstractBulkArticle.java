package grondag.fluidity.api.bulk;

///*******************************************************************************
// * Copyright 2019 grondag
// * 
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License.  You may obtain a copy
// * of the License at
// * 
// *   http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// * License for the specific language governing permissions and limitations under
// * the License.
// ******************************************************************************/
//
//package grondag.fluidity.api.article;
//
//import grondag.fluidity.api.bulk.BulkArticleView;
//import grondag.fluidity.api.fraction.AbstractFraction;
//import grondag.fluidity.impl.AbstractStoredArticle;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.util.PacketByteBuf;
//
//public abstract class AbstractBulkArticle<T extends AbstractFraction, V> extends AbstractStoredArticle<V> implements BulkArticleView<V> {
//    // Common units
//    public static final int BUCKET = 1;
//    public static final int KILOLITER = 1;
//    public static final int LITER = 1000;
//    public static final int BLOCK = 1;
//    public static final int SLAB = 2;
//    public static final int BOTTLE = 3;
//    public static final int QUARTER = 4;
//    public static final int INGOT = 9;
//    public static final int NUGGET = 81;
//
//    protected final T volume;
//    
//    protected AbstractBulkArticle(V article, T volume) {
//    	super(article);
//    	this.volume = volume;
//    }
//
//	protected AbstractBulkArticle(PacketByteBuf buf, T volume) {
//		super(buf);
//		this.volume = volume;
//	}
//	
//	protected AbstractBulkArticle(CompoundTag tag, T volume) {
//		super(tag);
//		this.volume = volume;
//	}
//	
//    @Override
//	public final void writeBuffer(PacketByteBuf buf) {
//        super.writeBuffer(buf);
//        volume().writeBuffer(buf);
//    }
//
//    @Override
//	public final void writeTag(CompoundTag tag) {
//        super.writeTag(tag);
//        volume().writeTag(tag);
//    }
//
//    @Override
//    public abstract T volume();
//    
//    /**
//     * @return Self if already immutable, otherwise an immutable, exact and complete
//     *         copy.
//     */
//    @Override
//    public abstract BulkArticle<V> toImmutable();
//
//    /**
//     * @return New mutable instance that is an exact and complete copy of the
//     *         current instance.
//     */
//    @Override
//    public final MutableBulkArticle<V> mutableCopy() {
//        return new MutableBulkArticle<V>(this);
//    }
//}
