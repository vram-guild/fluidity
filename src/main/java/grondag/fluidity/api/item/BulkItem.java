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

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;

/**
 * Represents a game resource that may be a fluid, or may be some other
 * thing that is quantified as fractional amounts instead of discrete fixed units.<p>
 *
 * Typically would be implemented on (@code Item}.
 */
@FunctionalInterface
@API(status = Status.EXPERIMENTAL)
public interface BulkItem extends StorageItem {
	@Nullable
	Fluid toFluid();

	default boolean isFluid() {
		return toFluid() != null;
	}

	@Override
	default boolean isBulk() {
		return true;
	}

	@Override
	default boolean isItem() {
		return false;
	}

	@Override
	default void writeTag(CompoundTag tag, String tagName) {
		tag.putString(tagName, BulkItemRegistry.INSTANCE.getId(this).toString());
	}

	BulkItem NOTHING = () -> null;

	static BulkItem fromTagValue(String tagValue) {
		return BulkItemRegistry.INSTANCE.get(tagValue);
	}
}
