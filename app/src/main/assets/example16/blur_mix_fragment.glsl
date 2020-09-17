#version 300 es
precision mediump float;

out vec4 FragColor;
in vec2 TexCoords;

uniform sampler2D orgTexture;
uniform sampler2D firstBlurTexture;
uniform sampler2D secondBlurTexture;
uniform lowp float blurAlpha;

void main() {
    lowp vec4 iColor = texture(orgTexture, TexCoords);
    lowp vec4 meanColor = texture(firstBlurTexture, TexCoords);
    lowp vec4 varColor = texture(secondBlurTexture, TexCoords);
    lowp float alpha = blurAlpha;
    lowp float theta = 0.1f;
    mediump float p = clamp((min(iColor.r, meanColor.r - 0.1f) - 0.2f) * 4.0f, 0.0f, 1.0f);
    mediump float meanVar = max(max(varColor.r, varColor.g), varColor.b);
    mediump float kMin;
    lowp vec3 resultColor;
    kMin = (1.0f - meanVar / (meanVar + theta)) * p * alpha;
    resultColor = mix(iColor.rgb, meanColor.rgb, kMin);
    resultColor = mix(iColor.rgb, resultColor, alpha);
    FragColor = vec4(resultColor, 1.0f);
}