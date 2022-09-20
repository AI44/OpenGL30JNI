#version 300 es
precision mediump float;

uniform sampler2D s_texColor;
uniform float u_progress; // [0.0, 1.0]

in vec2 v_texcoord0;

out vec4 FragColor;

void main() {
    float time = u_progress * 8.0;
    float amount = sin(time) * 0.03;
    float split = fract(time / 8.0);
    float scanOffset = 0.01;
    vec2 uv = v_texcoord0;
    if (uv.y > split && uv.y < split + 1.5 * scanOffset) {
        uv = float2(uv.x + amount, uv.y);
        uv.x += scanOffset;
        uv.y += scanOffset * sin(time / 2.0);
    }
    FragColor = texture(s_texColor, uv);
}