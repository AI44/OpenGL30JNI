#version 300 es
precision mediump float;

uniform sampler2D s_texColor;
uniform sampler2D s_texLut;

uniform float u_top;// 上边界
uniform float u_bottom;// 下边界
uniform float u_scale;// 缩放倍数 > 1.0

in vec2 v_texcoord0;

out vec4 FragColor;

vec4 getLutColor(vec4 inColor) {
    float blueColor = inColor.b * 63.0;//index

    vec2 quad1;
    quad1.y = floor(blueColor / 8.0);//row index
    quad1.x = floor(blueColor) - (quad1.y * 8.0);//column index

    vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 8.0);
    quad2.x = ceil(blueColor) - (quad2.y * 8.0);

    vec2 texPos1;
    texPos1.x = (quad1.x * 0.125) + 0.5 / 512.0 + ((0.125 - 1.0 / 512.0) * inColor.r);
    texPos1.y = (quad1.y * 0.125) + 0.5 / 512.0 + ((0.125 - 1.0 / 512.0) * inColor.g);

    vec2 texPos2;
    texPos2.x = (quad2.x * 0.125) + 0.5 / 512.0 + ((0.125 - 1.0 / 512.0) * inColor.r);
    texPos2.y = (quad2.y * 0.125) + 0.5 / 512.0 + ((0.125 - 1.0 / 512.0) * inColor.g);

    vec4 newColor1 = texture(s_texLut, texPos1);
    vec4 newColor2 = texture(s_texLut, texPos2);

    return mix(newColor1, newColor2, fract(blueColor));
}

vec2 getScaleCoord(vec2 coord) {
    float size = 1.0 / u_scale;
    float padding = (1.0 - size) / 2.0;
    float x = padding + coord.x * size;
    float y = padding + coord.y * size;
    float offset = size * (u_bottom - u_top) / 2.0;
    float s = sign(step(coord.y, 0.5) - 0.5);
    return vec2(x, y + offset * s);
}

void main() {
    vec4 orgColor = texture(s_texColor, v_texcoord0);
    vec4 blackColor = getLutColor(texture(s_texColor, getScaleCoord(v_texcoord0)));
    float top = step(u_top, v_texcoord0.y);
    float bottom = step(v_texcoord0.y, u_bottom);
    float flag = top * bottom;
    FragColor = orgColor * flag + blackColor * (1.0 - flag);
}