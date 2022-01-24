/*
 * This file is part of Fluidity and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.fluidity.impl.article;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.serialization.Lifecycle;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.article.ArticleTypeRegistry;

@SuppressWarnings({ "unchecked", "rawtypes" })
@Internal
public final class ArticleTypeRegistryImpl implements ArticleTypeRegistry {
	private ArticleTypeRegistryImpl() { }

	public static void initialize() { }

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
