package com.ideacarry.utils;

import android.opengl.GLES30;

import com.android.grafika.gles.GlUtil;

public class GLShaderProgram {
    public final int ID;

    public GLShaderProgram(String vertexShaderCode, String fragmentShaderCode) {
        ID = GlUtil.createProgram(vertexShaderCode, fragmentShaderCode);
    }

    public void use() {
        GLES30.glUseProgram(ID);
    }

    public void release() {
        GLES30.glDeleteProgram(ID);
    }

    public void setInt(final String name, final int value) {
        GLES30.glUniform1i(GLES30.glGetUniformLocation(ID, name), value);
    }

    public void setFloat(final String name, final float value) {
        GLES30.glUniform1f(GLES30.glGetUniformLocation(ID, name), value);
    }

    public void setVec2(final String name, final float[] value) {
        GLES30.glUniform2fv(GLES30.glGetUniformLocation(ID, name), 1, value, 0);
    }

    public void setVec2(final String name, float x, float y) {
        GLES30.glUniform2f(GLES30.glGetUniformLocation(ID, name), x, y);
    }

    public void setVec3(final String name, final float[] value) {
        GLES30.glUniform3fv(GLES30.glGetUniformLocation(ID, name), 1, value, 0);
    }

    public void setVec3(final String name, float x, float y, float z) {
        GLES30.glUniform3f(GLES30.glGetUniformLocation(ID, name), x, y, z);
    }

    public void setVec4(final String name, final float[] value) {
        GLES30.glUniform4fv(GLES30.glGetUniformLocation(ID, name), 1, value, 0);
    }

    public void setVec4(final String name, float x, float y, float z, float w) {
        GLES30.glUniform4f(GLES30.glGetUniformLocation(ID, name), x, y, z, w);
    }

    public void setMat4(final String name, final float[] mat) {
        GLES30.glUniformMatrix4fv(GLES30.glGetUniformLocation(ID, name), 1, false, mat, 0);
    }
}
