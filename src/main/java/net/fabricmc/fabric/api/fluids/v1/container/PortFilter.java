package net.fabricmc.fabric.api.fluids.v1.container;

import java.util.Set;
import java.util.function.Predicate;

import net.fabricmc.fabric.impl.fluids.PortFilterImpl;
import net.minecraft.util.math.Direction;

public interface PortFilter extends Predicate<FluidPort> {
    Set<Direction> sides();
    
    boolean includeFill();
    
    boolean includeDrain();
    
    static PortFilter ALL = builder().build();
    static PortFilter UP = builder().includeSide(Direction.UP).build();
    static PortFilter DOWN = builder().includeSide(Direction.DOWN).build();
    static PortFilter NORTH = builder().includeSide(Direction.NORTH).build();
    static PortFilter SOUTH = builder().includeSide(Direction.SOUTH).build();
    static PortFilter EAST = builder().includeSide(Direction.EAST).build();
    static PortFilter WEST = builder().includeSide(Direction.WEST).build();
    static PortFilter ALL_FILL = builder().excludeDrain().build();
    static PortFilter ALL_DRAIN = builder().excludeFill().build();
    
    static interface Builder {
        Builder includeSide(Direction side);
        
        Builder excludeSide(Direction side);

        Builder excludeFill();
        
        Builder excludeDrain();
        
        PortFilter build();
    }

    static Builder builder() {
        return PortFilterImpl.builder();
    }
}
