package net.fabricmc.fabric.impl.fluids;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.fabricmc.fabric.api.fluids.v1.container.FluidPort;
import net.fabricmc.fabric.api.fluids.v1.container.PortFilter;
import net.minecraft.util.math.Direction;

public class PortFilterImpl implements PortFilter {

    protected boolean includeFill;
    protected boolean includeDrain;
    protected Set<Direction> sides;
    
    protected PortFilterImpl(PortFilterImpl builder) {
        if(builder != null) {
            includeFill = builder.includeFill;
            includeDrain = builder.includeDrain;
            sides = builder.sides;
        }
    }
    
    @Override
    public boolean test(FluidPort port) {
        return (sides.isEmpty() || sides.contains(port.side()))
                && (includeFill || !port.canFill())
                && (includeDrain || !port.canDrain());
    }
    
    public static Builder builder() {
        return new BuilderImpl();
    }
    
    private static List<Direction> ALL_SIDES = ImmutableList.copyOf(Direction.values());
    
    protected static class BuilderImpl extends PortFilterImpl implements Builder {

        protected BuilderImpl() {
            super(null);
            clear();
        }
        
        protected void clear() {
            includeFill = true;
            includeDrain = true;
            sides = new HashSet<Direction>();
        }
        
        @Override
        public Builder includeSide(Direction side) {
            sides.add(side);
            return this;
        }
        
        @Override
        public Builder excludeSide(Direction side) {
            if(sides.isEmpty() || (side != null && sides.size() == 1 && sides.contains(null))) {
                sides.addAll(ALL_SIDES);
            }
            sides.remove(side);
            return this;
        }

        @Override
        public Builder excludeFill() {
            includeFill = false;
            return this;
        }

        @Override
        public Builder excludeDrain() {
            includeDrain = false;
            return this;
        }

        @Override
        public PortFilter build() {
            this.sides = ImmutableSet.copyOf(sides);
            final PortFilter result = new PortFilterImpl(this);
            clear();
            return result;
        }
    }

    @Override
    public Set<Direction> sides() {
        return sides;
    }

    @Override
    public boolean includeFill() {
        return includeFill;
    }

    @Override
    public boolean includeDrain() {
        return includeDrain;
    }

}
