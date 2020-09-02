#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;

out vec4 FragColor;

in vec2 TexCoords;

uniform samplerExternalOES screenTexture;

void main()
{
    vec3 col = texture(screenTexture, TexCoords).rgb;
    FragColor = vec4(col, 1.0f);
}