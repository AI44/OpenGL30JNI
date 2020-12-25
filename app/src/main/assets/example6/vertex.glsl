#version 300 es
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aColor;
layout (location = 2) in vec2 aTexCoord;

out vec3 ourColor;
out vec2 TexCoord;

uniform mat4 transform;

void main()
{
    //gl_Position = vec4(aPos.x, -aPos.y, aPos.z, 1.0);
    //gl_Position = vec4(aPos, 1.0);
    gl_Position = transform * vec4(aPos, 1.0);
    ourColor = aColor;
    //TexCoord = aTexCoord;
    TexCoord = vec2(aTexCoord.x, 1.0 - aTexCoord.y);
}