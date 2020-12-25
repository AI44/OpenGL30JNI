#version 300 es
precision mediump float;

// 优化后的高斯模糊

uniform sampler2D inputTexture;

in vec2 textureCoordinate;
// 高斯算子左右偏移值，当偏移值为2时，高斯算子为5 x 5
const int SHIFT_SIZE = 2;
in vec4 blurShiftCoordinates[SHIFT_SIZE];

out vec4 FragColor;

void main() {
    // 计算当前坐标的颜色值
    vec4 currentColor = texture(inputTexture, textureCoordinate);
    mediump vec3 sum = currentColor.rgb;
    // 计算偏移坐标的颜色值总和
    for (int i = 0; i < SHIFT_SIZE; i++) {
        sum += texture(inputTexture, blurShiftCoordinates[i].xy).rgb;
        sum += texture(inputTexture, blurShiftCoordinates[i].zw).rgb;
    }
    // 求出平均值
    FragColor = vec4(sum * 1.0 / float(2 * SHIFT_SIZE + 1), currentColor.a);
}