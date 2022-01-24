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

package grondag.fluidity.base.storage.discrete;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.device.ItemComponentContext;

/**
 * A version  of {@code SingleStackInventoryStore} that persists to an ItemStack.
 * Use for Item-based storage devices.
 *
 */
@Experimental
public class PortableSingleArticleStore extends SingleArticleStore {
	protected String keyName;
	protected java.util.function.Supplier<ItemStack> stackGetter;
	protected java.util.function.Consumer<ItemStack> stackSetter;

	public PortableSingleArticleStore(long defaultCapacity, String keyName, ItemComponentContext ctx) {
		super(defaultCapacity);
		stackGetter = ctx.stackGetter();
		stackSetter = ctx.stackSetter();
		this.keyName = keyName;
		dirtyNotifier = () -> saveToStack();

		final ItemStack stack = stackGetter.get();

		if (stack.hasTag() && stack.getTag().contains(keyName)) {
			readTag((CompoundTag) stack.getTag().get(keyName));
		}
	}

	protected void saveToStack() {
		final ItemStack stack = stackGetter.get();
		stack.getOrCreateTag().put(keyName, writeTag());
		stackSetter.accept(stack);
	}

	/**
	 * Use for client-side tank render/display.  Uses stack directly and doesn't instantiate a new Store each call.
	 *
	 * @param stack Stack containing serialized tank data
	 * @param keyName NBT tag name for store data
	 * @return amount in the tank, or zero if no tank data found
	 */
	public static long getAmount(ItemStack stack, String keyName) {
		final CompoundTag tag = stack.getTagElement(keyName);
		return tag == null ? 0 : tag.getLong("quantity");
	}

	/**
	 * Use for client-side tank render/display.  Uses stack directly and doesn't instantiate a new Store each call.
	 *
	 * @param stack Stack containing serialized tank data
	 * @param keyName NBT tag name for store data
	 * @return amount in the tank, or zero if no tank data found
	 */
	public static long getCapacity(ItemStack stack, String keyName) {
		final CompoundTag tag = stack.getTagElement(keyName);
		return tag == null ? 0 : tag.getLong("capacity");
	}

	/**
	 * Use for client-side tank render/display.  Uses stack directly and doesn't instantiate a new Store each call.
	 *
	 * @param stack Stack containing serialized tank data
	 * @param keyName NBT tag name for store data
	 * @return article in the tank, or {@code Article.NOTHING} if tank is empty or no tank data found
	 */
	public static Article getArticle(ItemStack stack, String keyName) {
		final CompoundTag tag = stack.getTagElement(keyName);

		if (tag == null) {
			return Article.NOTHING;
		}

		final long amount = tag == null ? 0 : tag.getLong("quantity");

		return amount == 0 ? Article.NOTHING : Article.fromTag(tag.get("art"));
	}
}
