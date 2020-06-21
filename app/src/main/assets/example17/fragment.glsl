#version 300 es
precision mediump float;

layout (location = 2) uniform sampler2D uTexturePic;

in vec2 TexCoords;

out vec4 FragColor;

void main() {
    vec3 rgb = texture(uTexturePic, TexCoords).rgb;
    FragColor = vec4(rgb, 1.0f);
}
