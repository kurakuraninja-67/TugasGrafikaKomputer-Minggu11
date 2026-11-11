
package com.grafkom.viewing3d;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryUtil;

public class MeshRenderer {

    private int vaoId;
    private int vboId;
    private int eboId;
    private int jumlahIndices;

    public MeshRenderer(MeshData mesh) {

        float[] vertices = mesh.getVertices();
        int[] indices = mesh.getIndices();

        jumlahIndices = indices.length;

        vaoId = GL30.glGenVertexArrays();
        vboId = GL15.glGenBuffers();
        eboId = GL15.glGenBuffers();

        GL30.glBindVertexArray(vaoId);

        FloatBuffer vertexBuffer =
            MemoryUtil.memAllocFloat(vertices.length);

        IntBuffer indexBuffer =
            MemoryUtil.memAllocInt(indices.length);

        try {
            vertexBuffer.put(vertices).flip();
            indexBuffer.put(indices).flip();

            GL15.glBindBuffer(
                GL15.GL_ARRAY_BUFFER, vboId
            );

            GL15.glBufferData(
                GL15.GL_ARRAY_BUFFER,
                vertexBuffer,
                GL15.GL_STATIC_DRAW
            );

            GL15.glBindBuffer(
                GL15.GL_ELEMENT_ARRAY_BUFFER, eboId
            );

            GL15.glBufferData(
                GL15.GL_ELEMENT_ARRAY_BUFFER,
                indexBuffer,
                GL15.GL_STATIC_DRAW
            );

        } finally {
            MemoryUtil.memFree(vertexBuffer);
            MemoryUtil.memFree(indexBuffer);
        }

        int stride = 8 * Float.BYTES;

        // Posisi: 3 float
        GL20.glVertexAttribPointer(
            0, 3, GL20.GL_FLOAT,
            false, stride, 0L
        );
        GL20.glEnableVertexAttribArray(0);

        // Normal: 3 float
        GL20.glVertexAttribPointer(
            1, 3, GL20.GL_FLOAT,
            false, stride, 3L * Float.BYTES
        );
        GL20.glEnableVertexAttribArray(1);

        // UV: 2 float
        GL20.glVertexAttribPointer(
            2, 2, GL20.GL_FLOAT,
            false, stride, 6L * Float.BYTES
        );
        GL20.glEnableVertexAttribArray(2);

        GL30.glBindVertexArray(0);
    }

    public void render() {

        GL30.glBindVertexArray(vaoId);

        GL15.glBindBuffer(
            GL15.GL_ELEMENT_ARRAY_BUFFER, eboId
        );

        org.lwjgl.opengl.GL11.glDrawElements(
    org.lwjgl.opengl.GL11.GL_TRIANGLES,
    jumlahIndices,
    org.lwjgl.opengl.GL11.GL_UNSIGNED_INT,
    0L
);

        GL30.glBindVertexArray(0);
    }

    public void hapus() {

        GL30.glDeleteVertexArrays(vaoId);
        GL15.glDeleteBuffers(vboId);
        GL15.glDeleteBuffers(eboId);
    }
}