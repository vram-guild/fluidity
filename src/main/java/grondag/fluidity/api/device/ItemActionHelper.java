package grondag.fluidity.api.device;

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

public interface ItemActionHelper {

	/**
	 * Use for fluids that fill bottles as Potions vs having a distinct fluid bottle.
	 */
	static void addPotionFillAction(Fluid fluid, Potion potion) {
		addPotionFillAction(fluid, potion, 3);
	}

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
	 * Use for fluids that fill bottles as Potions vs having a distinct fluid bottle.
	 */
	static void addPotionDrainAction(Fluid fluid, Potion potion) {
		addPotionDrainAction(fluid, potion, 3);
	}

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

	static void addPotionActions(Fluid fluid, Potion potion) {
		addPotionActions(fluid, potion, 3);
	}

	static void addPotionActions(Fluid fluid, Potion potion, long denominator) {
		addPotionDrainAction(fluid, potion, denominator);
		addPotionFillAction(fluid, potion, denominator);
	}

	static void addItemFillAction(Fluid fluid, Item emptyItem, Item fullItem) {
		addItemFillAction(fluid, emptyItem, fullItem, Fraction.ONE);
	}

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

	static void addItemDrainAction(Fluid fluid, Item emptyItem, Item fullItem) {
		addItemDrainAction(fluid, emptyItem, fullItem, Fraction.ONE);
	}

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

	static void addItemActions(Fluid fluid, Item emptyItem, Item fullItem) {
		addItemActions(fluid, emptyItem, fullItem, Fraction.ONE);
	}

	static void addItemActions(Fluid fluid, Item emptyItem, Item fullItem, Fraction amount) {
		addItemDrainAction(fluid, emptyItem, fullItem, amount);
		addItemFillAction(fluid, emptyItem, fullItem, amount);
	}
}
