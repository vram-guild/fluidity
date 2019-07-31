package grondag.fluidity.transact;

import java.util.Arrays;
import java.util.function.Supplier;

import org.apache.commons.lang3.ObjectUtils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.fluids.v1.container.FluidContainer;
import net.fabricmc.fabric.api.fluids.v1.container.FluidPort;
import net.fabricmc.fabric.api.fluids.v1.transact.FluidTx;
import net.fabricmc.fabric.api.fluids.v1.volume.MutableFluidVolume;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryListener;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.util.math.MathHelper;

/**
 * Draft of interface patterns for tanks and item inventories.
 * Uses inventories for brevity. (Fluids will require more new classes.)
 */
public class StorageSketch {

    @SuppressWarnings("unused")
    public static void runTests() {
        final SimpleInventory inv1 = new SimpleInventory(1);
        final SimpleInventory inv3 = new SimpleInventory(3);
        final SimpleInventory inv9 = new SimpleInventory(9);
        final SimpleInventory inv27 = new SimpleInventory(27);
        ItemStack lapis = new ItemStack(Items.LAPIS_LAZULI, 32);
        ItemStack cactus = new ItemStack(Items.CACTUS, 32);
        ItemStack cobble = new ItemStack(Items.COBBLESTONE, 32);
        
        storeAllOrNone(inv3, lapis, cactus, cobble);
        
        assert inv3.countInInv(Items.LAPIS_LAZULI) == 32;
        assert inv3.countInInv(Items.CACTUS) == 32;
        assert inv3.countInInv(Items.COBBLESTONE) == 32;
        assert lapis.isEmpty();
        assert cactus.isEmpty();
        assert cobble.isEmpty();
    }
    
    static boolean fetchIngredients(SimpleInventory inv, ItemStack... stacks) { 
        try (InventoryTx tx = InventoryTx.open()) {
            for (ItemStack stack : stacks) {
                if (!inv.takeExactly(stack)) {
                    tx.rollback();
                    return false;
                }
            }
            tx.commit();
            return true;
        }
    }
    
    static void storeAllOrNone(SimpleInventory target, ItemStack... stacks) {
        try (InventoryTx tx = InventoryTx.open()) {
            for(ItemStack stack : stacks) {
                InventoryTx.enlist(stack);
                if (!target.add(stack).isEmpty()) {
                   tx.rollback();
                   return;
                }
            }
            tx.commit();
        }
    }
    
    static void storeAllOrNone(MutableFluidVolume fluid, FluidContainer... targets) {
        try (FluidTx tx = FluidTx.open()) {
            tx.enlist(fluid);
            for(FluidContainer target : targets) {
                tx.enlist(target).input(fluid).applyAndSubtract(fluid, FluidPort.NORMAL);
                if(fluid.volume().isZero()) {
                    tx.commit();
                    return;
                }
            }
            tx.rollback();
        }
    }
    
    public static final int MASK_32 = (1 << 31) - 1;
    
    public static final int BLEND_MODE_COUNT = 5;
    public static final int BLEND_MODE_MASK = (1 << MathHelper.smallestEncompassingPowerOfTwo(BLEND_MODE_COUNT)) - 1;
    
    public static interface Transactor {
        void commit();
        void rollback();
    }

    static class InventoryTx implements AutoCloseable {
        private InventoryTx() {};

        @Override
        public void close() {
            if (isOpen) handleRollback();
        }

        public void rollback() {
            handleRollback();
        }

        public void commit() {
            handleCommit();
        }

        private static final InventoryTx instance = new InventoryTx();
        private static boolean isOpen = false;
        private static final ObjectOpenHashSet<Transactor> participants = new ObjectOpenHashSet<>();

        public static InventoryTx open() {
            if(isOpen) {
                throw new IllegalStateException("Request to start inventory transaction when already started.");
            } else {
                isOpen = true;
                return instance;
            }
        }

        public static Supplier<ItemStack> enlist(ItemStack stack) {
            ItemStackTx result = new ItemStackTx(stack);
            enlist(result);
            return result;
        }
        
        public static boolean enlistIfOpen(Transactor participant) {
            if(isOpen) {
                enlist(participant);
                return true;
            } else {
                return false;
            }
        }

        static void enlist(Transactor participant) {
            if (isOpen) {
                participants.add(participant);
            } else {
                throw new IllegalStateException("Inventory transaction request when transaction not started.");
            }
        }

        static void handleRollback() {
            if (isOpen) {
                participants.forEach(t -> t.rollback());
                isOpen = false;
            } else {
                throw new IllegalStateException("Inventory transaction rollback request when transaction not started.");
            }
        }

        static void handleCommit() {
            if (isOpen) {
                participants.forEach(t -> t.commit());
                isOpen = false;
            } else {
                throw new IllegalStateException("Inventory transaction rollback request when transaction not started.");
            }
        }
    }

    private static class ItemStackTx implements Supplier<ItemStack>, Transactor {
        private ItemStack stack;
        private ItemStack copy;
       
        private ItemStackTx(ItemStack stackIn) {
            stack = stackIn;
            copy = stackIn;
        }
        
        @Override
        public ItemStack get() {
            return stack;
        }

        @Override
        public void commit() {
            copy = null;
        }

        @Override
        public void rollback() {
            stack.setCooldown(copy.getCooldown());
            stack.setCount(copy.getCount());
            stack.setTag(copy.getTag());
        }
    }
    
    /** Transactional variant of BasicInventory */
    public static class SimpleInventory implements Inventory, RecipeInputProvider, Transactor {
        protected final int size;
        protected final ItemStack[] stacks; // second half contains copy during transaction
        protected ObjectArrayList<InventoryListener> listeners;
        protected boolean hasRollback = false;
        protected boolean needsNotify = false;
        
        public SimpleInventory(int size) {
            this.size = size;
            stacks = new ItemStack[size * 2];
        }

        public boolean takeExactly(ItemStack stackIn) {
            prepareRollback();
            int remaining = stackIn.getCount();
            for(int i = 0; i < size; i++) {
                ItemStack s = stacks[i];
                if(stackIn.isItemEqual(s) && ItemStack.areTagsEqual(stackIn, s)) {
                    //TODO
                }
            }
            return remaining == 0;
        }

        public SimpleInventory(ItemStack... stacksIn) {
            this(stacksIn.length);
            System.arraycopy(stacksIn, 0, stacks, 0, size);
        }

        public void addListener(InventoryListener listener) {
            if (listeners == null) {
                listeners = new ObjectArrayList<>();
            }
            listeners.add(listener);
        }

        public void removeListener(InventoryListener listener) {
            listeners.remove(listener);
        }

        @Override
        public ItemStack getInvStack(int slot) {
            prepareRollback();
            return slot >= 0 && slot < size ? ObjectUtils.defaultIfNull(stacks[slot], ItemStack.EMPTY) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack takeInvStack(int slot, int count) {
            prepareRollback();
            ItemStack result = getInvStack(slot);
            if(!result.isEmpty()) {
                result = result.split(count);
                markDirty();
            }
            return result;
        }

        public ItemStack add(ItemStack stackIn) {
            prepareRollback();
            ItemStack stack = stackIn.copy();

            for(int i = 0; i < size; ++i) {
                ItemStack existing = this.getInvStack(i);
                if (existing.isEmpty()) {
                    this.setInvStack(i, stack);
                    this.markDirty();
                    return ItemStack.EMPTY;
                }

                if (ItemStack.areItemsEqual(existing, stack)) {
                    int capacity = Math.min(this.getInvMaxStackAmount(), existing.getMaxCount());
                    int count = Math.min(stack.getCount(), capacity - existing.getCount());
                    if (count > 0) {
                        existing.increment(count);
                        stack.decrement(count);
                        if (stack.isEmpty()) {
                            this.markDirty();
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (stack.getCount() != stackIn.getCount()) {
                this.markDirty();
            }

            return stack;
        }

        @Override
        public ItemStack removeInvStack(int slot) {
            prepareRollback();
            if (slot >= 0 && slot < size) {
                ItemStack result = ObjectUtils.defaultIfNull(stacks[slot], ItemStack.EMPTY);
                stacks[slot] = ItemStack.EMPTY;
                markDirty();
                return result;
            } else {
                return ItemStack.EMPTY;
            }
        }

        @Override
        public void setInvStack(int slot, ItemStack stack) {
            prepareRollback();
            stack = stack.copy();
            if (!stack.isEmpty() && stack.getCount() > this.getInvMaxStackAmount()) {
                stack.setCount(this.getInvMaxStackAmount());
            }
            stacks[slot] = stack;
            markDirty();
        }

        @Override
        public int getInvSize() {
            return size;
        }

        @Override
        public boolean isInvEmpty() {
            for(int i = 0; i < size; i++) {
                if (!(stacks[i] == null || stacks[i].isEmpty())) return false;
            }
            return true;
        }

        @Override
        public void markDirty() {
            if(hasRollback) {
                needsNotify = true;
            } else {
                notifyListeners();
            }
        }

        protected void notifyListeners() {
            if (this.listeners != null) {
                listeners.forEach(l -> l.onInvChange(this));
            }
        }
        
        @Override
        public boolean canPlayerUseInv(PlayerEntity player) {
            return true;
        }

        @Override
        public void clear() {
            prepareRollback();
            Arrays.fill(stacks, 0, size, ItemStack.EMPTY);
            markDirty();
        }

        @Override
        public void provideRecipeInputs(RecipeFinder finder) {
            for(int i = 0; i < size; i++) {
                final ItemStack stack = stacks[i];
                if(stack != null && !stack.isEmpty()) finder.addItem(stack);
            }
        }

        protected void prepareRollback() {
            if(!hasRollback && InventoryTx.enlistIfOpen(this)) {
                for(int i = 0; i < size; i++) {
                    ItemStack stack = stacks[i];
                    stacks[i + size] = stack == null ? null : stack.copy();
                }
                hasRollback = true;
            }
        }
        
        @Override
        public void commit() {
            hasRollback = false;
            if(needsNotify) {
                notifyListeners();
                needsNotify = false;
            }
        }

        @Override
        public void rollback() {
            if(hasRollback) {
                System.arraycopy(stacks, size, stacks, 0, size);
                hasRollback = false;
            }
            needsNotify = false;
        }
    }
}
