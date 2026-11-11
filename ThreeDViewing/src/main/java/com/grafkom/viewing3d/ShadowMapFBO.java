package com.grafkom.viewing3d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

public class ShadowMapFBO {

    private int fboId;
    private int depthTextureId;

    private final int width;
    private final int height;

    public ShadowMapFBO(int width, int height) {
        this.width = width;
        this.height = height;

        buatFramebuffer();
    }

    private void buatFramebuffer() {

        fboId = GL30.glGenFramebuffers();
        depthTextureId = GL11.glGenTextures();

        GL11.glBindTexture(
            GL11.GL_TEXTURE_2D,
            depthTextureId
        );

        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL30.GL_DEPTH_COMPONENT24,
            width,
            height,
            0,
            GL11.GL_DEPTH_COMPONENT,
            GL11.GL_FLOAT,
            (java.nio.ByteBuffer) null
        );

        GL11.glTexParameteri(
            GL11.GL_TEXTURE_2D,
            GL11.GL_TEXTURE_MIN_FILTER,
            GL11.GL_NEAREST
        );

        GL11.glTexParameteri(
            GL11.GL_TEXTURE_2D,
            GL11.GL_TEXTURE_MAG_FILTER,
            GL11.GL_NEAREST
        );

        GL11.glTexParameteri(
            GL11.GL_TEXTURE_2D,
            GL11.GL_TEXTURE_WRAP_S,
            GL12.GL_CLAMP_TO_EDGE
        );

        GL11.glTexParameteri(
            GL11.GL_TEXTURE_2D,
            GL11.GL_TEXTURE_WRAP_T,
            GL12.GL_CLAMP_TO_EDGE
        );

        GL30.glBindFramebuffer(
            GL30.GL_FRAMEBUFFER,
            fboId
        );

        GL30.glFramebufferTexture2D(
            GL30.GL_FRAMEBUFFER,
            GL30.GL_DEPTH_ATTACHMENT,
            GL11.GL_TEXTURE_2D,
            depthTextureId,
            0
        );

        GL11.glDrawBuffer(GL11.GL_NONE);
        GL11.glReadBuffer(GL11.GL_NONE);

        int status = GL30.glCheckFramebufferStatus(
            GL30.GL_FRAMEBUFFER
        );

        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException(
                "Shadow Framebuffer tidak lengkap: " + status
            );
        }

        GL30.glBindFramebuffer(
            GL30.GL_FRAMEBUFFER,
            0
        );

        GL11.glBindTexture(
            GL11.GL_TEXTURE_2D,
            0
        );
    }

    public void bindForWriting() {

        GL30.glBindFramebuffer(
            GL30.GL_FRAMEBUFFER,
            fboId
        );

        GL11.glViewport(
            0, 0, width, height
        );

        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void bindForReading(int textureUnit) {

        GL13.glActiveTexture(
            GL13.GL_TEXTURE0 + textureUnit
        );

        GL11.glBindTexture(
            GL11.GL_TEXTURE_2D,
            depthTextureId
        );
    }

    public int getDepthTextureId() {
        return depthTextureId;
    }

    public void unbind() {
        GL30.glBindFramebuffer(
            GL30.GL_FRAMEBUFFER,
            0
        );
    }

    public void hapus() {
        GL30.glDeleteFramebuffers(fboId);
        GL11.glDeleteTextures(depthTextureId);
    }
}