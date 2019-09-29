package grondag.fluidity.api.bulk;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeType;

public interface RecipeBulkInputProvider {
	   void provideRecipeBulkInputs(Consumer<ItemStack> consumer, RecipeType<?>... types);
}
