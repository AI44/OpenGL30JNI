#version 300 es
precision mediump float;

uniform sampler2D uTexturePic;
uniform mediump sampler3D uTextureLUT;
uniform float intensity; // 0 - 1.0

in vec2 TexCoords;

out vec4 FragColor;

void main() {
    vec4 colorIn = texture(uTexturePic, TexCoords);
    vec4 colorOut = texture(uTextureLUT, colorIn.rgb);
    FragColor = mix(colorIn, colorOut, intensity);
    //FragColor = vec4(texture(uTextureLUT, vec3(TexCoords, 0.0)).rgb, 1.0);
}
