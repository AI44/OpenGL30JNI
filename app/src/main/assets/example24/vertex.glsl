#version 300 es

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aTex;

uniform mat4 model;
out vec3 tex;

void main() {
    gl_Position = model * vec4(aPos, 1.0);
    tex = aTex;
}
