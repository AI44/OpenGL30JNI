#version 300 es

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoords;

uniform mat4 uTextureMatrix;

out vec2 TexCoords;

void main()
{
    TexCoords = (uTextureMatrix * vec4(aTexCoords.x, aTexCoords.y, 0.0, 1.0)).xy;
    //TexCoords = aTexCoords;
    gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);
}