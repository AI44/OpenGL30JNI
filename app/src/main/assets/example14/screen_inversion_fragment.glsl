#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;

out vec4 FragColor;

in vec2 TexCoords;

uniform samplerExternalOES screenTexture;

void main()
{
    FragColor = vec4(vec3(1.0 - texture(screenTexture, TexCoords)), 1.0);
}