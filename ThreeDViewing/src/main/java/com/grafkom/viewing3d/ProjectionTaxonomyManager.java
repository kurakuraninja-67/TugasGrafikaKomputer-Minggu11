package com.grafkom.viewing3d;

import org.joml.Matrix4f;

public class ProjectionTaxonomyManager {

    public enum ProjectionType {
        PERSPECTIVE,
        ORTHOGRAPHIC,
        OBLIQUE,
        ISOMETRIC,
        CAVALIER,
        CABINET
    }

    private ProjectionType currentProjection =
            ProjectionType.PERSPECTIVE;

    public void setProjection(ProjectionType type) {
        currentProjection = type;
    }

    public ProjectionType getCurrentProjection() {
        return currentProjection;
    }

    public Matrix4f getProjectionMatrix(float aspectRatio) {

        Matrix4f projection = new Matrix4f();

        float width = 4.5f * aspectRatio;
        float height = 4.5f;

        if (currentProjection == ProjectionType.PERSPECTIVE) {
            projection.perspective(
                (float) Math.toRadians(60),
                aspectRatio,
                0.1f,
                100.0f
            );
            return projection;
        }

        projection.ortho(
            -width, width,
            -height, height,
            0.1f, 100.0f
        );

        if (currentProjection == ProjectionType.OBLIQUE
                || currentProjection == ProjectionType.CAVALIER
                || currentProjection == ProjectionType.CABINET) {

            float factor = 0.35f;
            float angle = 45.0f;

            if (currentProjection == ProjectionType.CAVALIER) {
                factor = 0.50f;
            }

            if (currentProjection == ProjectionType.CABINET) {
                factor = 0.25f;
            }

            float sx = factor *
                    (float) Math.cos(Math.toRadians(angle));

            float sy = factor *
                    (float) Math.sin(Math.toRadians(angle));

            Matrix4f shear = new Matrix4f();

            shear.m20(sx);
            shear.m21(sy);

            // Menjaga objek tetap berada di tengah layar.
            shear.m30(sx * 5.0f);
            shear.m31(sy * 5.0f);

            projection.mul(shear);
        }

        return projection;
    }
}