#version 300 es
precision mediump float;

// skin filter

in vec2 TexCoords;
out vec4 FragColor;

uniform sampler2D inputTexture;// image texture
uniform sampler2D grayTexture;// gray table
uniform sampler2D lookupTexture;// LUT

uniform highp float levelRangeInv;// range
uniform lowp float levelBlack;// gray level
uniform lowp float alpha;// skin alpha

void main() {
    vec3 textureColor = texture(inputTexture, TexCoords).rgb;

    textureColor = clamp((textureColor - vec3(levelBlack, levelBlack, levelBlack)) * levelRangeInv, 0.0, 1.0);
    textureColor.r = texture(grayTexture, vec2(textureColor.r, 0.5)).r;
    textureColor.g = texture(grayTexture, vec2(textureColor.g, 0.5)).g;
    textureColor.b = texture(grayTexture, vec2(textureColor.b, 0.5)).b;

    mediump float blueColor = textureColor.b * 15.0;

    mediump vec2 quad1;
    quad1.y = floor(blueColor / 4.0);
    quad1.x = floor(blueColor) - (quad1.y * 4.0);

    mediump vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 4.0);
    quad2.x = ceil(blueColor) - (quad2.y * 4.0);

    highp vec2 texPos1;
    texPos1.x = (quad1.x * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.r);
    texPos1.y = (quad1.y * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.g);

    highp vec2 texPos2;
    texPos2.x = (quad2.x * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.r);
    texPos2.y = (quad2.y * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.g);

    lowp vec4 newColor1 = texture(lookupTexture, texPos1);
    lowp vec4 newColor2 = texture(lookupTexture, texPos2);

    lowp vec3 newColor = mix(newColor1.rgb, newColor2.rgb, fract(blueColor));

    textureColor = mix(textureColor, newColor, alpha);

    FragColor = vec4(textureColor, 1.0);
}