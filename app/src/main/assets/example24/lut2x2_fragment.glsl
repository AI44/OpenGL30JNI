#version 300 es
precision mediump float;

uniform sampler2D lut;

in vec3 tex;

out vec4 FragColor;

void main() {
    float blueColor = tex.b * 3.0;//index

    vec2 quad1;
    quad1.y = floor(blueColor / 2.0);//row index
    quad1.x = floor(blueColor) - (quad1.y * 2.0);//column index

    vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 2.0);
    quad2.x = ceil(blueColor) - (quad2.y * 2.0);

    vec2 texPos1;
    texPos1.x = (quad1.x * 0.5) + 0.5 / 2.0 + ((0.5 - 1.0 / 2.0) * tex.r);
    texPos1.y = (quad1.y * 0.5) + 0.5 / 2.0 + ((0.5 - 1.0 / 2.0) * tex.g);

    vec2 texPos2;
    texPos2.x = (quad2.x * 0.5) + 0.5 / 2.0 + ((0.5 - 1.0 / 2.0) * tex.r);
    texPos2.y = (quad2.y * 0.5) + 0.5 / 2.0 + ((0.5 - 1.0 / 2.0) * tex.g);

    vec4 newColor1 = texture(lut, texPos1);
    vec4 newColor2 = texture(lut, texPos2);

    FragColor = mix(newColor1, newColor2, fract(blueColor));
}