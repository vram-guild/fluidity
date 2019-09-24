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
package grondag.fluidity.api.bulk;

import grondag.fluidity.api.storage.ArticleView;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public abstract class AbstractStoredArticle<T> implements ArticleView {
	private T article;
	
	protected AbstractStoredArticle(T article) {
		setArticle(article);
	}
	
	protected AbstractStoredArticle(PacketByteBuf buf) {
		readBuffer(buf);
	}
	
	protected AbstractStoredArticle(CompoundTag tag) {
		readTag(tag);
	}
	
	protected void setArticle(T article) {
		this.article = article;
	}
	
	public T article() {
		return article;
	}

	public void writeBuffer(PacketByteBuf buf) {
		buf.writeVarInt(ArticleProvider.REGISTRY.getRawId(provider));
		provider.toBuffer(article, buf);
	}

	public void writeTag(CompoundTag tag) {
		tag.putString("provider", ArticleProvider.REGISTRY.getId(provider).toString());
		provider.toTag(article, tag);
	}

	@SuppressWarnings("unchecked")
	public void readBuffer(PacketByteBuf buf) {
		provider = (ArticleProvider<T>) ArticleProvider.REGISTRY.get(buf.readVarInt());
		article = provider.fromBuffer(buf);
	}

	@SuppressWarnings("unchecked")
	public void readTag(CompoundTag tag) {
		provider = (ArticleProvider<T>) ArticleProvider.REGISTRY.get(new Identifier(tag.getString("provider")));
		article = provider.fromTag(tag);
	}
	
	@Override
	public ArticleProvider<T> provider() {
		return null;
	}
}
