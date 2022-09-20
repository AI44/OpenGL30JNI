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

    float x = mod(v_texcoord0.x, 1.0 / column);
    float texX = u_texOffset.x + x * column * u_texOffset.z;

    float y = mod(v_texcoord0.y, 1.0 / row);
    float texY = u_texOffset.y + y * row * u_texOffset.w;

    FragColor = texture(s_texColor, vec2(texX, texY));
}