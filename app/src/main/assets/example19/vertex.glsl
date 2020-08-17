#version 300 es

layout (location = 0) in vec2 aPos;

out vec2 TexCoords;

void main()
{
    TexCoords = (aPos + 1.0f) / 2.0f;//convert to texture coordinate system
    gl_Position = vec4(aPos, 0.0f, 1.0f);
}