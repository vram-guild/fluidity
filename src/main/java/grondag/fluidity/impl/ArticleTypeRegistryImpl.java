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
package grondag.fluidity.impl;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;

import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.article.ArticleTypeRegistry;

@SuppressWarnings({ "unchecked", "rawtypes" })
@API(status = INTERNAL)
public final class ArticleTypeRegistryImpl implements ArticleTypeRegistry {
	private ArticleTypeRegistryImpl() {

	}

	public static ArticleTypeRegistryImpl INSTANCE = new ArticleTypeRegistryImpl();

	private static final MutableRegistry<ArticleType> REGISTRY;

	static {
		REGISTRY = Registry.REGISTRIES.add(new Identifier("fluidity:article_types"),
				(MutableRegistry<ArticleType>) new DefaultedRegistry("c:nothing"));
	}

	@Override
	public <T> Identifier getId(ArticleType<T> article) {
		return REGISTRY.getId(article);
	}

	@Override
	public int getRawId(ArticleType article) {
		return REGISTRY.getRawId(article);
	}

	@Override
	public ArticleType get(Identifier id) {
		return REGISTRY.get(id);
	}

	@Override
	public ArticleType get(String idString) {
		return REGISTRY.get(new Identifier(idString));
	}

	@Override
	public ArticleType get(int index) {
		return REGISTRY.get(index);
	}

	@Override
	public void forEach(Consumer<? super ArticleType<?>> consumer) {
		REGISTRY.forEach((Consumer<? super ArticleType>) consumer);
	}

	@Override
	public ArticleType add(Identifier id, ArticleType articleType) {
		return REGISTRY.add(id, articleType);
	}

	@Override
	public boolean contains(Identifier id) {
		return REGISTRY.getIds().contains(id);
	}
}
