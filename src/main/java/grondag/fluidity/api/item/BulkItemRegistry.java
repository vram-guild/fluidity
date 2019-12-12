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

import grondag.fluidity.impl.BulkItemRegistryImpl;

@API(status = EXPERIMENTAL)
public interface BulkItemRegistry {
	BulkItemRegistry INSTANCE = BulkItemRegistryImpl.INSTANCE;

	BulkItem get(Identifier id);

	BulkItem get(String idString);

	BulkItem get(int index);

	void forEach(Consumer<BulkItem> consumer);

	boolean contains(Identifier id);

	BulkItem add(Identifier id, BulkItem item);

	default BulkItem add(String idString, BulkItem item) {
		return add(new Identifier(idString), item);
	}

	Identifier getId(BulkItem bulkItem);

	int getRawId(BulkItem bulkItem);
}
