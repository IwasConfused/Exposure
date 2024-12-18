package io.github.mortuusars.exposure.client.snapshot.capture.action;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.InterplanarProjectionFinishedC2SP;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class InterplanarProjectionAction implements CaptureAction {
    private final Entity photographer;
    private final UUID cameraID;

    public InterplanarProjectionAction(Entity photographer, UUID cameraID) {
        this.photographer = photographer;
        this.cameraID = cameraID;
    }

    @Override
    public void onSuccess() {
        photographer.level().playSound(Minecraft.getInstance().player, photographer, Exposure.SoundEvents.INTERPLANAR_PROJECT.get(),
                SoundSource.PLAYERS, 0.8f, 1.1f);
        for (int i = 0; i < 32; ++i) {
            photographer.level().addParticle(ParticleTypes.PORTAL, photographer.getX(),
                    photographer.getY() + photographer.getRandom().nextDouble() * 2.0, photographer.getZ(),
                    photographer.getRandom().nextGaussian(), 0.0, photographer.getRandom().nextGaussian());
        }
        Packets.sendToServer(new InterplanarProjectionFinishedC2SP(photographer.getUUID(), cameraID, true));
    }

    @Override
    public void onFailure() {
        photographer.level().playSound(Minecraft.getInstance().player, photographer, Exposure.SoundEvents.INTERPLANAR_PROJECT.get(),
                SoundSource.PLAYERS, 0.8f, 0.6f);
        for (int i = 0; i < 32; ++i) {
            photographer.level().addParticle(ParticleTypes.PORTAL, photographer.getX(),
                    photographer.getY() + photographer.getRandom().nextDouble() * 2.0, photographer.getZ(),
                    photographer.getRandom().nextGaussian(), 0.0, photographer.getRandom().nextGaussian());
        }
        Packets.sendToServer(new InterplanarProjectionFinishedC2SP(photographer.getUUID(), cameraID, false));
    }
}
