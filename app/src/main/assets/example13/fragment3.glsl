#version 300 es
precision mediump float;

//nv21 -> rgb
//width and height are multiples of 2
uniform sampler2D uTextureY;
uniform sampler2D uTextureVU;

in vec2 TexCoords;

out vec4 FragColor;

void main() {
    float y = texture(uTextureY, TexCoords).r;
    vec2 vu = texture(uTextureVU, TexCoords).rg;
    float v = vu.r - 0.5f;
    float u = vu.g - 0.5f;

    float r = y + (1.370705f * v);
    float g = y - (0.698001f * v) - (0.337633f * u);
    float b = y + (1.732446f * u);
    r = clamp(r, 0.0f, 1.0f);
    g = clamp(g, 0.0f, 1.0f);
    b = clamp(b, 0.0f, 1.0f);

    FragColor = vec4(r, g, b, 1.0f);
}
