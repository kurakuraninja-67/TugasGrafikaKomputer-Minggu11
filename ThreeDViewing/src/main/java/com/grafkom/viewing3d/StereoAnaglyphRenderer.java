package com.grafkom.viewing3d;

import org.lwjgl.opengl.GL11;

public class StereoAnaglyphRenderer {

    public void enableLeftEye() {
        GL11.glColorMask(true, false, false, true);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void enableRightEye() {
        GL11.glColorMask(false, true, true, true);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void disableAnaglyph() {
        GL11.glColorMask(true, true, true, true);
    }
}