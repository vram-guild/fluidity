package grondag.fluidity.api.bulk;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;

public class ContainerItem extends Item {
	private ContainerItem(Settings settings, Fluid fluid) {
		super(settings);
		fluid = null;
	}
	
	public ContainerItem(Settings settings) {
		super(settings);
	}
	
	public boolean isBulk() {
		return false;
	}
}
