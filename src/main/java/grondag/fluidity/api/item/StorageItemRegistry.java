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
public interface StorageItemRegistry {
	StorageItemRegistry INSTANCE = StorageItemRegistryImpl.INSTANCE;

	<V extends StorageItem> V get(Identifier id);

	<V extends StorageItem> V get(String idString);

	<V extends StorageItem> V get(int index);

	<V extends StorageItem> void forEach(Consumer<V> consumer);

	boolean contains(Identifier id);

	<V extends StorageItem> V add(Identifier id, V item);

	default <V extends StorageItem> V add(String idString, V item) {
		return add(new Identifier(idString), item);
	}

	<V extends StorageItem> Identifier getId(V bulkItem);

	<V extends StorageItem> int getRawId(V bulkItem);
}
