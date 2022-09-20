#version 300 es
precision mediump float;

uniform sampler2D s_texOrg;
uniform sampler2D s_texMosaic;
uniform sampler2D s_texLine;

in vec2 v_texcoord0;

out vec4 FragColor;

void main() {
    vec4 orgColor = texture(s_texOrg, v_texcoord0);
    vec4 mosaicColor = texture(s_texMosaic, v_texcoord0);
    vec4 lineColor = texture(s_texLine, v_texcoord0);

    vec4 avaliableMosaic = mix(mosaicColor, lineColor, step(lineColor.a, 0.0));
    //FragColor = mix(orgColor, avaliableMosaic, avaliableMosaic.a);
    FragColor = lineColor;
}