
package com.grafkom.viewing3d;

import java.util.ArrayList;
import java.util.List;

public class MeshData {

    private final float[] vertices;
    private final int[] indices;


    // Constructor untuk bidang lantai penerima bayangan.
    public MeshData(float ukuranLantai) {
        float y = -1.15f;
        float s = ukuranLantai;
        this.vertices = new float[] {
            -s, y, -s,  0, 1, 0,  0, 0,
            -s, y,  s,  0, 1, 0,  0, 1,
             s, y,  s,  0, 1, 0,  1, 1,
             s, y, -s,  0, 1, 0,  1, 0
        };
        this.indices = new int[] { 0, 1, 2, 0, 2, 3 };
    }

    // Constructor kubus
    public MeshData() {

        List<Float> data = new ArrayList<>();
        List<Integer> faces = new ArrayList<>();

        tambahSisi(data, faces,
            new float[][]{
                {-1, -1, 1}, {1, -1, 1},
                {1, 1, 1}, {-1, 1, 1}
            },
            new float[]{0, 0, 1}
        );

        tambahSisi(data, faces,
            new float[][]{
                {1, -1, -1}, {-1, -1, -1},
                {-1, 1, -1}, {1, 1, -1}
            },
            new float[]{0, 0, -1}
        );

        tambahSisi(data, faces,
            new float[][]{
                {1, -1, 1}, {1, -1, -1},
                {1, 1, -1}, {1, 1, 1}
            },
            new float[]{1, 0, 0}
        );

        tambahSisi(data, faces,
            new float[][]{
                {-1, -1, -1}, {-1, -1, 1},
                {-1, 1, 1}, {-1, 1, -1}
            },
            new float[]{-1, 0, 0}
        );

        tambahSisi(data, faces,
            new float[][]{
                {-1, 1, 1}, {1, 1, 1},
                {1, 1, -1}, {-1, 1, -1}
            },
            new float[]{0, 1, 0}
        );

        tambahSisi(data, faces,
            new float[][]{
                {-1, -1, -1}, {1, -1, -1},
                {1, -1, 1}, {-1, -1, 1}
            },
            new float[]{0, -1, 0}
        );

        vertices = ubahKeFloat(data);
        indices = ubahKeInt(faces);
    }

    // Constructor pilihan kubus atau sphere
    public MeshData(boolean gunakanSphere) {

        if (gunakanSphere) {
            MeshData sphere = buatSphere(24, 24);
            this.vertices = sphere.vertices;
            this.indices = sphere.indices;
        } else {
            MeshData kubus = new MeshData();
            this.vertices = kubus.vertices;
            this.indices = kubus.indices;
        }
    }

    // Constructor internal untuk menyimpan hasil sphere
    private MeshData(List<Float> data, List<Integer> faces) {
        vertices = ubahKeFloat(data);
        indices = ubahKeInt(faces);
    }

    // Membuat sphere dengan smooth normals
    private MeshData buatSphere(int stacks, int slices) {

        List<Float> data = new ArrayList<>();
        List<Integer> faces = new ArrayList<>();

        for (int i = 0; i <= stacks; i++) {

            double phi = Math.PI * i / stacks;

            for (int j = 0; j <= slices; j++) {

                double theta = 2.0 * Math.PI * j / slices;

                float x = (float) (Math.sin(phi) * Math.cos(theta));
                float y = (float) Math.cos(phi);
                float z = (float) (Math.sin(phi) * Math.sin(theta));

                // Posisi vertex
                data.add(x);
                data.add(y);
                data.add(z);

                // Normal vertex
                data.add(x);
                data.add(y);
                data.add(z);

                // Koordinat UV
                data.add((float) j / slices);
                data.add((float) i / stacks);
            }
        }

        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {

                int awal = i * (slices + 1) + j;
                int berikutnya = awal + slices + 1;

                faces.add(awal);
                faces.add(berikutnya);
                faces.add(awal + 1);

                faces.add(awal + 1);
                faces.add(berikutnya);
                faces.add(berikutnya + 1);
            }
        }

        return new MeshData(data, faces);
    }

    private void tambahSisi(
        List<Float> data,
        List<Integer> faces,
        float[][] posisi,
        float[] normal
    ) {

        float[][] uv = {
            {0, 0}, {1, 0},
            {1, 1}, {0, 1}
        };

        int awal = data.size() / 8;

        for (int i = 0; i < 4; i++) {

            data.add(posisi[i][0]);
            data.add(posisi[i][1]);
            data.add(posisi[i][2]);

            data.add(normal[0]);
            data.add(normal[1]);
            data.add(normal[2]);

            data.add(uv[i][0]);
            data.add(uv[i][1]);
        }

        faces.add(awal);
        faces.add(awal + 1);
        faces.add(awal + 2);

        faces.add(awal);
        faces.add(awal + 2);
        faces.add(awal + 3);
    }

    private float[] ubahKeFloat(List<Float> data) {

        float[] hasil = new float[data.size()];

        for (int i = 0; i < data.size(); i++) {
            hasil[i] = data.get(i);
        }

        return hasil;
    }

    private int[] ubahKeInt(List<Integer> data) {

        int[] hasil = new int[data.size()];

        for (int i = 0; i < data.size(); i++) {
            hasil[i] = data.get(i);
        }

        return hasil;
    }

    public float[] getVertices() {
        return vertices.clone();
    }

    public int[] getIndices() {
        return indices.clone();
    }

    public int getJumlahVertex() {
        return vertices.length / 8;
    }

    public int getJumlahIndices() {
        return indices.length;
    }
}
