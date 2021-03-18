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
package grondag.fluidity.base.storage.bulk;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.device.ItemComponentContext;
import grondag.fluidity.api.fraction.Fraction;

/**
 * A version  of {@code SimpleTank} that persists to an ItemStack.
 * Use for Item-based storage devices.
 *
 */
@Experimental
public abstract class AbstractPortableTank extends SimpleTank {
	protected final java.util.function.Supplier<ItemStack> stackGetter;
	protected final java.util.function.Consumer<ItemStack> stackSetter;

	/**
	 * Use this version for client-side display instance - will never save anything.
	 * Call {@link #readFromStack(ItemStack)} each time before using.
	 */
	public AbstractPortableTank() {
		super(Fraction.ONE);
		stackGetter = () -> ItemStack.EMPTY;
		stackSetter = s -> {};
		dirtyNotifier = () -> {};
	}

	public AbstractPortableTank(Fraction defaultCapacity, ItemComponentContext ctx) {
		this(defaultCapacity, ctx.stackGetter(), ctx.stackSetter());
	}

	public AbstractPortableTank(Fraction defaultCapacity, java.util.function.Supplier<ItemStack> stackGetter, java.util.function.Consumer<ItemStack> stackSetter) {
		super(defaultCapacity);
		this.stackGetter = stackGetter;
		this.stackSetter = stackSetter;
		dirtyNotifier = () -> saveToStack();

		final CompoundTag tag = readTagFromStack(stackGetter.get());

		if(tag != null && !tag.isEmpty()) {
			readTag(tag);
		}
	}

	/**
	 * Override if tag isn't at root (for block entities, for example)
	 */
	protected abstract CompoundTag readTagFromStack(ItemStack stack);

	protected abstract void  writeTagToStack(ItemStack stack, CompoundTag tag);

	/**
	 * For client-side display, create and retain a static reference and call this each frame.
	 */
	public void readFromStack(ItemStack stack) {
		super.readTag(readTagFromStack(stack));
	}

	protected void saveToStack() {
		final ItemStack stack = stackGetter.get();
		writeTagToStack(stack, writeTag());
		stackSetter.accept(stack);
	}
}
