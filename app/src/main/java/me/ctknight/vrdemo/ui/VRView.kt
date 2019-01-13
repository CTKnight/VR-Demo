package me.ctknight.vrdemo.ui

import android.content.Context
import android.util.AttributeSet
import com.google.vr.sdk.base.GvrView
import me.ctknight.vrdemo.VRRenderer

class VRView : GvrView {

  var renderer: VRRenderer? = null
    set(value) {
      setRenderer(value)
    }

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
    // Set the Renderer for drawing on the GLSurfaceView
    setTransitionViewEnabled(true)
  }

  companion object {
    private val TAG = VRView::class.java.simpleName
  }
}
