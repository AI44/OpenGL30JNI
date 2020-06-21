#version 300 es
precision mediump float;

layout (location = 2) uniform sampler2D uTexturePic;
layout (location = 3) uniform sampler2D uTextureLUT;
layout (location = 4) uniform float intensity;// 0 - 1.0f

in vec2 TexCoords;

out vec4 FragColor;

void main() {
    vec4 textureColor = texture(uTexturePic, TexCoords);
    float blueColor = textureColor.b * 15.0f;

    vec2 quad1;
    quad1.y = floor(blueColor / 4.0f);
    quad1.x = floor(blueColor) - (quad1.y * 4.0f);

    vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 4.0f);
    quad2.x = ceil(blueColor) - (quad2.y * 4.0f);

    vec2 texPos1;
    texPos1.x = (quad1.x * 0.25f) + 0.5f / 64.0f + ((0.25f - 1.0f / 64.0f) * textureColor.r);
    texPos1.y = (quad1.y * 0.25f) + 0.5f / 64.0f + ((0.25f - 1.0f / 64.0f) * textureColor.g);

    vec2 texPos2;
    texPos2.x = (quad2.x * 0.25f) + 0.5f / 64.0f + ((0.25f - 1.0f / 64.0f) * textureColor.r);
    texPos2.y = (quad2.y * 0.25f) + 0.5f / 64.0f + ((0.25f - 1.0f / 64.0f) * textureColor.g);

    vec4 newColor1 = texture(uTextureLUT, texPos1);
    vec4 newColor2 = texture(uTextureLUT, texPos2);

    vec4 newColor = mix(newColor1, newColor2, fract(blueColor));
    FragColor = mix(textureColor, vec4(newColor.rgb, textureColor.w), intensity);
}