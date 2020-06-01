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
package grondag.fluidity.impl.article;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.function.Consumer;

import com.mojang.serialization.Lifecycle;
import org.apiguardian.api.API;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.article.ArticleTypeRegistry;

@SuppressWarnings({ "unchecked", "rawtypes" })
@API(status = INTERNAL)
public final class ArticleTypeRegistryImpl implements ArticleTypeRegistry {
	private ArticleTypeRegistryImpl() {

	}

	public static void init() {}

	public static ArticleTypeRegistryImpl INSTANCE = new ArticleTypeRegistryImpl();

	private static final RegistryKey REGISTRY_KEY = RegistryKey.ofRegistry(new Identifier("fluidity:article_types"));
	private static final MutableRegistry<ArticleTypeImpl> ARTICLE_REGISTRY;

	static {
		ARTICLE_REGISTRY = (MutableRegistry<ArticleTypeImpl>) ((MutableRegistry) Registry.REGISTRIES).add(REGISTRY_KEY,
				new DefaultedRegistry("fluidity:nothing", REGISTRY_KEY, Lifecycle.experimental()));
	}

	@Override
	public <T> Identifier getId(ArticleType<T> article) {
		return ARTICLE_REGISTRY.getId((ArticleTypeImpl) article);
	}

	@Override
	public int getRawId(ArticleType article) {
		return ARTICLE_REGISTRY.getRawId((ArticleTypeImpl) article);
	}

	@Override
	public ArticleTypeImpl get(Identifier id) {
		return ARTICLE_REGISTRY.get(id);
	}

	@Override
	public ArticleTypeImpl get(String idString) {
		return ARTICLE_REGISTRY.get(new Identifier(idString));
	}

	@Override
	public ArticleTypeImpl get(int index) {
		return ARTICLE_REGISTRY.get(index);
	}

	@Override
	public void forEach(Consumer<? super ArticleType<?>> consumer) {
		ARTICLE_REGISTRY.forEach((Consumer<? super ArticleType>) consumer);
	}

	@Override
	public ArticleType add(Identifier id, ArticleType articleType) {
		return ARTICLE_REGISTRY.add(RegistryKey.of(REGISTRY_KEY, id), (ArticleTypeImpl) articleType);
	}

	@Override
	public boolean contains(Identifier id) {
		return ARTICLE_REGISTRY.getIds().contains(id);
	}
}
