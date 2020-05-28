package com.ideacarry.opengl30jni;

import android.app.Activity;

public class ItemData {
    private String name;
    private Class cls;

    public <T extends Activity> ItemData(String name, Class<T> cls) {
        this.name = name;
        this.cls = cls;
    }

    public String getName() {
        return name;
    }

    public Class getActivityClass() {
        return cls;
    }
}
