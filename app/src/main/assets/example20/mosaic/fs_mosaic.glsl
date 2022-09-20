#version 300 es
precision mediump float;

uniform sampler2D s_texColor;
uniform vec2 u_mosaicSize;// 马赛克方格 size [0.0, 1.0]

in vec2 v_texcoord0;

out vec4 FragColor;

void main() {
    float w = u_mosaicSize.x;
    float h = u_mosaicSize.y;
    float x = w * floor(v_texcoord0.x / w) + 0.5 * w;
    float y = h * floor(v_texcoord0.y / h) + 0.5 * h;

    FragColor = texture(s_texColor, vec2(x, y));
}