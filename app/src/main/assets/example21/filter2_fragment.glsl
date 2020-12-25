#version 300 es
precision mediump float;

in vec2 TexCoords;
out vec4 FragColor;

uniform sampler2D inputTexture;// 输入原图

// Convert from RGB to YCbCr.
// The RGB should be in [0, 1] and the output YCbCr will be in [0, 1] as well.
vec3 rgbToYCbCr(const vec3 rgb) {
    return rgb * mat3(
    0.299, 0.587, 0.114,
    -0.168736, -0.331264, 0.5,
    0.5, -0.418688, -0.081312) + vec3(0.0, 0.501961, 0.501961);
}

vec3 rgbToYCbCr2(const vec3 rgb) {
    return rgb * mat3(
    0.256788, 0.504129, 0.097906,
    -0.148224, -0.290992, 0.439216,
    0.439216, -0.367788, -0.071427) + vec3(0.062745, 0.501961, 0.501961);
}

// Convert the output of rgbToYCbCr back to RGB.
vec3 yCbCrToRgb(const vec3 ycbcr)
{
    float cb = ycbcr.y - 0.5;
    float cr = ycbcr.z - 0.5;
    float y = ycbcr.x;
    float r = 1.402 * cr;
    float g = -0.344 * cb - 0.714 * cr;
    float b = 1.772 * cb;
    return vec3(r, g, b) + y;
}

float gaussan(const float x, const float mean, const float var){
    float t = - 0.5 * pow((x - mean) * 255.0, 2.0) / (var * 255.0);
    return exp(t);
}

float detectSkin(const vec3 point, const float meanCb, const float varCb, const float meanCr, const float varCr){
    vec3 YCbCr = rgbToYCbCr(point);
    float pcb = gaussan(YCbCr.g, meanCb, varCb);
    float pcr = gaussan(YCbCr.b, meanCr, varCr);
    return 2.0 * pcb * pcr;
}

void main() {
    vec4 sourceColor = texture(inputTexture, TexCoords);
    float gray = detectSkin(sourceColor.rgb, 102.0 / 255.0, 196.0 / 255.0, 143.0 / 255.0, 196.0 / 255.0);
    FragColor = vec4(gray, gray, gray, 1.0);
}