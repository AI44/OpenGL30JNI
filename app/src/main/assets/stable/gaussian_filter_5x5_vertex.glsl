#version 300 es

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoords;

uniform float xOffset;
uniform float yOffset;

out vec2 TexCoords;
#define SHIFT_SIZE 2
out vec4 blurShiftCoordinates[SHIFT_SIZE];//vec4为一对坐标，从中心往外扩散

void main()
{
    TexCoords = aTexCoords;
    gl_Position = vec4(aPos, 0.0f, 1.0f);

    // 偏移步距
    vec2 singleStepOffset = vec2(xOffset, yOffset);

    // 记录偏移坐标
    for (int i = 0; i < SHIFT_SIZE; i++) {
        blurShiftCoordinates[i] = vec4(TexCoords.xy - float(i + 1) * singleStepOffset, TexCoords.xy + float(i + 1) * singleStepOffset);
    }
}