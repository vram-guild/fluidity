/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.fluidity.api.fluid.transact;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.IdentityHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import net.minecraft.server.MinecraftServer;

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
        // TODO: consider custom exceptions or at least revist these
        if(!isOpen) {
            throw new IllegalStateException("Encountered transaction operation for closed transaction.");
        }
        if(STACK.get(stackPointer) != this) {
            throw new IndexOutOfBoundsException("Transaction operations must apply to most recent open transaction.");
        }
        if(!innerLock.isHeldByCurrentThread()) {
            throw new ConcurrentModificationException("Attempt to modify transaction status from foreign thread");
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
        
        final boolean root = --stackPointer == -1;
        
        final Thread myThread = Thread.currentThread();
        innerLock.unlock();
        if(myThread != serverThread) {
            outerLock.unlock();
            if(root) {
                Thread.yield();
            }
        }
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
    public static void setThread(MinecraftServer server) {
        serverThread = Thread.currentThread();
    }
    
    private static Thread serverThread;
    
    static final ReentrantLock innerLock = new ReentrantLock();
    static final ReentrantLock outerLock = new ReentrantLock();
    
    private static final ArrayList<FluidTx> STACK = new ArrayList<>();
    private static int stackPointer = -1;
    
    public static FluidTx open() {
        final Thread myThread = Thread.currentThread();
        if(myThread != serverThread) {
            outerLock.lock();
        }
        innerLock.lock();
        
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
