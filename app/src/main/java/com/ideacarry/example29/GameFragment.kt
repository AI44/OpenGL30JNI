package com.ideacarry.example29

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication


/**
 * @author Raining
 * @date 2026-01-23 15:32
 */
class GameFragment : AndroidFragmentApplication() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val config = AndroidApplicationConfiguration()
        config.useAccelerometer = false
        config.useCompass = false
        return initializeForView(Main(), config)
    }
}
