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
 ******************************************************************************/package grondag.fluidity.base.storage;

 import com.google.common.util.concurrent.Runnables;
import grondag.fluidity.api.device.ItemComponentContext;
import grondag.fluidity.api.storage.Store;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

 /**
  * Forwarding store that causes changes to be serialized to an item stack.
  * For Item-based storage.  Wrapped store must extend {@code AbstractStore}
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
		 stackSetter = s -> {};
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
