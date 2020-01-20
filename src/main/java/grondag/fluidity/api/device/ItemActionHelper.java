package grondag.fluidity.api.device;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.Store;
import grondag.fluidity.api.transact.Transaction;

/**
 * Helper methods for registering common item actions.
 * Avoids repetitive code that would be needed if using
 * {@link DeviceComponentType#registerAction(java.util.function.BiPredicate, Item...)} directly.
 *
 * @see <a href="https://github.com/grondag/fluidity#item-actionss">https://github.com/grondag/fluidity#item-actions</a>
 */
@API(status = Status.EXPERIMENTAL)
public interface ItemActionHelper {
	/**
	 * Adds an action to fill bottles from a component of type {@link Store#STORAGE_COMPONENT}.
	 * Use this method instead of {@link #addItemFillAction(Fluid, Item, Item)} for fluids that
	 * fill bottles as potions instead of having a distinct fluid bottle.<p>
	 *
	 * The version assumes each bottle holds 1/3 of a block of fluid.
	 *
	 * @param fluid the {@code Fluid} instance for which this action should apply if present in the {@code Store}
	 * @param potion the {@code Potion} type the resulting bottle will have
	 */
	static void addPotionFillAction(Fluid fluid, Potion potion) {
		addPotionFillAction(fluid, potion, 3);
	}

	/**
	 * Adds an action to fill bottles from a component of type {@link Store#STORAGE_COMPONENT}.
	 * Use this method instead of {@link #addItemFillAction(Fluid, Item, Item, Fraction)} for fluids that
	 * fill bottles as potions instead of having a distinct fluid bottle.<p>
	 *
	 * @param fluid the {@code Fluid} instance for which this action should apply if present in the {@code Store}
	 * @param potion the {@code Potion} type the resulting bottle will have
	 * @param denominator what fraction of a block of fluid is needed to fill a bottle completely (usually 3)
	 */
	static void addPotionFillAction(Fluid fluid, Potion potion, long denominator) {
		Store.STORAGE_COMPONENT.registerAction((ctx, store) -> {
			if(store.hasSupplier() && ctx.player() != null) {
				try(Transaction tx = Transaction.open()) {
					tx.enlist(store);

					if(store.getSupplier().apply(Article.of(fluid), 1, denominator, false) == 1) {
						final ItemStack stack = ctx.stackGetter().get();

						if(stack.getCount() == 1) {
							ctx.stackSetter().accept(PotionUtil.setPotion(new ItemStack(Items.POTION), potion));
						} else {
							ctx.player().inventory.offerOrDrop(ctx.world(), PotionUtil.setPotion(new ItemStack(Items.POTION), potion));
							stack.decrement(1);
							ctx.stackSetter().accept(stack.isEmpty() ? ItemStack.EMPTY : stack);
						}

						tx.commit();
						return true;
					}
				}
			}

			return false;
		}, Items.GLASS_BOTTLE);
	}

	/**
	 * Adds an action to drain full bottles into a component of type {@link Store#STORAGE_COMPONENT}.
	 * Use this method instead of {@link #addItemDrainAction(Fluid, Item, Item)} for fluids that
	 * fill bottles as potions instead of having a distinct fluid bottle.<p>
	 *
	 * The version assumes each bottle holds 1/3 of a block of fluid.
	 *
	 * @param fluid the {@code Fluid} instance that will be added to the {@code Store} when the action is applied
	 * @param potion the {@code Potion} type for which this action should apply
	 */
	static void addPotionDrainAction(Fluid fluid, Potion potion) {
		addPotionDrainAction(fluid, potion, 3);
	}

	/**
	 * Adds an action to drain full bottles into a component of type {@link Store#STORAGE_COMPONENT}.
	 * Use this method instead of {@link #addItemDrainAction(Fluid, Item, Item, Fraction)} for fluids that
	 * fill bottles as potions instead of having a distinct fluid bottle.<p>
	 *
	 * @param fluid the {@code Fluid} instance that will be added to the {@code Store} when the action is applied
	 * @param potion the {@code Potion} type for which this action should apply
	 * @param denominator what fraction of a block of fluid is needed to fill a bottle completely (usually 3)
	 */
	static void addPotionDrainAction(Fluid fluid, Potion potion, long denominator) {
		Store.STORAGE_COMPONENT.registerAction((ctx, store) -> {
			if(store.hasConsumer() && ctx.player() != null && PotionUtil.getPotion(ctx.stackGetter().get()) == potion) {
				try(Transaction tx = Transaction.open()) {
					tx.enlist(store);

					if(store.getConsumer().apply(Article.of(fluid), 1, denominator, false) == 1) {
						ctx.stackSetter().accept(new ItemStack(Items.GLASS_BOTTLE));
						tx.commit();
						return true;
					}
				}
			}

			return false;
		}, Items.POTION);
	}

	/**
	 * Equivalent to calling {@link #addPotionDrainAction(Fluid, Potion)} and
	 * {@link #addPotionFillAction(Fluid, Potion)} with the same parameters.
	 *
	 * @param fluid
	 * @param potion
	 */
	static void addPotionActions(Fluid fluid, Potion potion) {
		addPotionActions(fluid, potion, 3);
	}

	/**
	 * Equivalent  to calling {@link #addPotionDrainAction(Fluid, Potion, long)} and
	 * {@link #addPotionFillAction(Fluid, Potion, long)} with the same parameters.
	 *
	 * @param fluid
	 * @param potion
	 * @param denominator
	 */
	static void addPotionActions(Fluid fluid, Potion potion, long denominator) {
		addPotionDrainAction(fluid, potion, denominator);
		addPotionFillAction(fluid, potion, denominator);
	}

	/**
	 * Adds an action to fill empty items from a component of type {@link Store#STORAGE_COMPONENT}.
	 *
	 * The version assumes the full item holds one block of fluid.
	 *
	 * @param fluid the {@code Fluid} instance for which this action should apply if present in the {@code Store}
	 * @param emptyItem the empty item that will be filled when used on a store containing the fluid
	 * @param fullItem the full item that will replace the empty item if the action is successful
	 */
	static void addItemFillAction(Fluid fluid, Item emptyItem, Item fullItem) {
		addItemFillAction(fluid, emptyItem, fullItem, Fraction.ONE);
	}

	/**
	 * Adds an action to fill empty items from a component of type {@link Store#STORAGE_COMPONENT}.
	 *
	 * Use this version when the full item holds some amount other than one block of fluid.
	 *
	 * @param fluid the {@code Fluid} instance for which this action should apply if present in the {@code Store}
	 * @param emptyItem the empty item that will be filled when used on a store containing the fluid
	 * @param fullItem the full item that will replace the empty item if the action is successful
	 * @param amount the amount needed to fill the item - also the amount that will be drained from the {@code Store}
	 */
	static void addItemFillAction(Fluid fluid, Item emptyItem, Item fullItem, Fraction amount) {
		Store.STORAGE_COMPONENT.registerAction((ctx, store) -> {
			if(store.hasSupplier() && ctx.player() != null) {
				try(Transaction tx = Transaction.open()) {
					tx.enlist(store);

					if(store.getSupplier().apply(Article.of(fluid), amount, false).equals(amount)) {
						final ItemStack stack = ctx.stackGetter().get();

						if(stack.getCount() == 1) {
							ctx.stackSetter().accept(new ItemStack(fullItem));
						} else {
							ctx.player().inventory.offerOrDrop(ctx.world(), new ItemStack(fullItem));
							stack.decrement(1);
							ctx.stackSetter().accept(stack.isEmpty() ? ItemStack.EMPTY : stack);
						}

						ctx.player().inventory.markDirty();
						ctx.player().container.sendContentUpdates();
						ctx.player().method_14241();
						tx.commit();
						return true;
					}
				}
			}

			return false;
		}, emptyItem);
	}

	/**
	 * Adds an action to drain full items into a component of type {@link Store#STORAGE_COMPONENT}.
	 *
	 * The version assumes the full item holds one block of fluid.
	 *
	 * @param fluid the {@code Fluid} that will be added to the {@code Store} on which the full item was used
	 * @param emptyItem the item that will replace the filled item if the action is successful
	 * @param fullItem the full item for which this action should apply if present
	 */
	static void addItemDrainAction(Fluid fluid, Item emptyItem, Item fullItem) {
		addItemDrainAction(fluid, emptyItem, fullItem, Fraction.ONE);
	}

	/**
	 * Adds an action to drain full items into a component of type {@link Store#STORAGE_COMPONENT}.
	 *
	 * Use this version when the full item holds some amount other than one block of fluid.
	 *
	 * @param fluid the {@code Fluid} that will be added to the {@code Store} on which the full item was used
	 * @param emptyItem the item that will replace the filled item if the action is successful
	 * @param fullItem the full item for which this action should apply if present
	 * @param amount the amount of fluid present in the full item - also the amount that will be added to the {@code Store}
	 */
	static void addItemDrainAction(Fluid fluid, Item emptyItem, Item fullItem, Fraction amount) {
		Store.STORAGE_COMPONENT.registerAction((ctx, store) -> {
			if(store.hasConsumer() && ctx.player() != null && ctx.stackGetter().get().getItem() == fullItem) {
				try(Transaction tx = Transaction.open()) {
					tx.enlist(store);

					if(store.getConsumer().apply(Article.of(fluid), amount, false).equals(amount)) {
						final ItemStack stack = ctx.stackGetter().get();

						if(stack.getCount() == 1) {
							ctx.stackSetter().accept(new ItemStack(emptyItem));
						} else {
							ctx.player().inventory.offerOrDrop(ctx.world(), new ItemStack(emptyItem));
							stack.decrement(1);
							ctx.stackSetter().accept(stack.isEmpty() ? ItemStack.EMPTY : stack);
						}

						ctx.player().inventory.markDirty();
						ctx.player().container.sendContentUpdates();
						ctx.player().method_14241();
						tx.commit();
						return true;
					}
				}
			}

			return false;
		}, fullItem);
	}

	/**
	 * Equivalent to calling {@link #addItemDrainAction(Fluid, Item, Item)} and
	 * {@link #addItemFillAction(Fluid, Item, Item)} with the same parameters.
	 *
	 * @param fluid
	 * @param emptyItem
	 * @param fullItem
	 */
	static void addItemActions(Fluid fluid, Item emptyItem, Item fullItem) {
		addItemActions(fluid, emptyItem, fullItem, Fraction.ONE);
	}

	/**
	 * Equivalent to calling {@link #addItemDrainAction(Fluid, Item, Item, Fraction)} and
	 * {@link #addItemFillAction(Fluid, Item, Item, Fraction)} with the same parameters.
	 *
	 * @param fluid
	 * @param emptyItem
	 * @param fullItem
	 * @param amount
	 */
	static void addItemActions(Fluid fluid, Item emptyItem, Item fullItem, Fraction amount) {
		addItemDrainAction(fluid, emptyItem, fullItem, amount);
		addItemFillAction(fluid, emptyItem, fullItem, amount);
	}
}
