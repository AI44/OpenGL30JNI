/*
 * Copyright 2013 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.grafika.gles

import android.graphics.SurfaceTexture
import android.view.Surface

/**
 * Recordable EGL window surface.
 *
 *
 * It's good practice to explicitly release() the surface, preferably from a "finally" block.
 */
class WindowSurface : EglSurfaceBase {
    private var mSurface: Surface? = null
    private var mReleaseSurface = false

    /**
     * Associates an EGL surface with the native window surface.
     *
     *
     * Set releaseSurface to true if you want the Surface to be released when release() is
     * called.  This is convenient, but can interfere with framework classes that expect to
     * manage the Surface themselves (e.g. if you release a SurfaceView's Surface, the
     * surfaceDestroyed() callback won't fire).
     */
    constructor(eglCore: EglCore, surface: Surface, releaseSurface: Boolean) : super(eglCore) {
        createWindowSurface(surface)
        mSurface = surface
        mReleaseSurface = releaseSurface
    }

    /**
     * Associates an EGL surface with the SurfaceTexture.
     */
    constructor(eglCore: EglCore, surfaceTexture: SurfaceTexture) : super(eglCore) {
        createWindowSurface(surfaceTexture)
    }

    /**
     * Releases any resources associated with the EGL surface (and, if configured to do so,
     * with the Surface as well).
     *
     *
     * Does not require that the surface's EGL context be current.
     */
    override fun release() {
        releaseEglSurface()
        mSurface?.let {
            if (mReleaseSurface) {
                it.release()
            }
            mSurface = null
        }
    }

    /**
     * Recreate the EGLSurface, using the new EglBase.  The caller should have already
     * freed the old EGLSurface with releaseEglSurface().
     *
     *
     * This is useful when we want to update the EGLSurface associated with a Surface.
     * For example, if we want to share with a different EGLContext, which can only
     * be done by tearing down and recreating the context.  (That's handled by the caller;
     * this just creates a new EGLSurface for the Surface we were handed earlier.)
     *
     *
     * If the previous EGLSurface isn't fully destroyed, e.g. it's still current on a
     * context somewhere, the create call will fail with complaints from the Surface
     * about already being connected.
     */
    fun recreate(newEglCore: EglCore) {
        val surface = mSurface ?: throw RuntimeException("not yet implemented for SurfaceTexture")
        mEglCore = newEglCore // switch to new context
        createWindowSurface(surface) // create new surface
    }
}