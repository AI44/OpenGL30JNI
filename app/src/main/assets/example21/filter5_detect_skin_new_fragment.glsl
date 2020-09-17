#version 300 es
precision mediump float;

in vec2 TexCoords;
out vec4 FragColor;

uniform sampler2D inputTexture;// 输入原图

// Convert from RGB to YCbCr.
// The RGB should be in [0, 1] and the output YCbCr will be in [0, 1] as well.
vec3 rgbToYCbCr(const vec3 rgb) {
    return rgb * mat3(
    0.299f, 0.587f, 0.114f,
    -0.168736f, -0.331264f, 0.5f,
    0.5f, -0.418688f, -0.081312f) + vec3(0.0f, 0.501961f, 0.501961f);
}

vec3 rgbToYCbCr2(const vec3 rgb) {
    return rgb * mat3(
    0.256788f, 0.504129f, 0.097906f,
    -0.148224f, -0.290992f, 0.439216f,
    0.439216f, -0.367788f, -0.071427f) + vec3(0.062745f, 0.501961f, 0.501961f);
}

// Convert the output of rgbToYCbCr back to RGB.
vec3 yCbCrToRgb(const vec3 ycbcr)
{
    float cb = ycbcr.y - 0.5f;
    float cr = ycbcr.z - 0.5f;
    float y = ycbcr.x;
    float r = 1.402f * cr;
    float g = -0.344f * cb - 0.714f * cr;
    float b = 1.772f * cb;
    return vec3(r, g, b) + y;
}

float gaussan(const float x, const float mean, const float var){
    float t = - 0.5f * pow((x - mean) * 255.0f, 2.0f) / (var * 255.0f);
    return exp(t);
}

float detectSkin(const vec3 point, const float meanCb, const float varCb, const float meanCr, const float varCr){
    vec3 YCbCr = rgbToYCbCr2(point);
    float pcb = gaussan(YCbCr.g, meanCb, varCb);
    float pcr = gaussan(YCbCr.b, meanCr, varCr);
    return clamp(2.0f * pcb * pcr, 0.0f, 1.0f);
}

void main() {
    vec4 sourceColor = texture(inputTexture, TexCoords);
    float gray = detectSkin(sourceColor.rgb, 102.0f / 255.0f, 196.0f / 255.0f, 143.0f / 255.0f, 196.0f / 255.0f);
    FragColor = vec4(gray, gray, gray, 1.0f);
}