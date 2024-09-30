package io.github.mortuusars.exposure.item.component.camera;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.core.camera.ShutterSpeed;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShutterState(boolean isOpen, long openedAtTick, ShutterSpeed shutterSpeed) {
    public static final ShutterState CLOSED = new ShutterState(false, 0L, ShutterSpeed.DEFAULT);

    public static final Codec<ShutterState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("is_open", false).forGetter(ShutterState::isOpen),
                    Codec.LONG.optionalFieldOf("opened_at", 0L).forGetter(ShutterState::openedAtTick),
                    ShutterSpeed.CODEC.optionalFieldOf("shutter_speed", ShutterSpeed.DEFAULT).forGetter(ShutterState::shutterSpeed))
            .apply(instance, ShutterState::new));

    public static final StreamCodec<ByteBuf, ShutterState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ShutterState::isOpen,
            ByteBufCodecs.VAR_LONG, ShutterState::openedAtTick,
            ShutterSpeed.STREAM_CODEC, ShutterState::shutterSpeed,
            ShutterState::new
    );

    public static ShutterState open(long openedAt, ShutterSpeed shutterSpeed) {
        return new ShutterState(true, openedAt, shutterSpeed);
    }

    public static ShutterState closed() {
        return CLOSED;
    }

    public long getCloseTick() {
        return isOpen ? openedAtTick + shutterSpeed.getDurationTicks() : -1;
    }

//    public static class Mutable {
//        private boolean isOpen;
//        private long openedAtTick;
//        private int openDurationTicks;
//
//        public boolean isOpen() {
//            return isOpen;
//        }
//
//        public Mutable setOpen(boolean open) {
//            isOpen = open;
//            return this;
//        }
//
//        public long getOpenedAtTick() {
//            return openedAtTick;
//        }
//
//        public Mutable setOpenedAtTick(long openedAtTick) {
//            this.openedAtTick = openedAtTick;
//            return this;
//        }
//
//        public int getOpenDurationTicks() {
//            return openDurationTicks;
//        }
//
//        public Mutable setOpenDurationTicks(int openDurationTicks) {
//            this.openDurationTicks = openDurationTicks;
//            return this;
//        }
//
//        public ShutterState toImmutable() {
//            return new ShutterState(isOpen, openedAtTick, openDurationTicks);
//        }
//    }
}
