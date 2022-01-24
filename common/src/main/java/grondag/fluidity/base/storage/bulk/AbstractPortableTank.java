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

package grondag.fluidity.base.storage.bulk;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

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
		stackSetter = s -> { };
		dirtyNotifier = () -> { };
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

		if (tag != null && !tag.isEmpty()) {
			readTag(tag);
		}
	}

	/**
	 * Override if tag isn't at root (for block entities, for example).
	 */
	protected abstract CompoundTag readTagFromStack(ItemStack stack);

	protected abstract void writeTagToStack(ItemStack stack, CompoundTag tag);

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
