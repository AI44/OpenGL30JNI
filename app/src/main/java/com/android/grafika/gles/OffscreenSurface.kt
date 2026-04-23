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

/**
 * Off-screen EGL surface (pbuffer).
 *
 *
 * It's good practice to explicitly release() the surface, preferably from a "finally" block.
 */
class OffscreenSurface(eglCore: EglCore, width: Int, height: Int) : EglSurfaceBase(eglCore) {
    /**
     * Releases any resources associated with the surface.
     */
    override fun release() {
        releaseEglSurface()
    }

    /**
     * Creates an off-screen surface with the specified width and height.
     */
    init {
        createOffscreenSurface(width, height)
    }
}