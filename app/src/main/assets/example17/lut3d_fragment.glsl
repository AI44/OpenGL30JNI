#version 300 es
precision mediump float;

layout (location = 2) uniform sampler2D uTexturePic;
layout (location = 3) uniform sampler3D uTextureLUT;
layout (location = 4) uniform float intensity;// 0 - 1.0f

in vec2 TexCoords;

out vec4 FragColor;

void main() {
    vec4 colorIn = texture(uTexturePic, TexCoords);
    vec4 colorOut = texture(uTextureLUT, colorIn.rgb);
    FragColor = mix(colorIn, colorOut, intensity);
    //FragColor = vec4(texture(uTextureLUT, vec3(TexCoords, 0.0f)).rgb, 1.0f);
}
