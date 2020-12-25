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
    float v = vu.r;
    float u = vu.g;

    y = 1.164 *(y - 0.0625);
    u = u - 0.5;
    v = v - 0.5;

    //1.596023559570*v
    float r = y + 1.596*v;
    //0.3917694091796875*u - 0.8129730224609375*v
    float g = y - 0.392*u - 0.813*v;
    //2.017227172851563*u
    float b = y + 2.017*u;

    FragColor = vec4(r, g, b, 1.0);
}
