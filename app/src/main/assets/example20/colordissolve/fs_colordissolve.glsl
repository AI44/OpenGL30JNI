#version 300 es
precision highp float;

uniform sampler2D s_texColor;
uniform sampler2D s_texColor1;
uniform float s_progress;

in vec2 v_texcoord0;

out vec4 FragColor;

void main() {
    vec4 backgroundColor = texture(s_texColor, v_texcoord0);
    vec4 frontColor = texture(s_texColor1, v_texcoord0);

    float backgroundColorAverage = (backgroundColor.r + backgroundColor.g + backgroundColor.b) / 3.0;
    float frontColorAverage = (frontColor.r + frontColor.g + frontColor.b) / 3.0;

    float delta = abs(frontColorAverage - backgroundColorAverage);

    float alpha = clamp((delta - s_progress) / (s_progress + 0.00001), 0.0, 1.0);

    FragColor = mix(backgroundColor, frontColor, 1.0 - alpha);
}