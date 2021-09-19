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

import java.util.function.Consumer;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.ApiStatus.Internal;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.article.ArticleTypeRegistry;

@SuppressWarnings({ "unchecked", "rawtypes" })
@Internal
public final class ArticleTypeRegistryImpl implements ArticleTypeRegistry {
	private ArticleTypeRegistryImpl() {

	}

	public static void init() {}

	public static ArticleTypeRegistryImpl INSTANCE = new ArticleTypeRegistryImpl();

	private static final ResourceKey REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation("fluidity:article_types"));
	private static final WritableRegistry<ArticleTypeImpl> ARTICLE_REGISTRY;

	static {
		ARTICLE_REGISTRY = (WritableRegistry<ArticleTypeImpl>) ((WritableRegistry) Registry.REGISTRY).register(REGISTRY_KEY,
				new DefaultedRegistry("fluidity:nothing", REGISTRY_KEY, Lifecycle.stable()), Lifecycle.stable());
	}

	@Override
	public <T> ResourceLocation getId(ArticleType<T> article) {
		return ARTICLE_REGISTRY.getKey((ArticleTypeImpl) article);
	}

	@Override
	public int getRawId(ArticleType article) {
		return ARTICLE_REGISTRY.getId((ArticleTypeImpl) article);
	}

	@Override
	public ArticleTypeImpl get(ResourceLocation id) {
		return ARTICLE_REGISTRY.get(id);
	}

	@Override
	public ArticleTypeImpl get(String idString) {
		return ARTICLE_REGISTRY.get(new ResourceLocation(idString));
	}

	@Override
	public ArticleTypeImpl get(int index) {
		return ARTICLE_REGISTRY.byId(index);
	}

	@Override
	public void forEach(Consumer<? super ArticleType<?>> consumer) {
		ARTICLE_REGISTRY.forEach((Consumer<? super ArticleType>) consumer);
	}

	@Override
	public ArticleType add(ResourceLocation id, ArticleType articleType) {
		return ARTICLE_REGISTRY.register(ResourceKey.create(REGISTRY_KEY, id), (ArticleTypeImpl) articleType, Lifecycle.stable());
	}

	@Override
	public boolean contains(ResourceLocation id) {
		return ARTICLE_REGISTRY.keySet().contains(id);
	}
}
