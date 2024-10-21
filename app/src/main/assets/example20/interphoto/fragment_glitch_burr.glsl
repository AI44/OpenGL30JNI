precision highp float;

varying vec2 vTextureCoord;

uniform sampler2D inputImageTexture;

uniform float iTime;
uniform float glitchFactor; // default 1.2
uniform float rgbGlitchFactor; // default 0.02

float hash(vec2 p) {
    float h = dot(p,vec2(127.1,311.7));
    return -1.0 + 2.0*fract(sin(h)*43758.5453123);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    vec2 u = f*f*(3.0-2.0*f);

    return mix(mix(hash( i + vec2(0.0,0.0) ),
                    hash( i + vec2(1.0,0.0) ), u.x),
                mix( hash( i + vec2(0.0,1.0) ),
                    hash( i + vec2(1.0,1.0) ), u.x), u.y);
}

float noise(vec2 p, int oct) {
    mat2 m = mat2( 1.6,  1.2, -1.2,  1.6 );
    float f  = 0.0;

    for (int i = 1; i < 3; i++) {
        float mul = 1.0/pow(2.0, float(i));
        f += mul*noise(p);
        p = m*p;
    }

    return f;
}

void main() {
    vec2 uv = vTextureCoord;

    float time = mod(iTime, 7.0) + 35.0;

    float glitch = pow(cos(time*0.5)*glitchFactor+1.0, glitchFactor);


    if (noise(time+vec2(0, 0))*glitch > 0.62) {
        uv.y = mod(uv.y+noise(vec2(time*4.0, 0)), 1.0);
    }


    vec2 hp = vec2(0.0, uv.y);
    float nh = noise(hp*7.0+time*10.0, 3) * (noise(hp+time*0.3)*0.8);
    nh += noise(hp*100.0+time*10.0, 3)*rgbGlitchFactor;
    float rnd = 0.0;
    if (glitch > 0.0) {
        rnd = hash(uv);
        if (glitch < 1.0) {
             rnd *= glitch;
        }
    }
    nh *= glitch + rnd;
    float r = texture2D(inputImageTexture, fract(uv+vec2(nh, 0.08)*nh)).r;
    float g = texture2D(inputImageTexture, fract(uv+vec2(nh-0.07, 0.0)*nh)).g;
    float b = texture2D(inputImageTexture, fract(uv+vec2(nh, 0.0)*nh)).b;

    vec3 col = vec3(r, g, b);
    gl_FragColor = vec4(col.rgb, 1.0);
}