#version 300 es
precision mediump float;

//nv21 -> rgb
//width and height are multiples of 2
uniform sampler2D uTextureY;
uniform sampler2D uTextureVU;

in vec2 TexCoords;

out vec4 FragColor;

void main() {
    vec3 yuv;
    vec3 rgb;
    yuv.r = texture(uTextureY, TexCoords).r - 0.0625f;
    vec2 vu = texture(uTextureVU, TexCoords).rg;
    yuv.g = vu.g - 0.5f;
    yuv.b = vu.r - 0.5f;
    rgb = mat3(
    1.164, 1.164, 1.164,
    0.0, -0.213, 2.112,
    1.793, -0.533, 0.0) * yuv;

    FragColor = vec4(rgb, 1.0);
}
