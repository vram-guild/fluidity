package net.fabricmc.fabric.api.fluids.v1.transact;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.function.Consumer;

/**
 * Global transaction manager for fluid containers.
 * 
 * Use like so:
 * <blockquote><pre>void storeAllOrNone(MutableFluidVolume fluid, FluidContainer... targets) {
 *       try (FluidTx tx = FluidTx.open()) {
 *           tx.enlist(fluid);
 *           for(FluidContainer target : targets) {
 *               tx.enlist(target).input().applyAndSubtract(fluid, FluidPort.NORMAL);
 *               if(fluid.volume().isZero()) {
 *                   tx.commit();
 *                   return;
 *               }
 *           }
 *           tx.rollback();
 *       }
 *   }</pre></blockquote>
 *   
 *   TODO: Consider locking mechanism for use by non-main threads.  Main thread would
 *   have priority and hold the lock during all server-thread transactions. Containers
 *   that exploit this may still need to synchronize internally for single-call, 
 *   atomic operations that don't rely on the transaction manager.
 *   The commit stack would have to be empty when a lock is obtained.
 */
public final class FluidTx implements AutoCloseable {

    public final class Context {
        private Context() {}
        
        public <T> void  setState(T state) {
            stateStorage.put(contextContainer, state);
        }
        
        @SuppressWarnings("unchecked")
        public <T> T getState() {
            return (T) stateStorage.get(contextContainer);
        }
        
        public boolean isCommited() {
            return isCommited;
        }
    }
    
    private final Context context = new Context();
    private boolean isOpen = true;
    private boolean isCommited = false;
    private final IdentityHashMap<FluidTxActor, Consumer<FluidTx.Context>> participants = new IdentityHashMap<>();
    private final IdentityHashMap<FluidTxActor, Object> stateStorage = new IdentityHashMap<>();
    private FluidTxActor contextContainer;
    
    private FluidTx() {}
    
    @Override
    public void close() {
        if(isOpen) {
            rollback();
        }
    }
    
    private void clear() {
        participants.clear();
        stateStorage.clear();
        contextContainer = null;
        isOpen = false;
        isCommited = false;
    }
    
    private void validate() {
        if(!isOpen) {
            throw new IllegalStateException("Encountered transaction operation for closed transaction.");
        }
        if(STACK.get(stackPointer) != this) {
            throw new IndexOutOfBoundsException("Transaction operations must apply to most recent open transaction.");
        }
    }
    
    public void rollback() {
       close(false);
    }

    public void commit() {
        close(true);
    }
    
    private void close(boolean isCommited) {
        validate();
        this.isCommited = isCommited;
        participants.forEach((c, r) -> {
            contextContainer = c;
            r.accept(context);
        });
        clear();
    }
    
    public <T extends FluidTxActor> T enlist(T container) {
        validate();
        if (!participants.containsKey(container)) {
            contextContainer = container;
            participants.put(container, defaultRollback(container.prepareTx(context)));
            contextContainer = null;
        }
        return container;
    }
    
    private Consumer<FluidTx.Context> defaultRollback(Consumer<FluidTx.Context> consumer) {
        return consumer;
    }
    
    
    ///// STATIC MEMBERS FOLLOW /////
    
    
    private static final ArrayList<FluidTx> STACK = new ArrayList<>();
    private static int stackPointer = -1;
    
    public static FluidTx open() {
        final FluidTx result;
        if(STACK.size() > ++stackPointer) {
            result = STACK.get(stackPointer);
            result.isOpen = true;
        } else {
            assert STACK.size() == stackPointer;
            result = new FluidTx();
            STACK.add(result);
        }
        return result;
     }
}
