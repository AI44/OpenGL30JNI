#version 300 es
precision mediump float;

uniform lowp sampler2D uTexturePic;
uniform lowp sampler3D uTextureLUT;
uniform float intensity;// 0 - 1.0f

in vec2 TexCoords;

out vec4 FragColor;

void main() {
    lowp vec4 colorIn = texture(uTexturePic, TexCoords);
    lowp vec4 colorOut = texture(uTextureLUT, colorIn.rgb);
    FragColor = mix(colorIn, colorOut, intensity);
    //FragColor = vec4(texture(uTextureLUT, vec3(TexCoords, 0.0f)).rgb, 1.0f);
}
