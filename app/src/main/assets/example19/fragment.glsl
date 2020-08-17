#version 300 es
precision mediump float;

out vec4 FragColor;
in vec2 TexCoords;

uniform sampler2D imageTexture;

void main() {
    FragColor = vec4(texture(imageTexture, TexCoords).rgb, 1.0f);
}
