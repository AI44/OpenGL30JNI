#version 300 es
precision mediump float;

out vec4 FragColor;
in highp vec2 TexCoords;

uniform sampler2D inputImageTexture;
uniform sampler2D grayTexture;
uniform sampler2D lookupTexture;

uniform highp float levelRangeInv;//range
uniform lowp float levelBlack;// gray level
uniform lowp float alpha;// skin level

void main() {
    lowp vec3 textureColor = texture(inputImageTexture, TexCoords).rgb;

    textureColor = clamp((textureColor - vec3(levelBlack, levelBlack, levelBlack)) * levelRangeInv, 0.0f, 1.0f);
    textureColor.r = texture(grayTexture, vec2(textureColor.r, 0.5f)).r;
    textureColor.g = texture(grayTexture, vec2(textureColor.g, 0.5f)).g;
    textureColor.b = texture(grayTexture, vec2(textureColor.b, 0.5f)).b;

    mediump float blueColor = textureColor.b * 15.0f;

    mediump vec2 quad1;
    quad1.y = floor(blueColor / 4.0f);
    quad1.x = floor(blueColor) - (quad1.y * 4.0f);

    mediump vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 4.0f);
    quad2.x = ceil(blueColor) - (quad2.y * 4.0f);

    highp vec2 texPos1;
    texPos1.x = (quad1.x * 0.25f) + 0.5f / 64.0f + ((0.25f - 1.0f / 64.0f) * textureColor.r);
    texPos1.y = (quad1.y * 0.25f) + 0.5f / 64.0f + ((0.25f - 1.0f / 64.0f) * textureColor.g);

    highp vec2 texPos2;
    texPos2.x = (quad2.x * 0.25f) + 0.5f / 64.0f + ((0.25f - 1.0f / 64.0f) * textureColor.r);
    texPos2.y = (quad2.y * 0.25f) + 0.5f / 64.0f + ((0.25f - 1.0f / 64.0f) * textureColor.g);

    lowp vec4 newColor1 = texture(lookupTexture, texPos1);
    lowp vec4 newColor2 = texture(lookupTexture, texPos2);

    lowp vec3 newColor = mix(newColor1.rgb, newColor2.rgb, fract(blueColor));

    textureColor = mix(textureColor, newColor, alpha);

    FragColor = vec4(textureColor, 1.0f);
}