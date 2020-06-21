#version 300 es
precision mediump float;

out vec4 FragColor;
in vec2 TexCoords;

uniform sampler2D inputImageTexture;

in vec4 textureShift_1;
in vec4 textureShift_2;
in vec4 textureShift_3;
in vec4 textureShift_4;

void main() {
    mediump vec3 sum = texture(inputImageTexture, TexCoords).rgb;
    sum += texture(inputImageTexture, textureShift_1.xy).rgb;
    sum += texture(inputImageTexture, textureShift_1.zw).rgb;
    sum += texture(inputImageTexture, textureShift_2.xy).rgb;
    sum += texture(inputImageTexture, textureShift_2.zw).rgb;
    sum += texture(inputImageTexture, textureShift_3.xy).rgb;
    sum += texture(inputImageTexture, textureShift_3.zw).rgb;
    sum += texture(inputImageTexture, textureShift_4.xy).rgb;
    sum += texture(inputImageTexture, textureShift_4.zw).rgb;

    FragColor = vec4(sum * 0.1111f, 1.0f);
}