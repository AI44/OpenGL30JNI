#version 300 es

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoords;

uniform float texelWidthOffset;
uniform float texelHeightOffset;

// 高斯算子左右偏移值，当偏移值为5时，高斯算子为 11 x 11
const int SHIFT_SIZE = 5;
out vec2 textureCoordinate;
out vec4 blurShiftCoordinates[SHIFT_SIZE];

void main() {
    gl_Position = vec4(aPos, 0.0f, 1.0f);
    textureCoordinate = aTexCoords;

    // 偏移步距
    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);

    // 记录偏移坐标
    for (int i = 0; i < SHIFT_SIZE; i++) {
        blurShiftCoordinates[i] = vec4(textureCoordinate.xy - float(i + 1) * singleStepOffset,
        textureCoordinate.xy + float(i + 1) * singleStepOffset);
    }
}