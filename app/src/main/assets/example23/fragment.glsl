#version 300 es
precision mediump float;

uniform lowp vec3 color;
out lowp vec4 FragColor;

void main() {
    FragColor = vec4(color, 1.0);
}
