[转载LearnOpenGL](https://learnopengl-cn.github.io)

视口
---

```oclight
glViewport(0, 0, 800, 600);
```

OpenGL幕后使用glViewport中定义的位置和宽高进行2D坐标的转换，将OpenGL中的位置坐标转换为你的屏幕坐标。
例如，OpenGL中的坐标(-0.5, 0.5)有可能（最终）被映射为屏幕中的坐标(200,450)。
注意，处理过的OpenGL坐标范围只为-1到1，因此我们事实上将(-1到1)范围内的坐标映射到(0, 800)和(0, 600)。


```oclight
glViewport(int x, int y, int width, int height)
```

![viewport](viewport.png)