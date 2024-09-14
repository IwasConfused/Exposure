package io.github.mortuusars.exposure.integration.kubejs.neoforge;

import io.github.mortuusars.exposure.neoforge.api.event.FrameAddedEvent;
import io.github.mortuusars.exposure.neoforge.api.event.ModifyFrameDataEvent;
import io.github.mortuusars.exposure.neoforge.api.event.ShutterOpeningEvent;
import io.github.mortuusars.exposure.integration.kubejs.ExposureJSPlugin;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;

public class ExposureJSPluginImpl {
    public static void subscribeToEvents() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, ExposureJSPluginImpl::fireShutterOpeningEvent);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, ExposureJSPluginImpl::fireModifyFrameDataEvent);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, ExposureJSPluginImpl::fireFrameAddedEvent);
    }

    public static void fireShutterOpeningEvent(ShutterOpeningEvent event) {
        if (ExposureJSPlugin.fireShutterOpeningEvent(event.player, event.cameraStack, event.lightLevel, event.shouldFlashFire))
            event.setCanceled(true);
    }

    public static void fireModifyFrameDataEvent(ModifyFrameDataEvent event) {
        ExposureJSPlugin.fireModifyFrameDataEvent(event.player, event.cameraStack, event.frame, event.entitiesInFrame);
    }

    public static void fireFrameAddedEvent(FrameAddedEvent event) {
        ExposureJSPlugin.fireFrameAddedEvent(event.player, event.cameraStack, event.frame);
    }
}
