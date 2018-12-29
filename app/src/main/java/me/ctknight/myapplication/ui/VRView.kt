package me.ctknight.myapplication.ui

import android.content.Context
import android.util.AttributeSet
import com.google.vr.sdk.base.GvrView
import me.ctknight.myapplication.Scene
import me.ctknight.myapplication.VRRenderer

class VRView : GvrView {
  lateinit var mRenderer: VRRenderer

  lateinit var scene: Scene

  var isInEdit: Boolean = false
  var isEditLight: Boolean = false

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  private fun init(context: Context) {
    //    super(context);

    // Create an OpenGL ES 2.0 context
    setEGLContextClientVersion(2)
    setEGLConfigChooser(8, 8, 8, 8, 16, 0) // Alpha used for plane blending.

    scene = Scene()
    mRenderer = VRRenderer(context.applicationContext, scene)
    // Set the Renderer for drawing on the GLSurfaceView
    setRenderer(mRenderer)
    setTransitionViewEnabled(true)
  }

  companion object {
    private val TAG = VRView::class.java.simpleName
  }
}
