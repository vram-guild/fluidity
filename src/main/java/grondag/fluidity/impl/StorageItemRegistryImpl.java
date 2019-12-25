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

import grondag.fluidity.api.item.Article;
import grondag.fluidity.api.item.ArticleRegistry;

@SuppressWarnings({ "unchecked", "rawtypes" })
@API(status = INTERNAL)
public class StorageItemRegistryImpl implements ArticleRegistry {
	public static StorageItemRegistryImpl INSTANCE = new StorageItemRegistryImpl();

	private static final MutableRegistry REGISTRY;

	static {
		REGISTRY = Registry.REGISTRIES.add(new Identifier("c:storage_items"),
				(MutableRegistry<? extends Article>) new DefaultedRegistry("c:nothing"));

		REGISTRY.add(new Identifier("c:nothing"), Article.NOTHING);
	}

	@Override
	public <V extends Article> Identifier getId(V bulkItem) {
		return REGISTRY.getId(bulkItem);
	}

	@Override
	public <V extends Article> int getRawId(V bulkItem) {
		return REGISTRY.getRawId(bulkItem);
	}

	@Override
	public <V extends Article> V get(Identifier id) {
		return (V) REGISTRY.get(id);
	}

	@Override
	public <V extends Article> V get(String idString) {
		return (V) REGISTRY.get(new Identifier(idString));
	}

	@Override
	public <V extends Article> V get(int index) {
		return (V) REGISTRY.get(index);
	}

	@Override
	public <V extends Article> void forEach(Consumer<V> consumer) {
		REGISTRY.forEach(consumer);
	}

	@Override
	public <V extends Article> V add(Identifier id, V item) {
		return (V) REGISTRY.add(id, item);
	}

	@Override
	public boolean contains(Identifier id) {
		return REGISTRY.getIds().contains(id);
	}
}
