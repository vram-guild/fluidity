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
package grondag.fluidity.api.article;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import net.minecraft.util.Identifier;

import grondag.fluidity.impl.ArticleRegistryImpl;

@API(status = EXPERIMENTAL)
public interface ArticleRegistry {
	ArticleRegistry INSTANCE = ArticleRegistryImpl.INSTANCE;

	Article get(Identifier id);

	Article get(String idString);

	Article get(int index);

	void forEach(Consumer<Article> consumer);

	boolean contains(Identifier id);

	Article add(Identifier id, Article article);

	default Article add(String idString, Article article) {
		return add(new Identifier(idString), article);
	}

	Identifier getId(Article article);

	int getRawId(Article article);
}
