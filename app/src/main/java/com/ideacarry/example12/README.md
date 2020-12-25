## 屏幕渲染

```
1.eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY); //获取显示设备

2.EGL14.eglInitialize(...); //初始化gl版本

3.EGL14.eglChooseConfig(eglDisplay, ...); //配置gl环境

4.EGL14.eglCreateContext(eglDisplay, eglConfig, shareContext, ...); //创建gl context

5.eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface, ...); //创建gl surface

6.EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext); //切换gl context

7.EGL14.eglSwapBuffers(eglDisplay, eglSurface); //交换显存(将surface显存和显示器的显存交换)

8.EGL14.eglDestroySurface(eglDisplay, eglSurface); //销毁gl surface

9.EGL14.eglDestroyContext(eglDisplay, eglContext); //销毁gl context
```

## 离屏渲染

```
5.eglSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, eglConfig, ...); //创建离屏渲染surface
```

## 共享context

```
shareContext = EGL14.eglGetCurrentContext();
eglDisplay = EGL14.eglGetCurrentDisplay();
EGL14.eglGetConfigs(eglDisplay, shareConfig, ...);

new Thread(new Runnable() -> {
    eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
    ...
    EglContext = EGL14.eglCreateContext(eglDisplay, shareConfig, shareContext, ...);
    ...
}).start();
```