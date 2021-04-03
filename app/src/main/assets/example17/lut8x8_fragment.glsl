#version 300 es
precision mediump float;

uniform sampler2D uTexturePic;
uniform sampler2D uTextureLUT;
uniform float intensity; // 0 - 1.0

in vec2 TexCoords;

out vec4 FragColor;

void main() {
    vec4 textureColor = texture(uTexturePic, TexCoords);
    float blueColor = textureColor.b * 63.0; //index

    vec2 quad1;
    quad1.y = floor(blueColor / 8.0); //row index
    quad1.x = floor(blueColor) - (quad1.y * 8.0); //column index

    vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 7.999);
    quad2.x = ceil(blueColor) - (quad2.y * 8.0);

    vec2 texPos1;
    texPos1.x = (quad1.x * 0.125) + 0.5 / 512.0 + ((0.125 - 1.0 / 512.0) * textureColor.r);
    texPos1.y = (quad1.y * 0.125) + 0.5 / 512.0 + ((0.125 - 1.0 / 512.0) * textureColor.g);

    vec2 texPos2;
    texPos2.x = (quad2.x * 0.125) + 0.5 / 512.0 + ((0.125 - 1.0 / 512.0) * textureColor.r);
    texPos2.y = (quad2.y * 0.125) + 0.5 / 512.0 + ((0.125 - 1.0 / 512.0) * textureColor.g);

    vec4 newColor1 = texture(uTextureLUT, texPos1);
    vec4 newColor2 = texture(uTextureLUT, texPos2);

    vec4 newColor = mix(newColor1, newColor2, fract(blueColor));
    FragColor = mix(textureColor, vec4(newColor.rgb, textureColor.w), intensity);
}