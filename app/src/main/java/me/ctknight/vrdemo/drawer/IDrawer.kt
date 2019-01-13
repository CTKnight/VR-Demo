/*
 * Copyright 2019 Jiewen Lai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ctknight.vrdemo.drawer

import android.opengl.GLES20

import me.ctknight.vrdemo.ModelData
import me.ctknight.vrdemo.Scene

abstract class IDrawer {
  abstract fun prepareOnGLThread()

  abstract fun draw(scene: Scene, model: ModelData, vMatrix: FloatArray, pMatrix: FloatArray, mode: Int)

  fun preDraw() {
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
  }
}
