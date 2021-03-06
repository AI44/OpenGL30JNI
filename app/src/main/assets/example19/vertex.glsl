#version 300 es

layout (location = 0) in vec2 aPos;

out vec2 TexCoords;

void main()
{
    TexCoords = (aPos + 1.0) / 2.0;//convert to texture coordinate system
    gl_Position = vec4(aPos, 0.0, 1.0);
}