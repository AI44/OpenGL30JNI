#version 300 es
precision mediump float;

uniform sampler2D s_texColor;
uniform vec2 u_sobelStep;
uniform float u_strength;

in vec2 v_texcoord0;

out vec4 FragColor;

void main() {
    vec2 sobelStep = u_sobelStep;
    float strength = u_strength;
    // 把rgba转成亮度值
    vec3 kRec709Luma = vec3(0.2126, 0.7152, 0.0722);

    vec4 topLeft = texture(s_texColor, vec2(v_texcoord0.x - sobelStep.x, v_texcoord0.y - sobelStep.y));
    vec4 top = texture(s_texColor, vec2(v_texcoord0.x, v_texcoord0.y - sobelStep.y));
    vec4 topRight = texture(s_texColor, vec2(v_texcoord0.x + sobelStep.x, v_texcoord0.y - sobelStep.y));
    vec4 centerLeft = texture(s_texColor, vec2(v_texcoord0.x - sobelStep.x, v_texcoord0.y));
    vec4 centerRight = texture(s_texColor, vec2(v_texcoord0.x + sobelStep.x, v_texcoord0.y));
    vec4 bottomLeft = texture(s_texColor, vec2(v_texcoord0.x - sobelStep.x, v_texcoord0.y + sobelStep.y));
    vec4 bottom = texture(s_texColor, vec2(v_texcoord0.x, v_texcoord0.y + sobelStep.y));
    vec4 bottomRight = texture(s_texColor, vec2(v_texcoord0.x + sobelStep.x, v_texcoord0.y + sobelStep.y));

    vec4 h = -topLeft - 2.0 * top - topRight + bottomLeft + 2.0 * bottom + bottomRight;
    vec4 v = -bottom - 2.0 * centerLeft - topLeft + bottomRight + 2.0 * centerRight + topRight;

    float grayH  = dot(h.rgb, kRec709Luma);
    float grayV  = dot(v.rgb, kRec709Luma);

    float color = length(vec2(grayH, grayV)) * strength;

    FragColor = vec4(color, color, color, 1.0);
}