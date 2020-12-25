#version 300 es
precision mediump float;

out vec4 FragColor;

in vec2 TexCoords;

uniform sampler2D screenTexture;

const float offset = 1.0 / 720.0;
const vec2 offsets[9] = vec2[](
vec2(-offset, offset), // left top
vec2(0.0, offset), // top
vec2(offset, offset), // right top
vec2(-offset, 0.0), // left
vec2(0.0, 0.0), // center
vec2(offset, 0.0), // right
vec2(-offset, -offset), // left bottom
vec2(0.0, -offset), // bottom
vec2(offset, -offset)// right bottom
);
const float kernel[9] = float[](
1.0 / 16.0, 2.0 / 16.0, 1.0 / 16.0,
2.0 / 16.0, 4.0 / 16.0, 2.0 / 16.0,
1.0 / 16.0, 2.0 / 16.0, 1.0 / 16.0
);

void main()
{
    vec3 sampleTex[9];
    for (int i = 0; i < 9; i++)
    {
        sampleTex[i] = vec3(texture(screenTexture, TexCoords.st + offsets[i]));
    }
    vec3 col = vec3(0.0);
    for (int i = 0; i < 9; i++)
    col += sampleTex[i] * kernel[i];

    FragColor = vec4(col, 1.0);
}