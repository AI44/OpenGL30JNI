#version 300 es
precision mediump float;

uniform mediump sampler3D cube;
in vec3 tex;

out vec4 FragColor;

void main() {
    //FragColor = texture(cube, vec3(tex.xy, tex.z * 0.75 + 0.125));
    FragColor = texture(cube, tex);
}
