/*******************************************************************************
 * Copyright 2019, 2020 grondag
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
package grondag.fluidity.api.article;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import net.minecraft.util.Identifier;

import grondag.fluidity.impl.article.ArticleTypeRegistryImpl;

@API(status = EXPERIMENTAL)
public interface ArticleTypeRegistry {
	static ArticleTypeRegistry instance() {
		return ArticleTypeRegistryImpl.INSTANCE;
	}

	<T> ArticleType<T> get(Identifier id);

	<T> ArticleType<T> get(String idString);

	<T> ArticleType<T> get(int index);

	void forEach(Consumer<? super ArticleType<?>> consumer);

	boolean contains(Identifier id);

	<T> ArticleType<T> add(Identifier id, ArticleType<T> articleType);

	default <T> ArticleType<T> add(String idString, ArticleType<T> articleType) {
		return add(new Identifier(idString), articleType);
	}

	<T> Identifier getId(ArticleType<T> articleType);

	<T> int getRawId(ArticleType<T> articleType);
}
