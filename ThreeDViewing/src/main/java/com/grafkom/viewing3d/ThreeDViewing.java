package com.grafkom.viewing3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

public class ThreeDViewing {

    private long window;

    private final Camera3D camera = new Camera3D(
        new Vector3f(0, 0, 5),
        new Vector3f(0, 0, 0),
        new Vector3f(0, 1, 0)
    );

    private final ProjectionTaxonomyManager projectionManager =
        new ProjectionTaxonomyManager();

    private ShaderProgram shader;
    private ShaderProgram shadowShader;
    private ShadowMapFBO shadowMap;

    private MeshRenderer cubeRenderer;
    private MeshRenderer sphereRenderer;
    private MeshRenderer floorRenderer;
    private Texture2D texture;
    private HSRManager hsrManager;

    private int gridVAO;
    private int gridVBO;
    private int jumlahVertexGrid;

    // 0 = Phong, 1 = Blinn-Phong
    private int shadingModel = 0;

    // 0 = Flat, 1 = Smooth
    private int shadingMode = 1;

    private boolean gunakanSphere = false;
    private boolean gunakanTexture = true;
    private boolean anaglyphMode = false;
    private boolean gunakanBilinear = true;

    private float angle = 0.0f;
    private float radius = 5.0f;
    private float azimuth = 0.0f;
    private float elevation = 20.0f;

    private final Vector3f lightPosition = new Vector3f(3, 5, 3);
    private Matrix4f lightSpaceMatrix;

    public static void main(String[] args) {
        new ThreeDViewing().run();
    }

    public void run() {
        try {
            init();
            loop();
        } finally {
            hapusResource();
            if (window != 0) {
                GLFW.glfwDestroyWindow(window);
            }
            GLFW.glfwTerminate();
        }
    }

    private void init() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Gagal menginisialisasi GLFW");
        }

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(1000, 700, "3D Visual Realism Suite", 0, 0);
        if (window == 0) {
            GLFW.glfwTerminate();
            throw new RuntimeException("Gagal membuat jendela OpenGL");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GL.createCapabilities();

        // Shader utama menangani shading, tekstur, dan shadow mapping.
        shader = new ShaderProgram("/shaders/phong.vert", "/shaders/phong.frag");

        // Shader depth digunakan pada pass pertama shadow mapping.
        shadowShader = new ShaderProgram("/shaders/shadow.vert", "/shaders/shadow.frag");
        shadowMap = new ShadowMapFBO(2048, 2048);

        cubeRenderer = new MeshRenderer(new MeshData());
        sphereRenderer = new MeshRenderer(new MeshData(true));
        floorRenderer = new MeshRenderer(new MeshData(10.0f));

        texture = new Texture2D("/textures/kayu.png", gunakanBilinear);

        hsrManager = new HSRManager();
        hsrManager.aturDepthTest(true);
        hsrManager.aturFaceCulling(false);

        GL11.glClearColor(0.025f, 0.025f, 0.035f, 1.0f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        buatGrid();

        GLFW.glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_PRESS) {
                handleKey(key);
            }
        });
    }

    private void handleKey(int key) {
        switch (key) {
            case GLFW.GLFW_KEY_W: elevation += 5; break;
            case GLFW.GLFW_KEY_S: elevation -= 5; break;
            case GLFW.GLFW_KEY_A: azimuth -= 5; break;
            case GLFW.GLFW_KEY_D: azimuth += 5; break;
            case GLFW.GLFW_KEY_Q: radius += 0.2f; break;
            case GLFW.GLFW_KEY_E: radius -= 0.2f; break;

            case GLFW.GLFW_KEY_1:
                projectionManager.setProjection(ProjectionTaxonomyManager.ProjectionType.PERSPECTIVE);
                break;
            case GLFW.GLFW_KEY_2:
                projectionManager.setProjection(ProjectionTaxonomyManager.ProjectionType.ORTHOGRAPHIC);
                break;
            case GLFW.GLFW_KEY_3:
                projectionManager.setProjection(ProjectionTaxonomyManager.ProjectionType.OBLIQUE);
                break;
            case GLFW.GLFW_KEY_4:
                projectionManager.setProjection(ProjectionTaxonomyManager.ProjectionType.ISOMETRIC);
                break;
            case GLFW.GLFW_KEY_5:
                projectionManager.setProjection(ProjectionTaxonomyManager.ProjectionType.CAVALIER);
                break;
            case GLFW.GLFW_KEY_6:
                projectionManager.setProjection(ProjectionTaxonomyManager.ProjectionType.CABINET);
                break;

            case GLFW.GLFW_KEY_B: shadingModel = 1 - shadingModel; break;
            case GLFW.GLFW_KEY_F: shadingMode = 1 - shadingMode; break;
            case GLFW.GLFW_KEY_Z:
                hsrManager.aturDepthTest(!hsrManager.isDepthTestAktif());
                break;
            case GLFW.GLFW_KEY_C:
                hsrManager.aturFaceCulling(!hsrManager.isFaceCullingAktif());
                break;
            case GLFW.GLFW_KEY_O: gunakanSphere = !gunakanSphere; break;
            case GLFW.GLFW_KEY_T: gunakanTexture = !gunakanTexture; break;
            case GLFW.GLFW_KEY_M: gantiFilterTekstur(); break;
            case GLFW.GLFW_KEY_SPACE: anaglyphMode = !anaglyphMode; break;
            default: break;
        }

        radius = Math.max(2.0f, Math.min(radius, 12.0f));
        elevation = Math.max(-80.0f, Math.min(elevation, 80.0f));
    }

    private void gantiFilterTekstur() {
        gunakanBilinear = !gunakanBilinear;
        if (texture != null) {
            texture.hapus();
        }
        texture = new Texture2D("/textures/kayu.png", gunakanBilinear);
    }

    private void loop() {
        while (!GLFW.glfwWindowShouldClose(window)) {
            int[] width = new int[1];
            int[] height = new int[1];
            GLFW.glfwGetFramebufferSize(window, width, height);

            if (width[0] == 0 || height[0] == 0) {
                GLFW.glfwPollEvents();
                continue;
            }

            float aspect = (float) width[0] / height[0];
            float cameraAzimuth = azimuth;
            float cameraElevation = elevation;

            if (projectionManager.getCurrentProjection()
                    == ProjectionTaxonomyManager.ProjectionType.ISOMETRIC) {
                cameraAzimuth = 45.0f;
                cameraElevation = 35.264f;
            }

            camera.updateOrbitalPosition(radius, cameraAzimuth, cameraElevation);
            Matrix4f view = camera.calculateLookAtMatrix();
            Matrix4f projection = projectionManager.getProjectionMatrix(aspect);

            angle += 0.5f;

            if (anaglyphMode) {
                GL11.glColorMask(true, false, false, true);
                gambarScene(view, projection, -0.04f, width[0], height[0]);

                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
                GL11.glColorMask(false, true, true, true);
                gambarScene(view, projection, 0.04f, width[0], height[0]);

                GL11.glColorMask(true, true, true, true);
            } else {
                gambarScene(view, projection, 0.0f, width[0], height[0]);
            }

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

    private Matrix4f buatModelObjek(float eyeOffset) {
        return new Matrix4f()
            .translation(eyeOffset, 0.0f, 0.0f)
            .rotateY((float) Math.toRadians(angle))
            .rotateX((float) Math.toRadians(angle));
    }

    private void siapkanLightSpaceMatrix() {
        Matrix4f lightView = new Matrix4f().lookAt(
            lightPosition,
            new Vector3f(0, -0.2f, 0),
            new Vector3f(0, 1, 0)
        );

        Matrix4f lightProjection = new Matrix4f().ortho(
            -8.0f, 8.0f, -8.0f, 8.0f, 1.0f, 20.0f
        );

        lightSpaceMatrix = lightProjection.mul(lightView);
    }

    private void gambarScene(Matrix4f view, Matrix4f projection,
                             float eyeOffset, int width, int height) {
        Matrix4f modelObjek = buatModelObjek(eyeOffset);
        siapkanLightSpaceMatrix();

        // PASS 1: render kedalaman dari sudut pandang sumber cahaya.
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        shadowMap.bindForWriting();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        shadowShader.gunakan();
        kirimMatrix(shadowShader, "lightSpaceMatrix", lightSpaceMatrix);
        kirimMatrix(shadowShader, "model", modelObjek);

        if (gunakanSphere) {
            sphereRenderer.render();
        } else {
            cubeRenderer.render();
        }

        shadowMap.unbind();

        // Pulihkan viewport dan status depth test sesuai tombol Z.
        GL11.glViewport(0, 0, width, height);
        if (hsrManager.isDepthTestAktif()) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        } else {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // PASS 2: render scene dengan pencahayaan dan shadow map.
        shader.gunakan();
        kirimMatrix(shader, "view", view);
        kirimMatrix(shader, "projection", projection);
        kirimMatrix(shader, "lightSpaceMatrix", lightSpaceMatrix);

        setVec3(shader, "lightPos", lightPosition);
        setVec3(shader, "viewPos", camera.getEye());
        setVec3(shader, "lightColor", new Vector3f(1, 1, 1));

        setInt(shader, "shadingModel", shadingModel);
        setInt(shader, "shadingMode", shadingMode);

        // Tekstur kayu di unit 0.
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        texture.gunakan();
        setInt(shader, "texture1", 0);

        // Depth texture shadow map di unit 1.
        shadowMap.bindForReading(1);
        setInt(shader, "shadowMap", 1);

        // Lantai digambar lebih dahulu agar menerima bayangan objek.
        kirimMatrix(shader, "model", new Matrix4f());
        setVec3(shader, "objectColor", new Vector3f(0.34f, 0.36f, 0.32f));
        setInt(shader, "useTexture", 0);
        setInt(shader, "useShadow", 1);
        floorRenderer.render();

        // Objek utama.
        kirimMatrix(shader, "model", modelObjek);
        setVec3(shader, "objectColor", new Vector3f(0.75f, 0.78f, 0.95f));
        setInt(shader, "useTexture", gunakanTexture ? 1 : 0);
        setInt(shader, "useShadow", 0);

        if (gunakanSphere) {
            sphereRenderer.render();
        } else {
            cubeRenderer.render();
        }

        // Grid dekoratif di atas lantai, tanpa menerima shadow.
        gambarGrid(view, projection, eyeOffset);
    }

    private void gambarGrid(Matrix4f view, Matrix4f projection, float eyeOffset) {
        shader.gunakan();

        Matrix4f model = new Matrix4f().translation(eyeOffset, 0.0f, 0.0f);
        kirimMatrix(shader, "model", model);
        kirimMatrix(shader, "view", view);
        kirimMatrix(shader, "projection", projection);

        setVec3(shader, "objectColor", new Vector3f(0.16f, 0.18f, 0.17f));
        setInt(shader, "useTexture", 0);
        setInt(shader, "useShadow", 0);

        GL30.glBindVertexArray(gridVAO);
        GL11.glDrawArrays(GL11.GL_LINES, 0, jumlahVertexGrid);
        GL30.glBindVertexArray(0);
    }

    private void buatGrid() {
        float[] vertices = new float[21 * 4 * 3];
        int index = 0;
        float floorY = -1.145f;

        for (int i = -10; i <= 10; i++) {
            vertices[index++] = -10;
            vertices[index++] = floorY;
            vertices[index++] = i;

            vertices[index++] = 10;
            vertices[index++] = floorY;
            vertices[index++] = i;

            vertices[index++] = i;
            vertices[index++] = floorY;
            vertices[index++] = -10;

            vertices[index++] = i;
            vertices[index++] = floorY;
            vertices[index++] = 10;
        }

        jumlahVertexGrid = vertices.length / 3;
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();

        gridVAO = GL30.glGenVertexArrays();
        gridVBO = GL15.glGenBuffers();

        GL30.glBindVertexArray(gridVAO);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, gridVBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false,
                3 * Float.BYTES, 0L);
        GL20.glEnableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
    }

    private void kirimMatrix(ShaderProgram target, String nama, Matrix4f matrix) {
        int location = GL20.glGetUniformLocation(target.getProgramId(), nama);
        if (location < 0) return;

        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.get(buffer);
        GL20.glUniformMatrix4fv(location, false, buffer);
    }

    private void setVec3(ShaderProgram target, String nama, Vector3f nilai) {
        int location = GL20.glGetUniformLocation(target.getProgramId(), nama);
        if (location >= 0) {
            GL20.glUniform3f(location, nilai.x, nilai.y, nilai.z);
        }
    }

    private void setInt(ShaderProgram target, String nama, int nilai) {
        int location = GL20.glGetUniformLocation(target.getProgramId(), nama);
        if (location >= 0) {
            GL20.glUniform1i(location, nilai);
        }
    }

    private void hapusResource() {
        if (cubeRenderer != null) cubeRenderer.hapus();
        if (sphereRenderer != null) sphereRenderer.hapus();
        if (floorRenderer != null) floorRenderer.hapus();
        if (texture != null) texture.hapus();
        if (shader != null) shader.hapus();
        if (shadowShader != null) shadowShader.hapus();
        if (shadowMap != null) shadowMap.hapus();

        if (gridVAO != 0) GL30.glDeleteVertexArrays(gridVAO);
        if (gridVBO != 0) GL15.glDeleteBuffers(gridVBO);
    }
}
