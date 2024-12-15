package io.github.mortuusars.exposure.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.*;

public class ItemAndStack<T extends Item> {
    private final T item;
    private final ItemStack stack;

    public ItemAndStack(ItemStack stack) {
        this.stack = stack;
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

    //TODO: rework or delete

    public static <T extends Item> void executeIfItemMatches(Class<T> c, Item item, Consumer<T> ifMatches) {
        if (c.isInstance(item)) {
            //noinspection unchecked
            ifMatches.accept(((T)item));
        }
    }

    public static <T extends Item> void executeIfItemMatches(Class<T> c, Item item, Consumer<T> ifMatches, Runnable ifNot) {
        if (c.isInstance(item)) {
            //noinspection unchecked
            ifMatches.accept(((T)item));
        }
        else {
            ifNot.run();
        }
    }

    public static <T extends Item> void executeIfItemMatches(Class<T> c, ItemStack stack, Consumer<T> ifMatches) {
        if (c.isInstance(stack.getItem())) {
            //noinspection unchecked
            ifMatches.accept(((T)stack.getItem()));
        }
    }

    public static <T extends Item> void executeIfItemMatches(Class<T> c, ItemStack stack, Consumer<T> ifMatches, Runnable ifNot) {
        if (c.isInstance(stack.getItem())) {
            //noinspection unchecked
            ifMatches.accept(((T)stack.getItem()));
        }
        else {
            ifNot.run();
        }
    }

    public static <T extends Item, R> R mapIfItemMatches(Class<T> c, ItemStack stack, Function<T, R> ifMatches, Supplier<R> ifNot) {
        if (c.isInstance(stack.getItem())) {
            //noinspection unchecked
            return ifMatches.apply(((T)stack.getItem()));
        }
        else {
            return ifNot.get();
        }
    }
}
