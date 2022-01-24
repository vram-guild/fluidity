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

package grondag.fluidity.base.storage;

import com.google.common.util.concurrent.Runnables;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import grondag.fluidity.api.device.ItemComponentContext;
import grondag.fluidity.api.storage.Store;

/**
 * Forwarding store that causes changes to be serialized to an item stack.
 * For Item-based storage.  Wrapped store must extend {@code AbstractStore}.
 */
public abstract class AbstractPortableStore extends ForwardingStore {
	protected final java.util.function.Supplier<ItemStack> stackGetter;
	protected final java.util.function.Consumer<ItemStack> stackSetter;
	protected Runnable dirtyNotifier = Runnables.doNothing();

	/**
	 * Use this version for client-side display instance - will never save anything.
	 * Call {@link #readFromStack(ItemStack)} each time before using.
	 */
	public AbstractPortableStore(Store wrapped) {
		stackGetter = () -> ItemStack.EMPTY;
		stackSetter = s -> { };
		setWrapped(wrapped);
	}

	public AbstractPortableStore(Store wrapped, ItemComponentContext ctx) {
		this(wrapped, ctx.stackGetter(), ctx.stackSetter());
	}

	public AbstractPortableStore(Store wrapped, java.util.function.Supplier<ItemStack> stackGetter, java.util.function.Consumer<ItemStack> stackSetter) {
		this.stackGetter = stackGetter;
		this.stackSetter = stackSetter;
		dirtyNotifier = () -> saveToStack();
		setWrapped(wrapped);

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
		readTag(readTagFromStack(stack));
	}

	protected void saveToStack() {
		final ItemStack stack = stackGetter.get();
		writeTagToStack(stack, writeTag());
		stackSetter.accept(stack);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setWrapped(Store wrapped) {
		super.setWrapped(wrapped);
		((AbstractStore) wrapped).onDirty(dirtyNotifier);
	}
}
