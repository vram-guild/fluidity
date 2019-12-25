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

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleRegistry;

@SuppressWarnings({ "unchecked", "rawtypes" })
@API(status = INTERNAL)
public class ArticleRegistryImpl implements ArticleRegistry {
	public static ArticleRegistryImpl INSTANCE = new ArticleRegistryImpl();

	private static final MutableRegistry<Article> REGISTRY;

	static {
		REGISTRY = Registry.REGISTRIES.add(new Identifier("c:storage_articles"),
				(MutableRegistry<Article>) new DefaultedRegistry("c:nothing"));

		REGISTRY.add(new Identifier("c:nothing"), Article.NOTHING);
	}

	@Override
	public Identifier getId(Article article) {
		return REGISTRY.getId(article);
	}

	@Override
	public int getRawId(Article article) {
		return REGISTRY.getRawId(article);
	}

	@Override
	public Article get(Identifier id) {
		return REGISTRY.get(id);
	}

	@Override
	public Article get(String idString) {
		return REGISTRY.get(new Identifier(idString));
	}

	@Override
	public Article get(int index) {
		return REGISTRY.get(index);
	}

	@Override
	public void forEach(Consumer<Article> consumer) {
		REGISTRY.forEach(consumer);
	}

	@Override
	public Article add(Identifier id, Article article) {
		return REGISTRY.add(id, article);
	}

	@Override
	public boolean contains(Identifier id) {
		return REGISTRY.getIds().contains(id);
	}
}
