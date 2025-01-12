package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.camera.CameraID;
import io.github.mortuusars.exposure.core.camera.PhotographerEntity;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.InterplanarProjectionFinishedC2SP;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;

public class InterplanarProjectionAction implements CaptureAction {
    private final PhotographerEntity photographer;
    private final CameraID cameraID;

    public InterplanarProjectionAction(PhotographerEntity photographer, CameraID cameraID) {
        this.photographer = photographer;
        this.cameraID = cameraID;
    }

    @Override
    public void onSuccess() {
//        Entity entity = photographer.asEntity();
//        photographer.playCameraSoundSided(Exposure.SoundEvents.INTERPLANAR_PROJECT.get(), 0.8f, 1.1f, 0.1f);
//        for (int i = 0; i < 32; ++i) {
//            entity.level().addParticle(ParticleTypes.PORTAL, entity.getX(),
//                    entity.getY() + entity.getRandom().nextDouble() * 2.0, entity.getZ(),
//                    entity.getRandom().nextGaussian(), 0.0, entity.getRandom().nextGaussian());
//        }
        Packets.sendToServer(new InterplanarProjectionFinishedC2SP(photographer.asEntity().getUUID(), cameraID, true));
    }

    @Override
    public void onFailure() {
//        Entity entity = photographer.asEntity();
//        photographer.playCameraSoundSided(Exposure.SoundEvents.INTERPLANAR_PROJECT.get(), 0.8f, 0.6f, 0.1f);
//        for (int i = 0; i < 32; ++i) {
//            entity.level().addParticle(ParticleTypes.PORTAL, entity.getX(),
//                    entity.getY() + entity.getRandom().nextDouble() * 2.0, entity.getZ(),
//                    entity.getRandom().nextGaussian(), 0.0, entity.getRandom().nextGaussian());
//        }
        Packets.sendToServer(new InterplanarProjectionFinishedC2SP(photographer.asEntity().getUUID(), cameraID, false));
    }
}
