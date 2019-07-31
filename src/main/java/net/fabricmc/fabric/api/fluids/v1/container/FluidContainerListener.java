package net.fabricmc.fabric.api.fluids.v1.container;

@FunctionalInterface
public interface FluidContainerListener {
    public static enum Operation {
        ADD,
        REMOVE,
        UPDATE,
        DISCONNECT
    }
    
    void accept(ContainerFluidVolume fluidVolume, Operation op);
    
    @FunctionalInterface
    public static interface StopNotifier {
        void stopListening();
    }
}
