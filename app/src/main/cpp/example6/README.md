[转载LearnOpenGL](https://learnopengl-cn.github.io)

纹理环绕方式
-----------

![texture_wrapping](texture_wrapping.png)

```oclight
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
```

如果我们选择GL_CLAMP_TO_BORDER选项，我们还需要指定一个边缘的颜色。
```oclight
float borderColor[] = { 1.0f, 1.0f, 0.0f, 1.0f };
glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor);
```


纹理过滤
-------

![texture_filtering](texture_filtering.png)

```oclight
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
```

多级渐远纹理
-----------

![mipmaps](mipmaps.png)

```oclight
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
```


2D纹理坐标范围
-------------

![tex_coords](tex_coords.png)