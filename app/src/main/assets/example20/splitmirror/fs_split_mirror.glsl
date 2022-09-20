#version 300 es
precision mediump float;

uniform sampler2D s_texColor;
// 纹理偏移数据(x,y,w,h)
uniform vec4 u_texOffset;
// 列 >= 1
uniform int u_column;
// 行 >= 1
uniform int u_row;

in vec2 v_texcoord0;

out vec4 FragColor;

void main() {
    float column = float(u_column);
    float row = float(u_row);
    float w = 1.0 / column;
    float h = 1.0 / row;
    float index = floor(v_texcoord0.x / w) + floor(v_texcoord0.y / h) * column;
    // 偶数做镜像
    float even = step(0.5, mod(index, 2.0));

    float x = mod(v_texcoord0.x, w);
    float texX = u_texOffset.x + x * column * u_texOffset.z;
    texX = (1.0 - even) * texX + even * (1.0 - texX);

    float y = mod(v_texcoord0.y, h);
    float texY = u_texOffset.y + y * row * u_texOffset.w;

    FragColor = texture(s_texColor, vec2(texX, texY));
}