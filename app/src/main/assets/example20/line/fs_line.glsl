#version 300 es
precision mediump float;

uniform sampler2D s_texColor;
uniform vec4 u_color;

out vec4 FragColor;

void main() {
    vec4 mask = texture(s_texColor, vec2(gl_PointCoord.x, 1.0 - gl_PointCoord.y));
    //FragColor = u_color.a * vec4(u_color.rgb, 1.0) * mask;
    FragColor = mask;
}