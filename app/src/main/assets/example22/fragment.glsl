#version 300 es
precision mediump float;

in vec2 ImageTexCoords;//图片纹理坐标
in vec2 LightTexCoords;//光效纹理坐标

uniform lowp sampler2D imageTexture;//图片texture
uniform lowp sampler2D lightTexture;//光效texture,Textrue wrap mode使用GL_CLAMP_TO_BORDER模式，具体代码如下：
//float borderColor[] = { 0.0, 0.0, 0.0, 1.0 };//超出部分使用黑色，如果图片周围是黑色的，那么也可以用GL_CLAMP_TO_EDGE模式
//glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor);

out lowp vec4 FragColor;

void main() {
    vec4 imageColor = texture(imageTexture, ImageTexCoords);//图片颜色
    vec4 lightColor = texture(lightTexture, LightTexCoords);//光效颜色，可能是透明的，如果坐标没命中光效的坐标范围就是透明的
    //这里做光效混合
    //FragColor = imageColor;
    FragColor = 1.0 - (1.0 - imageColor) * (1.0 - lightColor);//滤色C=1-(1-A)*(1-B)
}
