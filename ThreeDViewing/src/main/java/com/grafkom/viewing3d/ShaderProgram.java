
package com.grafkom.viewing3d;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ShaderProgram {

    private final int programId;

    public ShaderProgram(String vertexPath, String fragmentPath) {

        int vertexShader = compileShader(vertexPath, GL20.GL_VERTEX_SHADER);
        int fragmentShader = compileShader(fragmentPath, GL20.GL_FRAGMENT_SHADER);

        programId = GL20.glCreateProgram();

        GL20.glAttachShader(programId, vertexShader);
        GL20.glAttachShader(programId, fragmentShader);

        GL20.glLinkProgram(programId);

        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL20.GL_FALSE) {
            String error = GL20.glGetProgramInfoLog(programId);
            GL20.glDeleteProgram(programId);
            throw new RuntimeException("Shader linking gagal: " + error);
        }

        GL20.glDetachShader(programId, vertexShader);
        GL20.glDetachShader(programId, fragmentShader);

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    private int compileShader(String path, int type) {

        String source = bacaFile(path);

        int shaderId = GL20.glCreateShader(type);

        GL20.glShaderSource(shaderId, source);
        GL20.glCompileShader(shaderId);

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            String error = GL20.glGetShaderInfoLog(shaderId);
            GL20.glDeleteShader(shaderId);
            throw new RuntimeException("Shader compilation gagal (" + path + "): " + error);
        }

        return shaderId;
    }

    private String bacaFile(String path) {

        try (InputStream input = getClass().getResourceAsStream(path)) {

            if (input == null) {
                throw new RuntimeException("File shader tidak ditemukan: " + path);
            }

            return new String(input.readAllBytes(), StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new RuntimeException("Gagal membaca file shader: " + path, e);
        }
    }

    public void gunakan() {
        GL20.glUseProgram(programId);
    }

    public int getProgramId() {
        return programId;
    }

    public void hapus() {
        GL20.glDeleteProgram(programId);
    }
}
