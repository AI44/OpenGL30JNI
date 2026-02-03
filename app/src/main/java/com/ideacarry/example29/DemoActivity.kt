package com.ideacarry.example29

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.ideacarry.opengl30jni.R

/**
 * @author Raining
 * @date 2026-02-02 21:09
 *
 * #I# gdx
 *
 */
class DemoActivity : AppCompatActivity(), AndroidFragmentApplication.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.example29_fragment)


        // 添加 LibGDX Fragment
        if (savedInstanceState == null) {
            val gameFragment = GameFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, gameFragment)
            transaction.commit()
        }
    }

    override fun exit() {
    }
}