#version 300 es

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoords;

layout (location = 2) uniform highp float texelWidthOffset;
layout (location = 3) uniform highp float texelHeightOffset;

out vec2 TexCoords;
out vec4 textureShift_1;
out vec4 textureShift_2;
out vec4 textureShift_3;
out vec4 textureShift_4;

void main() {
    gl_Position = vec4(aPos.xy, 0.0f, 1.0f);
    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);
    TexCoords = aTexCoords.xy;
    textureShift_1 = vec4(aTexCoords.xy - singleStepOffset, aTexCoords.xy + singleStepOffset);
    textureShift_2 = vec4(aTexCoords.xy - 2.0f * singleStepOffset, aTexCoords.xy + 2.0f * singleStepOffset);
    textureShift_3 = vec4(aTexCoords.xy - 3.0f * singleStepOffset, aTexCoords.xy + 3.0f * singleStepOffset);
    textureShift_4 = vec4(aTexCoords.xy - 4.0f * singleStepOffset, aTexCoords.xy + 4.0f * singleStepOffset);
}