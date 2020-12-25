#version 300 es

// 优化后的高斯模糊

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoords;

uniform highp float texelWidthOffset;
uniform highp float texelHeightOffset;

out vec2 textureCoordinate;
// 高斯算子左右偏移值，当偏移值为2时，高斯算子为5 x 5
const int SHIFT_SIZE = 2;
out vec4 blurShiftCoordinates[SHIFT_SIZE];

void main() {
    gl_Position = vec4(aPos, 0.0, 1.0);
    textureCoordinate = aTexCoords;
    // 偏移步距
    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);
    // 记录偏移坐标
    for (int i = 0; i < SHIFT_SIZE; i++) {
        blurShiftCoordinates[i] = vec4(textureCoordinate.xy - float(i + 1) * singleStepOffset,
        textureCoordinate.xy + float(i + 1) * singleStepOffset);
    }
}