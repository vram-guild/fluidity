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
package grondag.fluidity.api.item;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import net.minecraft.util.Identifier;

import grondag.fluidity.impl.StorageItemRegistryImpl;

@API(status = EXPERIMENTAL)
public interface ArticleRegistry {
	ArticleRegistry INSTANCE = StorageItemRegistryImpl.INSTANCE;

	<V extends Article> V get(Identifier id);

	<V extends Article> V get(String idString);

	<V extends Article> V get(int index);

	<V extends Article> void forEach(Consumer<V> consumer);

	boolean contains(Identifier id);

	<V extends Article> V add(Identifier id, V item);

	default <V extends Article> V add(String idString, V item) {
		return add(new Identifier(idString), item);
	}

	<V extends Article> Identifier getId(V bulkItem);

	<V extends Article> int getRawId(V bulkItem);
}
