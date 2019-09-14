package grondag.fluidity.api.bulk;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.registry.DefaultedRegistry;

public interface BulkResource {
	DefaultedRegistry<BulkResource> REGISTRY = null;
	
	BulkResource EMPTY = null;
	
	default boolean isFluid() {
		return toFluid() == Fluids.EMPTY;
	}
	
	default Fluid toFluid() {
		return Fluids.EMPTY;
	}
}
