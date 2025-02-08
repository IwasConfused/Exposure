package io.github.mortuusars.exposure.world.item.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.*;

public class ItemAndStack<T extends Item> {
    private final T item;
    private final ItemStack stack;

    public ItemAndStack(ItemStack stack) {
        this.stack = stack;
        //noinspection unchecked
        this.item = (T) stack.getItem();
    }

    public T getItem() {
        return item;
    }

    public ItemStack getItemStack() {
        return stack;
    }

    public ItemAndStack<T> apply(BiConsumer<T, ItemStack> function) {
        function.accept(item, stack);
        return this;
    }

    public <R> R map(BiFunction<T, ItemStack, R> mappingFunction) {
        return mappingFunction.apply(item, stack);
    }

    @Override
    public String toString() {
        return "ItemAndStack{" + stack.toString() + '}';
    }
}
