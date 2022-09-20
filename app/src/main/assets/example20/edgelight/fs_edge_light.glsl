#version 300 es
precision mediump float;

uniform sampler2D s_texColor;
uniform sampler2D s_edgeLightColor;

in vec2 v_texcoord0;

out vec4 FragColor;

void main() {
    vec4 inputColor = texture(s_texColor, v_texcoord0);
    vec4 edgeLightColor = texture(s_edgeLightColor, v_texcoord0);

    //FragColor = 1.0 - (1.0 - inputColor) *(1.0 - edgeLightColor);
    //FragColor = edgeLightColor;
    FragColor = inputColor + edgeLightColor;
}