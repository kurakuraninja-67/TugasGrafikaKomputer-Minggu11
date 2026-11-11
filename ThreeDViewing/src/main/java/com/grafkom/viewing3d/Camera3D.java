
package com.grafkom.viewing3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera3D {

    private Vector3f eye;
    private Vector3f target;
    private Vector3f up;

    public Camera3D(
        Vector3f eye,
        Vector3f target,
        Vector3f up
    ) {
        this.eye = new Vector3f(eye);
        this.target = new Vector3f(target);
        this.up = new Vector3f(up);
    }

    // Menghasilkan matriks kamera
    public Matrix4f calculateLookAtMatrix() {

        return new Matrix4f().lookAt(
            eye,
            target,
            up
        );
    }

    // Mengatur posisi kamera berdasarkan orbit
    public void updateOrbitalPosition(
        float radius,
        float azimuthDeg,
        float elevationDeg
    ) {

        float azimuthRad =
            (float) Math.toRadians(azimuthDeg);

        float elevationRad =
            (float) Math.toRadians(elevationDeg);

        float x = radius
            * (float) Math.cos(elevationRad)
            * (float) Math.sin(azimuthRad);

        float y = radius
            * (float) Math.sin(elevationRad);

        float z = radius
            * (float) Math.cos(elevationRad)
            * (float) Math.cos(azimuthRad);

        eye.set(
            target.x + x,
            target.y + y,
            target.z + z
        );
    }

    // Mengambil posisi kamera
    public Vector3f getEye() {
        return new Vector3f(eye);
    }

    public Vector3f getTarget() {
        return new Vector3f(target);
    }
}
