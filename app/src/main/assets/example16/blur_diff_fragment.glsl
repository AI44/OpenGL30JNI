#version 300 es
precision mediump float;

out vec4 FragColor;
in vec2 TexCoords;

layout (location = 2) uniform sampler2D blurTexture;
layout (location = 3) uniform sampler2D orgTexture;

void main() {
    vec4 iColor = texture(blurTexture, TexCoords);
    vec4 meanColor = texture(orgTexture, TexCoords);
    vec4 diffColor = iColor - meanColor;
    diffColor.r = min(diffColor.r * 50.0f * diffColor.r, 1.0f);
    diffColor.g = min(diffColor.g * 50.0f * diffColor.g, 1.0f);
    diffColor.b = min(diffColor.b * 50.0f * diffColor.b, 1.0f);
    FragColor = vec4(diffColor.rgb, 1.0f);
}