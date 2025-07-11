package com.shangguan.utils;

import java.awt.*;

public class ColorMapper {
    public static Color map(double value, double min, double max) {
        if (Double.isNaN(value)) return Color.BLACK;

        double ratio = (value - min) / (max - min);
        ratio = Math.max(0.0, Math.min(1.0, ratio)); // clamp to [0,1]

        if (ratio < 0.25) {
            return interpolate(Color.BLUE, Color.CYAN, ratio / 0.25);
        } else if (ratio < 0.5) {
            return interpolate(Color.CYAN, Color.GREEN, (ratio - 0.25) / 0.25);
        } else if (ratio < 0.75) {
            return interpolate(Color.GREEN, Color.YELLOW, (ratio - 0.5) / 0.25);
        } else {
            return interpolate(Color.YELLOW, Color.RED, (ratio - 0.75) / 0.25);
        }
    }

    private static Color interpolate(Color c1, Color c2, double ratio) {
        int r = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int g = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int b = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return new Color(r, g, b);
    }
}
