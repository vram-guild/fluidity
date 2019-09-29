package grondag.fluidity.api.bulk;

import net.minecraft.fluid.Fluid;

public class BulkItem extends ContainerItem {
	private BulkItem(Settings settings, Fluid fluid) {
		super(settings);
		fluid = null;
	}
	
	public BulkItem(Settings settings) {
		super(settings);
		fluid = null;
	}
	
	@Override
	public final boolean isBulk() {
		return true;
	}
	
	private Fluid fluid;
	
	public Fluid fluid() {
		return fluid;
	}
	
	public boolean isFluid() {
		return fluid != null;
	}
}
