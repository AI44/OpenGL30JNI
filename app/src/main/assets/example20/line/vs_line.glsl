#version 300 es

layout (location = 0) in vec2 aPos;

uniform float u_size;

void main() {
    gl_PointSize = u_size;
    gl_Position = vec4(aPos, 0.0, 1.0);
}