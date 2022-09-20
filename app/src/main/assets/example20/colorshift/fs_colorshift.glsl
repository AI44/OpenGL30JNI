#version 300 es
precision mediump float;

uniform sampler2D s_texColor;
uniform vec2 u_offset;

in vec2 v_texcoord0;

out vec4 FragColor;

void main() {
    vec4 shadowR = texture(s_texColor, v_texcoord0 + u_offset);
    vec4 shadowRColor = vec4(shadowR.x, 0, 0, 1);

    vec4 shadowG = texture(s_texColor, v_texcoord0);
    vec4 shadowGColor = vec4(0, shadowG.y, 0, 1);

    vec4 shadowB = texture(s_texColor, v_texcoord0 - u_offset);
    vec4 shadowBColor = vec4(0, 0, shadowB.z, 1);

    FragColor = (shadowRColor + shadowGColor + shadowBColor);
}