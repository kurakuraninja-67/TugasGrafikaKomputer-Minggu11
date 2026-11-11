
package com.grafkom.viewing3d;

import org.lwjgl.opengl.GL11;

public class HSRManager {

    private boolean depthTestAktif = true;
    private boolean faceCullingAktif = false;

    public void aturDepthTest(boolean aktif) {
        depthTestAktif = aktif;

        if (depthTestAktif) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LESS);
        } else {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
    }

    public void aturFaceCulling(boolean aktif) {
        faceCullingAktif = aktif;

        if (faceCullingAktif) {
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(GL11.GL_BACK);
            GL11.glFrontFace(GL11.GL_CCW);
        } else {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
    }

    public void bersihkanBuffer() {
        GL11.glClear(
            GL11.GL_COLOR_BUFFER_BIT |
            GL11.GL_DEPTH_BUFFER_BIT
        );
    }

    public boolean isDepthTestAktif() {
        return depthTestAktif;
    }

    public boolean isFaceCullingAktif() {
        return faceCullingAktif;
    }
}
