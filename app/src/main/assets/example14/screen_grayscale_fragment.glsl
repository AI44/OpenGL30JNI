#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;

out vec4 FragColor;

in vec2 TexCoords;

uniform samplerExternalOES screenTexture;

void main()
{
    //FragColor = texture(screenTexture, TexCoords);
    //float average = (FragColor.r + FragColor.g + FragColor.b) / 3.0;
    //FragColor = vec4(average, average, average, 1.0);

    FragColor = texture(screenTexture, TexCoords);
    float average = 0.2126 * FragColor.r + 0.7152 * FragColor.g + 0.0722 * FragColor.b;
    FragColor = vec4(average, average, average, 1.0);
}