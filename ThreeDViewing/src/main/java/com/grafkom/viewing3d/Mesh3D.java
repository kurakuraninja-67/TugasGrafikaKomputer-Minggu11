package com.grafkom.viewing3d;

import org.joml.Vector3f;

public class Mesh3D {

    private final Vector3f[] vertices = {
        new Vector3f(-1, -1, -1),
        new Vector3f( 1, -1, -1),
        new Vector3f( 1,  1, -1),
        new Vector3f(-1,  1, -1),
        new Vector3f(-1, -1,  1),
        new Vector3f( 1, -1,  1),
        new Vector3f( 1,  1,  1),
        new Vector3f(-1,  1,  1)
    };

    private final int[] indices = {
        0, 1, 2, 2, 3, 0,
        4, 5, 6, 6, 7, 4,
        0, 4, 7, 7, 3, 0,
        1, 5, 6, 6, 2, 1,
        3, 2, 6, 6, 7, 3,
        0, 1, 5, 5, 4, 0
    };

    public Vector3f[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }
}