package io.github.mortuusars.exposure.core.camera;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.AttachmentSound;
import io.github.mortuusars.exposure.item.FilmRollItem;
import io.github.mortuusars.exposure.item.component.StoredItemStack;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public record AttachmentType(String id, DataComponentType<StoredItemStack> componentType,
                             Predicate<ItemStack> itemPredicate, AttachmentSound sound) {
    public static final AttachmentType FILM = new AttachmentType("film", Exposure.DataComponents.FILM,
            stack -> stack.getItem() instanceof FilmRollItem, AttachmentSound.FILM);
    public static final AttachmentType FLASH = new AttachmentType("flash", Exposure.DataComponents.FLASH,
            stack -> stack.is(Exposure.Tags.Items.FLASHES), AttachmentSound.FLASH);
    public static final AttachmentType LENS = new AttachmentType("lens", Exposure.DataComponents.LENS,
            stack -> stack.is(Exposure.Tags.Items.LENSES), AttachmentSound.LENS);
    public static final AttachmentType FILTER = new AttachmentType("filter", Exposure.DataComponents.FILTER,
            stack -> stack.is(Exposure.Tags.Items.FILTERS), AttachmentSound.FILTER);

    public boolean matches(ItemStack stack) {
        return itemPredicate.test(stack);
    }

    @Override
    public String toString() {
        return "AttachmentType{" +
                "exposureId='" + id + '\'' +
                '}';
    }
}
