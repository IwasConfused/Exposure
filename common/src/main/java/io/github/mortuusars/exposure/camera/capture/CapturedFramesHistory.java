package io.github.mortuusars.exposure.camera.capture;

import com.google.common.collect.ImmutableList;
import io.github.mortuusars.exposure.item.component.ExposureFrame;

import java.util.ArrayList;
import java.util.List;

public class CapturedFramesHistory {
    
    private static final ArrayList<ExposureFrame> lastExposures = new ArrayList<>();
    private static int limit = 32;

    public static List<ExposureFrame> get() {
        return ImmutableList.copyOf(lastExposures);
    }

    public static void add(ExposureFrame frame) {
        lastExposures.addFirst(frame);

        while (lastExposures.size() > limit) {
            lastExposures.remove(limit);
        }
    }

    public static int getLimit() {
        return limit;
    }

    public static void setLimit(int limit) {
        CapturedFramesHistory.limit = limit;
    }

    public static void clear() {
        lastExposures.clear();
    }
}
