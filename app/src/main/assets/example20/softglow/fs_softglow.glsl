#version 300 es
precision mediump float;

uniform sampler2D s_texColor;
uniform sampler2D s_blurColor;

in vec2 v_texcoord0;

out vec4 FragColor;

void main() {
    vec4 blurColor = texture(s_blurColor, v_texcoord0);
    vec4 originalColor = texture(s_texColor, v_texcoord0);
    vec4 whiteColor = vec4(1.0, 1.0, 1.0, 1.0);
    float light = 1.1;
    float contrast = 0.9;

    // brightness
    blurColor *= light;

    // contrast
    blurColor = vec4(((blurColor.rgb - vec3(0.5)) * contrast + vec3(0.5)), blurColor.a);

    FragColor = whiteColor - ((whiteColor - blurColor) * (whiteColor - originalColor));
}