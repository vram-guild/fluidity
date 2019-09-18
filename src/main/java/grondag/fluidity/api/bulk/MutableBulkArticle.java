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
//package grondag.fluidity.api.bulk;
//
//import java.util.function.Consumer;
//
//import grondag.fluidity.api.fraction.Fraction;
//import grondag.fluidity.api.fraction.FractionView;
//import grondag.fluidity.api.fraction.MutableFraction;
//import grondag.fluidity.api.transact.TransactionContext;
//import grondag.fluidity.api.transact.Transactor;
//import grondag.fluidity.impl.AbstractBulkArticle;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.util.PacketByteBuf;
//
//public final class MutableBulkArticle<V> extends AbstractBulkArticle<MutableFraction, V> implements Transactor {
//    public MutableBulkArticle(CompoundTag tag) {
//    	super(tag, new MutableFraction(tag));
//    }
//
//    public MutableBulkArticle(PacketByteBuf buffer) {
//    	super(buffer, new MutableFraction(buffer));
//    }
//
//    public MutableBulkArticle(V article, FractionView volume) {
//    	super(article, new MutableFraction(volume));
//    }
//
//    public MutableBulkArticle(BulkArticleView<V> template) {
//    	super(template.article(), new MutableFraction(template.volume()));
//    }
//
//    public MutableBulkArticle(V article, long buckets) {
//    	super(article, new MutableFraction(buckets));
//    }
//
//    public MutableBulkArticle(V article, long numerator, long divisor) {
//        super(article, new MutableFraction(numerator, divisor));
//    }
//
//    public MutableBulkArticle(V article, long buckets, long numerator, long divisor) {
//        super(article, new MutableFraction(buckets, numerator, divisor));
//    }
//
//    @Override
//	public final void setArticle(V article) {
//        super.setArticle(article);
//    }
//
//    public final void set(BulkArticleView<V> template) {
//        setArticle(template.article());
//        volume().set(template.volume());
//    }
//
//    @Override
//    public final MutableFraction volume() {
//        return volume;
//    }
//
//    @Override
//    public final BulkArticle<V> toImmutable() {
//        return new BulkArticle<V>(this);
//    }
//
//    @Override
//	public final void readTag(CompoundTag tag) {
//    	super.readTag(tag);
//        this.volume.readTag(tag);
//    }
//
//    @Override
//	public final void readBuffer(PacketByteBuf buffer) {
//    	super.readBuffer(buffer);
//        this.volume.readBuffer(buffer);
//    }
//
//    public static <T> MutableBulkArticle<T> of(T resource, FractionView volume) {
//        return new MutableBulkArticle<T>(resource, volume);
//    }
//
//    @Override
//    public Consumer<TransactionContext> prepareTx(TransactionContext context) {
//        context.setState(this.toImmutable());
//        return this::handleTx;
//    }
//
//    private void handleTx(TransactionContext context) {
//        if (!context.isCommited()) {
//            this.set(context.getState());
//        }
//    }
//
//    @Override
//    public final FractionView capacity() {
//        return Fraction.MAX_VALUE;
//    }
//}
