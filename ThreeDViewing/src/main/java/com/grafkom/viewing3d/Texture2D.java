
package com.grafkom.viewing3d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Texture2D {

    private final int textureId;

    public Texture2D(String resourcePath, boolean gunakanBilinear) {

        textureId = GL11.glGenTextures();

        byte[] fileBytes;

        try (InputStream input =
                getClass().getResourceAsStream(resourcePath)) {

            if (input == null) {
                throw new RuntimeException(
                    "Tekstur tidak ditemukan: " + resourcePath
                );
            }

            fileBytes = input.readAllBytes();

        } catch (IOException e) {
            throw new RuntimeException(
                "Gagal membaca tekstur: " + resourcePath, e
            );
        }

        ByteBuffer encoded = MemoryUtil.memAlloc(fileBytes.length);
        encoded.put(fileBytes).flip();

        try (MemoryStack stack = MemoryStack.stackPush()) {

            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(true);

            ByteBuffer image = STBImage.stbi_load_from_memory(
                encoded,
                width,
                height,
                channels,
                4
            );

            if (image == null) {
                throw new RuntimeException(
                    "Gagal memuat gambar: "
                    + STBImage.stbi_failure_reason()
                );
            }

            try {
                GL11.glBindTexture(
                    GL11.GL_TEXTURE_2D, textureId
                );

                GL11.glTexImage2D(
                    GL11.GL_TEXTURE_2D,
                    0,
                    GL11.GL_RGBA,
                    width.get(0),
                    height.get(0),
                    0,
                    GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE,
                    image
                );

                GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

                int minFilter = gunakanBilinear
                    ? GL11.GL_LINEAR_MIPMAP_LINEAR
                    : GL11.GL_NEAREST_MIPMAP_NEAREST;

                int magFilter = gunakanBilinear
                    ? GL11.GL_LINEAR
                    : GL11.GL_NEAREST;

                GL11.glTexParameteri(
                    GL11.GL_TEXTURE_2D,
                    GL11.GL_TEXTURE_MIN_FILTER,
                    minFilter
                );

                GL11.glTexParameteri(
                    GL11.GL_TEXTURE_2D,
                    GL11.GL_TEXTURE_MAG_FILTER,
                    magFilter
                );

                GL11.glTexParameteri(
                    GL11.GL_TEXTURE_2D,
                    GL11.GL_TEXTURE_WRAP_S,
                    GL11.GL_REPEAT
                );

                GL11.glTexParameteri(
                    GL11.GL_TEXTURE_2D,
                    GL11.GL_TEXTURE_WRAP_T,
                    GL11.GL_REPEAT
                );

            } finally {
                STBImage.stbi_image_free(image);
            }

        } finally {
            MemoryUtil.memFree(encoded);
        }
    }

    public void gunakan() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
    }

    public int getTextureId() {
        return textureId;
    }

    public void hapus() {
        GL11.glDeleteTextures(textureId);
    }
}
