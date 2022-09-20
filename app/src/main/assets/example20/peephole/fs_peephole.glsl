#version 300 es
precision mediump float;

uniform sampler2D s_texColor;
// 中心点
const vec2 center = vec2(.5, .5);
// 高度和宽度的比例，> 0
uniform float u_ratio;
// 鱼眼半径 (0.0, 1.0]
uniform float u_radius;
// 中心图像凸起程度 (0.0, 1.0]
uniform float u_scale;

in vec2 v_texcoord0;

out vec4 FragColor;

void main() {
    float aspectRatio = u_ratio;
    float radius = u_radius;
    float scale = u_scale;

    radius = min(radius, radius*aspectRatio);

    vec2 textureCoordinateToUse = vec2(v_texcoord0.x, ((v_texcoord0.y - center.y) * aspectRatio) + center.y);
    float dist = distance(center, textureCoordinateToUse);
    textureCoordinateToUse = v_texcoord0;

    textureCoordinateToUse -= center;
    float percent = 1.0 - ((radius - dist) / radius) * scale;
    percent = pow(percent, 2.0);

    textureCoordinateToUse = textureCoordinateToUse * percent;
    textureCoordinateToUse += center;

    float edgeMix = smoothstep(radius - 0.02, radius + 0.02, dist);

    vec4 finalColor = texture(s_texColor, textureCoordinateToUse);
    FragColor = mix(finalColor, vec4(0.0, 0.0, 0.0, 1.0), edgeMix);
}