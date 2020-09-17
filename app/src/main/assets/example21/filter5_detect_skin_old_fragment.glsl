#version 300 es
precision mediump float;

in vec2 TexCoords;
out vec4 FragColor;

uniform sampler2D inputTexture;// 输入原图

vec3 RGB2YCbCr(vec3 points){
    float R = points.r * 255.0;
    float G = points.g * 255.0;
    float B = points.b * 255.0;
    float Y = 0.257 * R + 0.504 * G + 0.098 * B + 16.0;
    float Cb = -0.148 * R - 0.291 * G + 0.439 * B + 128.0;
    float Cr = 0.439 * R - 0.368 * G - 0.071 * B + 128.0;
    return vec3(Y,Cb,Cr);
}
float Gaussan(float x,float mean,float var){
    float t = - 0.5 * pow((x - mean),2.0) / var;
    return exp(t);
}
float GetPDF(vec3 point,float meanCb,float varCb,float meanCr,float varCr){
    vec3 YCbCr = RGB2YCbCr(point);
    float pcb = Gaussan(YCbCr.g,meanCb,varCb);
    float pcr = Gaussan(YCbCr.b,meanCr,varCr);
    return 2.0 * pcb * pcr;
}
vec4 f_SkinPDF(vec4 point_color){
    //default setting is computed using special skin data.
    //meanCb-varCb:102-196
    //meanCr-varCr:143-196
    float gray = GetPDF(vec3(point_color),102.0,196.0,143.0,196.0);
    return vec4(gray,gray,gray,point_color.a);
}

void main() {
    vec4 sourceColor = texture(inputTexture, TexCoords);
    FragColor = f_SkinPDF(sourceColor);
}