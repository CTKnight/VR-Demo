package me.ctknight.myapplication.drawer

import android.opengl.GLES20

import me.ctknight.myapplication.ModelData
import me.ctknight.myapplication.Scene

abstract class IDrawer {
  abstract fun prepareOnGLThread()

  abstract fun draw(scene: Scene, model: ModelData, vMatrix: FloatArray, pMatrix: FloatArray, mode: Int)

  fun preDraw() {
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
  }
}
