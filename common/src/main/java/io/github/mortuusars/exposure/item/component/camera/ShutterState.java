package io.github.mortuusars.exposure.item.component.camera;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShutterState(boolean isOpen, long openedAtTick, int openDurationTicks) {
    public static final ShutterState EMPTY = new ShutterState(false, 0L, 1);

    public static final Codec<ShutterState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("is_open", false).forGetter(ShutterState::isOpen),
                    Codec.LONG.optionalFieldOf("opened_at", 0L).forGetter(ShutterState::openedAtTick),
                    Codec.INT.optionalFieldOf("open_duration", 1).forGetter(ShutterState::openDurationTicks))
            .apply(instance, ShutterState::new));

    public static final StreamCodec<ByteBuf, ShutterState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ShutterState::isOpen,
            ByteBufCodecs.VAR_LONG, ShutterState::openedAtTick,
            ByteBufCodecs.VAR_INT, ShutterState::openDurationTicks,
            ShutterState::new
    );

    public static ShutterState open(long openedAt, int duration) {
        return new ShutterState(true, openedAt, duration);
    }

    public static ShutterState closed() {
        return EMPTY;
    }

    public long getCloseTick() {
        return openedAtTick + openDurationTicks;
    }

    public static class Mutable {
        private boolean isOpen;
        private long openedAtTick;
        private int openDurationTicks;

        public boolean isOpen() {
            return isOpen;
        }

        public Mutable setOpen(boolean open) {
            isOpen = open;
            return this;
        }

        public long getOpenedAtTick() {
            return openedAtTick;
        }

        public Mutable setOpenedAtTick(long openedAtTick) {
            this.openedAtTick = openedAtTick;
            return this;
        }

        public int getOpenDurationTicks() {
            return openDurationTicks;
        }

        public Mutable setOpenDurationTicks(int openDurationTicks) {
            this.openDurationTicks = openDurationTicks;
            return this;
        }

        public ShutterState toImmutable() {
            return new ShutterState(isOpen, openedAtTick, openDurationTicks);
        }
    }
}
