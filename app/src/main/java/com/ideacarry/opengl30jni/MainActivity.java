package com.ideacarry.opengl30jni;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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
        LIST_DATA.add(new ItemData("11.freetype加载文字", com.ideacarry.example11.DemoActivity.class));
        LIST_DATA.add(new ItemData("12.自建gl环境 + surfaceView", com.ideacarry.example12.DemoActivity.class));
        LIST_DATA.add(new ItemData("13.渲染yuv数据", com.ideacarry.example13.DemoActivity.class));
        LIST_DATA.add(new ItemData("14.CameraX+SurfaceTexture+SurfaceView", com.ideacarry.example14.DemoActivity.class));
        LIST_DATA.add(new ItemData("15.framebuffer", com.ideacarry.example15.DemoActivity.class));
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
    }
}
