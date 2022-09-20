#version 300 es
precision highp float;

#define STRENGTH 50.0

uniform sampler2D s_texColor;
uniform vec2 u_inputSize; // 渲染图像分辨率(真实)
uniform float u_progress; // [0.0, 1.0]

in vec2 v_texcoord0;

out vec4 FragColor;

#define ITERATIONS 1


//----------------------------------------------------------------------------------------
float hash12(vec2 p)
{
    vec3 p3  = fract(vec3(p.xyx) * .1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float hashOld12(vec2 p)
{
    // Two typical hashes...
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);

    // This one is better, but it still stretches out quite quickly...
    // But it's really quite bad on my Mac(!)
    //return fract(sin(dot(p, vec2(1.0,113.0)))*43758.5453123);

}

vec3 hash33(vec3 p3)
{
    p3 = fract(p3 * vec3(.1031, .1030, .0973));
    p3 += dot(p3, p3.yxz+33.33);
    return fract((p3.xxy + p3.yxx)*p3.zyx);

}

vec3 hashOld33(vec3 p)
{
    p = vec3(dot(p, vec3(127.1, 311.7, 74.7)),
    dot(p, vec3(269.5, 183.3, 246.1)),
    dot(p, vec3(113.5, 271.9, 124.6)));

    return fract(sin(p)*43758.5453123);
}

void main() {
    vec4 color = texture(s_texColor, v_texcoord0);
    float time = u_progress * 50.0 + 10.0;
    float x = (v_texcoord0.x + 4.0) * (v_texcoord0.y + 4.0) * (time * 10.0);
    float grain = (mod((mod(x, 13.0) + 1.0) * (mod(x, 13.0) + 1.0), 0.01) - 0.005) * STRENGTH;
    FragColor = color + vec4(grain, grain, grain, 1.0);


    //    float iTime = u_progress;
    //    vec2 fragCoord = v_texcoord0;
    //    vec2 iResolution = u_inputSize;
    //
    //    vec2 position = fragCoord.xy * iResolution.xy;
    //    vec2 uv = fragCoord.xy;
    //    #if 1
    //    float a = 0.0;
    //    for (int t = 0; t < ITERATIONS; t++)
    //    {
    //        float v = float(t+1)*.152;
    //        vec2 pos = (position * v + iTime * 1500. + 50.0);
    //        a += hash12(pos);
    //    }
    //    vec3 col = vec3(a);
    //    #else
    //    vec3 a = vec3(0.0);
    //    for (int t = 0; t < ITERATIONS; t++)
    //    {
    //        float v = float(t+1)*.132;
    //        vec3 pos = vec3(position, iTime*.3) + iTime * 500. + 50.0;
    //        a += hash33(pos);
    //    }
    //    vec3 col = vec3(a);
    //    #endif
    //    FragColor = mix(color, vec4(col, 1.0), 0.3);
}