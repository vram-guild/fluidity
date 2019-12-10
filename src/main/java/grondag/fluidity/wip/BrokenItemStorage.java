package grondag.fluidity.wip;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.RecipeInputProvider;

import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.item.base.BulkItem;
import grondag.fluidity.api.item.base.ItemStackView;
import grondag.fluidity.api.item.base.StackHelper;
import grondag.fluidity.api.storage.ItemStorage;
import grondag.fluidity.api.storage.view.ArticleView;
import grondag.fluidity.api.transact.TransactionContext;

public interface BrokenItemStorage extends ItemStorage, Inventory, RecipeInputProvider  {


	@Override
	default boolean isEmpty() {
		final int size = slotCount();

		for (int i = 0; i < size; i++) {
			if (!getInvStack(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	default boolean hasDynamicSlots() {
		return false;
	}

	@Override
	default FractionView accept(BulkItem item, FractionView volume, boolean simulate) {
		return Fraction.ZERO;
	}

	@Override
	default FractionView supply(BulkItem item, FractionView volume, boolean simulate) {
		return Fraction.ZERO;
	}

	@Override
	default long accept(Item item, CompoundTag tag, long count, boolean simulate) {
		if (item == null || item == Items.AIR || count == 0) {
			return 0;
		}

		final int size = slotCount();

		long result = 0;
		ItemStackView view = null;
		boolean isDirty = false;

		for (int i = 0; i < size; i++) {
			final ItemStack candidate = getInvStack(i);

			if (candidate.isEmpty()) {
				final int n = (int) Math.min(count - result, item.getMaxCount());

				if (!simulate) {
					final ItemStack newStack = StackHelper.newStack(item, tag, n);
					setInvStack(i, newStack);
					isDirty = true;
					view = notifyListeners(view, newStack, i);
				}
				result += n;

			} else if (candidate.getItem() == item) {

				final int capacity = candidate.getMaxCount() - candidate.getCount();
				if (capacity > 0) {
					final int n = (int) Math.min(count - result, capacity);

					if (!simulate) {
						candidate.increment(n);
						view = notifyListeners(view, candidate, i);
					}
					result += n;
				}

			}
			if (result == count) {
				break;
			}
		}

		if (isDirty) {
			notifyInvListeners();
		}

		return result;
	}

	@Override
	default long supply(Item item, CompoundTag tag, long count, boolean simulate) {
		if (item == null || item == Items.AIR || count == 0) {
			return 0;
		}

		final int size = slotCount();
		long result = 0;
		ItemStackView view = null;
		boolean isDirty = false;

		for (int i = 0; i < size; i++) {
			final ItemStack candidate = getStack(i);
			if (StackHelper.areItemsEqual(item, tag, candidate)) {
				final int n = (int) Math.min(count - result, candidate.getCount());

				if (!simulate) {
					isDirty = true;
					candidate.decrement(n);

					if (candidate.isEmpty()) {
						setStack(i, ItemStack.EMPTY);
					}

					view = notifyListeners(view, getStack(i), i);
				}
				result += n;

				if (result == count) {
					break;
				}
			}
		}

		if (isDirty) {
			notifyInvListeners();
		}

		return result;
	}

	@Override
	default void forEach(T connection, Predicate<ArticleView> filter, Predicate<ArticleView> consumer) {
		final ItemStackView view = new ItemStackView();
		final int size = slotCount();
		for (int i = 0; i < size; i++) {
			final ItemStack stack = getStack(i);

			if (!stack.isEmpty()) {
				view.prepare(stack, i);

				if (filter.test(view)) {
					if (!consumer.test(view)) {
						break;
					}
				}
			}
		}
	}

	@Override
	default ArticleView view(int slot) {
		return new ItemStackView(getStack(slot), slot);
	}

	@Override
	default Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		final int size = slotCount();
		final ItemStack[] state = new ItemStack[size];

		for (int i = 0; i < size; i++) {
			state[i] = getStack(i).copy();
		}

		context.setState(state);
		return rollackHandler;
	}

	private Consumer<TransactionContext> rollackHandler = this::handleRollback;

	private void handleRollback(TransactionContext context) {
		if (!context.isCommited()) {
			final int size = this.slotCount();
			final ItemStack[] state = context.getState();
			ItemStackView view = null;
			boolean isDirty = false;

			for (int i = 0; i < size; i++) {
				ItemStack myStack = getStack(i);
				ItemStack stateStack = state[i];
				if (!myStack.isItemEqual(stateStack)) {
					setStack(i, stateStack);
					isDirty = true;
					view = notifyListeners(view, stateStack, i);
				}
			}

			if (isDirty) {
				notifyInvListeners();
			}
		}
	}

	@Override void clear() {

	}

	@Override int getInvSize() {
		return slotCount();
	}

	@Override boolean isInvEmpty() {
		return this.isEmpty();
	}

	@Override ItemStack getInvStack(int slot) {
		return getStack(slot);
	}

	@Override ItemStack takeInvStack(int slot, int count) {
		if (slot < 0 || slot >= slotCount()) {
			return ItemStack.EMPTY;
		}

		final ItemStack stack = getStack(slot);

		final int n = Math.min(count, stack.getCount());

		if (n == 0) {
			return ItemStack.EMPTY;
		}

		final ItemStack result = stack.copy();
		result.setCount(n);

		stack.decrement(n);

		if (stack.isEmpty()) {
			setStack(slot, ItemStack.EMPTY);
		}

		notifyListeners(null, getStack(slot), slot);
		notifyInvListeners();
		return result;
	}

	@Override ItemStack removeInvStack(int slot) {
		if (slot < 0 || slot >= slotCount()) {
			return ItemStack.EMPTY;
		}

		final ItemStack stack = getStack(slot);

		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		setStack(slot, ItemStack.EMPTY);
		notifyListeners(null, ItemStack.EMPTY, slot);
		return stack;
	}

	@Override void setInvStack(int slot, ItemStack stackIn) {
		if (slot < 0 || slot >= slotCount())
			return;

		if (!stackIn.isEmpty() && stackIn.getCount() > this.getInvMaxStackAmount()) {
			stackIn.setCount(this.getInvMaxStackAmount());
		}

		final ItemStack stack = getStack(slot);
		if (stack.isItemEqual(stackIn) && stack.getCount() == stackIn.getCount())
			return;

		setStack(slot, stackIn);

		notifyListeners(null, stackIn, slot);
		notifyInvListeners();
	}

	@Override void markDirty() {
		notifyInvListeners();

		if (listeners != null && !listeners.isEmpty()) {
			final ItemStackView view = new ItemStackView();
			final int size = slotCount();
			for (int i = 0; i < size; i++) {
				notifyListeners(view.prepare(getStack(i), i));
			}
		}
	}

	protected void notifyInvListeners() {
		if (invListeners != null && !invListeners.isEmpty()) {
			final int limit = invListeners.size();
			for (int i = 0; i < limit; i++) {
				invListeners.get(i).onInvChange(this);
			}
		}
	}
}
