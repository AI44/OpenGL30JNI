#version 300 es
precision mediump float;

#define NUM 16.0
const float MAX_INDEX = NUM - 1.0;

uniform sampler2D uTexturePic;
uniform mediump sampler2DArray uTextureLUT;
uniform float intensity; // 0 - 1.0

in vec2 TexCoords;

out vec4 FragColor;

void main() {
    vec4 colorIn = texture(uTexturePic, TexCoords);

    float index = colorIn.b * NUM;
    float pos0 = index - 0.5;
    float pos1 = clamp(floor(pos0), 0.0, MAX_INDEX);
    float pos2 = clamp(ceil(pos0), 0.0, MAX_INDEX);
    vec4 color1 = texture(uTextureLUT, vec3(colorIn.rg, pos1));
    vec4 color2 = texture(uTextureLUT, vec3(colorIn.rg, pos2));
    vec4 colorOut = mix(color1, color2, fract(pos0));

    FragColor = mix(colorIn, colorOut, intensity);
}