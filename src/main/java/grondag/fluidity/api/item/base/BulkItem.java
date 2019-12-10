package grondag.fluidity.api.item.base;

import javax.annotation.Nullable;

import net.minecraft.fluid.Fluid;

/**
 * Represents a game resource that may be a fluid, or may be some other
 * thing that is quantified as fractional amounts instead of discrete fixed units.<p>
 *
 * Typically would be implemented on (@code Item}.
 */
@FunctionalInterface
public interface BulkItem {
	@Nullable
	Fluid toFluid();

	default boolean isFluid() {
		return toFluid() != null;
	}
}
