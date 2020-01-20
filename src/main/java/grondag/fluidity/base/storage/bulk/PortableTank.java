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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.device.ItemComponentContext;
import grondag.fluidity.api.fraction.Fraction;

/**
 * A version  of {@code SimpleTank} that persists to an ItemStack.
 * Use for Item-based storage devices.
 *
 */
@API(status = Status.EXPERIMENTAL)
public class PortableTank extends SimpleTank {
	protected ItemStack stack;
	protected String keyName;
	protected java.util.function.Supplier<ItemStack> stackGetter;
	protected java.util.function.Consumer<ItemStack> stackSetter;

	public PortableTank(Fraction defaultCapacity, String keyName, ItemComponentContext ctx) {
		super(defaultCapacity);
		stackGetter = ctx.stackGetter();
		stackSetter = ctx.stackSetter();
		this.keyName = keyName;
		dirtyNotifier = () -> saveToStack();

		final ItemStack stack = stackGetter.get();

		if(stack.hasTag() &&  stack.getTag().contains(keyName)) {
			readTag((CompoundTag) stack.getTag().get(keyName));
		}
	}

	protected void saveToStack() {
		final ItemStack stack = stackGetter.get();
		stack.getOrCreateTag().put(keyName, this.writeTag());
		stackSetter.accept(stack);
	}

	/**
	 * Use for client-side tank render/display.  Uses stack directly and doesn't instantiate a new Store each call.
	 *
	 * @param stack Stack containing serialized tank data
	 * @param keyName NBT tag name for tank data
	 * @return amount in the tank, or zero if no tank data found
	 */
	public static Fraction getAmount(ItemStack stack, String keyName) {
		final CompoundTag tag = stack.getSubTag(keyName);
		return tag == null ? Fraction.ZERO : new Fraction(tag.getCompound("content"));
	}

	/**
	 * Use for client-side tank render/display.  Uses stack directly and doesn't instantiate a new Store each call.
	 *
	 * @param stack Stack containing serialized tank data
	 * @param keyName NBT tag name for tank data
	 * @return amount in the tank, or zero if no tank data found
	 */
	public static Fraction getCapacity(ItemStack stack, String keyName) {
		final CompoundTag tag = stack.getSubTag(keyName);
		return tag == null ? Fraction.ZERO : new Fraction(tag.getCompound("capacity"));
		//	article = Article.fromTag(tag.get("art"));
	}

	/**
	 * Use for client-side tank render/display.  Uses stack directly and doesn't instantiate a new Store each call.
	 *
	 * @param stack Stack containing serialized tank data
	 * @param keyName NBT tag name for tank data
	 * @return article in the tank, or {@code Article.NOTHING} if tank is empty or no tank data found
	 */
	public static Article getArticle(ItemStack stack, String keyName) {
		final CompoundTag tag = stack.getSubTag(keyName);

		if(tag == null) {
			return Article.NOTHING;
		}

		final Fraction amount =  tag == null ? Fraction.ZERO : new Fraction(tag.getCompound("content"));

		return amount.isZero() ? Article.NOTHING : Article.fromTag(tag.get("art"));
	}
}
