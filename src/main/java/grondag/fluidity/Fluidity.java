package grondag.fluidity;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.fluids.v1.container.MutableFluidVolume;
import net.fabricmc.fabric.api.fluids.v1.volume.SubstanceVolume;
import net.minecraft.fluid.Fluids;

public class Fluidity implements ModInitializer {
	@Override
	public void onInitialize() {
	    MutableFluidVolume f = SubstanceVolume.create(Fluids.WATER, 10, 10,1);
	    
	    assert f.wholeUnits() == 10;
	    assert f.numerator() == 0;
	    
	    f.drain(4, 3);
	    
	    System.out.println(f.volumeForDisplay(1));
	    
	    f.drain(7175, 1000);
	    
	    System.out.println(f.volumeForDisplay(1));
	    
	    f.fill(4, 3);
	    
	    System.out.println(f.volumeForDisplay(1));
	    
	    f.fill(7175, 1000);
	    
	    System.out.println(f.volumeForDisplay(1));
	    
	    f.drain(10, 1);
	    
	    assert f.isEmpty();
	}
}
