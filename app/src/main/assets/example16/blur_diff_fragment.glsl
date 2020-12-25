#version 300 es
precision mediump float;

out vec4 FragColor;
in vec2 TexCoords;

uniform sampler2D blurTexture;
uniform sampler2D orgTexture;

void main() {
    vec4 iColor = texture(blurTexture, TexCoords);
    vec4 meanColor = texture(orgTexture, TexCoords);
    vec4 diffColor = iColor - meanColor;
    diffColor.r = min(diffColor.r * 50.0 * diffColor.r, 1.0);
    diffColor.g = min(diffColor.g * 50.0 * diffColor.g, 1.0);
    diffColor.b = min(diffColor.b * 50.0 * diffColor.b, 1.0);
    FragColor = vec4(diffColor.rgb, 1.0);
}