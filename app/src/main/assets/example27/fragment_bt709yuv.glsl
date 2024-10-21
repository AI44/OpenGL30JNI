#version 300 es
precision mediump float;

uniform sampler2D uTextureY;
uniform sampler2D uTextureVU;

in vec2 TexCoords;

out vec4 FragColor;

// #I# yuv format
// NV12: YYYY YYYY YYYY YYYY UVUV UVUV
// NV21: YYYY YYYY YYYY YYYY VUVU VUVU
// COLOR_FormatYUV420SemiPlanar -> NV12
// Camera -> NV21
// U -> Cb
// V -> Cr
void main() {
    vec3 yuv;
    vec3 rgb;
    // nv12
    yuv.r = texture(uTextureY, TexCoords).r - 0.0625;// y, 16/256
    vec2 vu = texture(uTextureVU, TexCoords).xy;
    yuv.g = vu.x - 0.5;// cb, U, 128/256
    yuv.b = vu.y - 0.5;// cr, V, 128/256
    rgb = mat3(
    1.164, 1.164, 1.164,
    0.0, -0.213, 2.112,
    1.793, -0.533, 0.0) * yuv;

    FragColor = vec4(rgb, 1.0);
}
