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

import grondag.fluidity.api.item.BulkItem;
import grondag.fluidity.api.item.BulkItemRegistry;

@API(status = INTERNAL)
public class BulkItemRegistryImpl implements BulkItemRegistry {
	public static BulkItemRegistryImpl INSTANCE = new BulkItemRegistryImpl();

	private static final MutableRegistry<BulkItem> REGISTRY;

	static {
		REGISTRY = Registry.REGISTRIES.add(new Identifier("c:bulk_items"),
				(MutableRegistry<BulkItem>) new DefaultedRegistry<BulkItem>("c:nothing"));

		REGISTRY.add(new Identifier("c:nothing"), BulkItem.NOTHING);
	}

	@Override
	public Identifier getId(BulkItem bulkItem) {
		return REGISTRY.getId(bulkItem);
	}

	@Override
	public int getRawId(BulkItem bulkItem) {
		return REGISTRY.getRawId(bulkItem);
	}

	@Override
	public BulkItem get(Identifier id) {
		return REGISTRY.get(id);
	}

	@Override
	public BulkItem get(String idString) {
		return REGISTRY.get(new Identifier(idString));
	}

	@Override
	public BulkItem get(int index) {
		return REGISTRY.get(index);
	}

	@Override
	public void forEach(Consumer<BulkItem> consumer) {
		REGISTRY.forEach(consumer);
	}

	@Override
	public BulkItem add(Identifier id, BulkItem item) {
		return REGISTRY.add(id, item);
	}

	@Override
	public boolean contains(Identifier id) {
		return REGISTRY.getIds().contains(id);
	}
}
