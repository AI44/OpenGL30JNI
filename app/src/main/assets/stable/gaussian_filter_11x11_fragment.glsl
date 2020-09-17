#version 300 es
precision mediump float;

#define SHIFT_SIZE 5
const float KERNEL[SHIFT_SIZE + 1] = float[](0.109379f, 0.107213f, 0.100969f, 0.0913609f, 0.0794254f, 0.0663417f);

uniform lowp sampler2D inputTexture;

in vec2 TexCoords;
in vec4 blurShiftCoordinates[SHIFT_SIZE];

out lowp vec4 FragColor;

void main() {
    lowp vec4 currentColor = texture(inputTexture, TexCoords);
    vec3 sum = currentColor.rgb * KERNEL[0];
    float percent;
    for (int i = 0; i < SHIFT_SIZE; i++) {
        percent = KERNEL[i+1];
        sum += texture(inputTexture, blurShiftCoordinates[i].xy).rgb * percent;
        sum += texture(inputTexture, blurShiftCoordinates[i].zw).rgb * percent;
    }
    FragColor = vec4(sum, currentColor.a);
}
