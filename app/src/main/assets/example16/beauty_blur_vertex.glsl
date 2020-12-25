#version 300 es

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoords;

uniform highp float texelWidthOffset;
uniform highp float texelHeightOffset;

out vec2 TexCoords;
out vec4 textureShift_1;
out vec4 textureShift_2;
out vec4 textureShift_3;
out vec4 textureShift_4;

void main() {
    gl_Position = vec4(aPos.xy, 0.0, 1.0);
    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);
    TexCoords = aTexCoords.xy;
    textureShift_1 = vec4(aTexCoords.xy - singleStepOffset, aTexCoords.xy + singleStepOffset);
    textureShift_2 = vec4(aTexCoords.xy - 2.0 * singleStepOffset, aTexCoords.xy + 2.0 * singleStepOffset);
    textureShift_3 = vec4(aTexCoords.xy - 3.0 * singleStepOffset, aTexCoords.xy + 3.0 * singleStepOffset);
    textureShift_4 = vec4(aTexCoords.xy - 4.0 * singleStepOffset, aTexCoords.xy + 4.0 * singleStepOffset);
}