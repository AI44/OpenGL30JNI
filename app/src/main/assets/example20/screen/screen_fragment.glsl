#version 300 es
precision mediump float;

out vec4 FragColor;
in vec2 TexCoords;

uniform sampler2D screenTexture;

void main()
{
    FragColor = vec4(texture(screenTexture, TexCoords).rgb, 1.0f);
}