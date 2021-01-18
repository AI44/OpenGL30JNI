package com.ideacarry.opengl30jni;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    public static final ArrayList<ItemData> LIST_DATA = new ArrayList<>();

    static {
        System.loadLibrary("native-lib");

        LIST_DATA.add(new ItemData("1.画三角形", com.ideacarry.example1.DemoActivity.class));
        LIST_DATA.add(new ItemData("2.VAO使用", com.ideacarry.example2.DemoActivity.class));
        LIST_DATA.add(new ItemData("3.VBO + VAO + EBO", com.ideacarry.example3.DemoActivity.class));
        LIST_DATA.add(new ItemData("4.uniform", com.ideacarry.example4.DemoActivity.class));
        LIST_DATA.add(new ItemData("5.线性插值 + assets文件调用", com.ideacarry.example5.DemoActivity.class));
        LIST_DATA.add(new ItemData("6.纹理 + 矩阵变换", com.ideacarry.example6.DemoActivity.class));
        LIST_DATA.add(new ItemData("7.坐标系变换 + 深度测试", com.ideacarry.example7.DemoActivity.class));
        LIST_DATA.add(new ItemData("8.lookAt + 欧拉角", com.ideacarry.example8.DemoActivity.class));
        LIST_DATA.add(new ItemData("9.基础光照", com.ideacarry.example9.DemoActivity.class));
        LIST_DATA.add(new ItemData("10.材质光照", com.ideacarry.example10.DemoActivity.class));
        LIST_DATA.add(new ItemData("11.FreeType加载文字", com.ideacarry.example11.DemoActivity.class));
        LIST_DATA.add(new ItemData("12.自建gl环境 + surfaceView", com.ideacarry.example12.DemoActivity.class));
        LIST_DATA.add(new ItemData("13.渲染yuv数据", com.ideacarry.example13.DemoActivity.class));
        LIST_DATA.add(new ItemData("14.CameraX+SurfaceTexture+SurfaceView", com.ideacarry.example14.DemoActivity.class));
        LIST_DATA.add(new ItemData("15.framebuffer", com.ideacarry.example15.DemoActivity.class));
        LIST_DATA.add(new ItemData("16.美颜 + textureView", com.ideacarry.example16.DemoActivity.class));
        LIST_DATA.add(new ItemData("17.LUT滤镜", com.ideacarry.example17.DemoActivity.class));
        LIST_DATA.add(new ItemData("18.离屏渲染", com.ideacarry.example18.DemoActivity.class));
        LIST_DATA.add(new ItemData("19.人脸点渲染图像", com.ideacarry.example19.DemoActivity.class));
        LIST_DATA.add(new ItemData("20.镜头效果测试", com.ideacarry.example20.DemoActivity.class));
        LIST_DATA.add(new ItemData("21.图片效果测试", com.ideacarry.example21.DemoActivity.class));
        LIST_DATA.add(new ItemData("22.光效测试", com.ideacarry.example22.DemoActivity.class));
        LIST_DATA.add(new ItemData("23.模板测试", com.ideacarry.example23.DemoActivity.class));
        LIST_DATA.add(new ItemData("24.texture3D线性插值测试对比", com.ideacarry.example24.DemoActivity.class));
        LIST_DATA.add(new ItemData("25.MediaCodec", com.ideacarry.example25.DemoActivity.class));
        Collections.reverse(LIST_DATA);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView view = findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        view.setLayoutManager(layoutManager);
        ListAdapter adapter = new ListAdapter(LIST_DATA);
        view.setAdapter(adapter);
        adapter.addItemClickListener(data -> {
            Intent intent = new Intent(MainActivity.this, data.getActivityClass());
            startActivity(intent);
        });

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET},
                999);
    }
}
