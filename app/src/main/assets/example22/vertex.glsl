#version 300 es

layout (location = 0) in vec2 aPos;//顶点坐标
layout (location = 1) in vec2 aTexCoords;//纹理坐标

uniform float screenWidth;//屏幕的长宽
uniform float screenHeight;

uniform float lightWidth;//光效的长宽
uniform float lightHeight;

uniform highp mat4 matrix;//光效位置逆矩阵

out vec2 ImageTexCoords;
out vec2 LightTexCoords;

void main() {
    ImageTexCoords = aTexCoords;
    gl_Position = vec4(aPos.x, -aPos.y, 0.0f, 1.0f);

    vec4 lightPosition = matrix * vec4(screenWidth * aTexCoords.x, screenHeight * aTexCoords.y, 0.0f, 1.0f);//使用屏幕真实坐标反算光效位置(这里是真实坐标)
    LightTexCoords = vec2(lightPosition.x / lightWidth, lightPosition.y / lightHeight);//真实坐标转换为逻辑坐标
}
