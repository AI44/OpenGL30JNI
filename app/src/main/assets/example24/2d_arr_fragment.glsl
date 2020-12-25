#version 300 es
precision mediump float;

#define NUM 4.0
const float MAX_INDEX = NUM - 1.0;

uniform mediump sampler2DArray arr;
in vec3 tex;

out vec4 FragColor;

void main() {
    float index = tex.z * NUM;
    float pos0 = index - 0.5;
    float pos1 = clamp(floor(pos0), 0.0, MAX_INDEX);
    float pos2 = clamp(ceil(pos0), 0.0, MAX_INDEX);
    vec4 color1 = texture(arr, vec3(tex.xy, pos1));
    vec4 color2 = texture(arr, vec3(tex.xy, pos2));
    FragColor = mix(color1, color2, fract(pos0));
}
