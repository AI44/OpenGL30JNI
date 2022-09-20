#version 300 es

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoords;

out vec2 v_texcoord0;

void main() {
    v_texcoord0 = aTexCoords;
    gl_Position = vec4(aPos, 0.0, 1.0);
}