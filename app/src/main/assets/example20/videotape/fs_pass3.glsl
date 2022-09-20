#version 300 es
precision mediump float;

uniform sampler2D s_texColor;
uniform vec2 u_inputSize;
uniform float u_progress; // [0.0, 1.0]

in vec2 v_texcoord0;

out vec4 FragColor;

void main() {
    float horizon = 0.4 * 0.6 - 0.2;
    float vertical = (0.4 * 0.55 - 0.2) / (u_inputSize.y / u_inputSize.x);

    vec2 offset = horizon * vec2(0.1, 0.0) + vertical * vec2(0.0, 0.1);

    vec4 cr = texture(s_texColor, v_texcoord0 + offset);
    vec4 cga = texture(s_texColor, v_texcoord0);
    vec4 cb = texture(s_texColor, v_texcoord0 - offset);
    FragColor = vec4(cr.r, cga.g, cb.b, cga.a);
}