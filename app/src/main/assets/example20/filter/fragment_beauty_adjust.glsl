#version 300 es
precision mediump float;

in vec2 TexCoords;

uniform sampler2D inputTexture;// 输入原图
uniform sampler2D blurTexture;// 原图的高斯模糊纹理
uniform sampler2D highPassBlurTexture;// 高反差保留的高斯模糊纹理
uniform lowp float intensity;// 磨皮程度

out vec4 FragColor;

void main() {
    lowp vec4 sourceColor = texture(inputTexture, TexCoords);
    lowp vec4 blurColor = texture(blurTexture, TexCoords);
    lowp vec4 highPassBlurColor = texture(highPassBlurTexture, TexCoords);
    // 调节蓝色通道值
    mediump float value = clamp((min(sourceColor.b, blurColor.b) - 0.2) * 5.0, 0.0, 1.0);
    // 找到模糊之后RGB通道的最大值
    mediump float maxChannelColor = max(max(highPassBlurColor.r, highPassBlurColor.g), highPassBlurColor.b);
    // 计算当前的强度
    mediump float currentIntensity = (1.0 - maxChannelColor / (maxChannelColor + 0.2)) * value * intensity;
    // 混合输出结果
    lowp vec3 resultColor = mix(sourceColor.rgb, blurColor.rgb, currentIntensity);
    // 输出颜色
    FragColor = vec4(resultColor, 1.0);
}
