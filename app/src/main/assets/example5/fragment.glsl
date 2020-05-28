#version 300 es
precision mediump float;
out vec4 fragColor;
in vec4 color;
void main(){
    fragColor = color;
}