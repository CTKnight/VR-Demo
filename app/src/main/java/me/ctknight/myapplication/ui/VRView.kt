package me.ctknight.myapplication.ui

import android.content.Context
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.google.vr.sdk.base.GvrView
import me.ctknight.myapplication.Scene
import me.ctknight.myapplication.VRRenderer

class VRView : GvrView {
  lateinit var mRenderer: VRRenderer

  private val TOUCH_SCALE_FACTOR = 180.0f / 320
  private val LIGHT_MOVE_FACTOR = 0.01f
  lateinit var scene: Scene

  var isInEdit: Boolean = false
  var isEditLight: Boolean = false
  private var lastEditId: Long = -1

  private lateinit var mDetector: GestureDetectorCompat
  private lateinit var mScaleDetector: ScaleGestureDetector

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
    mRenderer = VRRenderer(this)
    // Set the Renderer for drawing on the GLSurfaceView
    setRenderer(mRenderer)
    setTransitionViewEnabled(true)
    setTransitionViewEnabled(true)
    //        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    // now
//    setupInteraction()
  }

  private fun setupInteraction() {
    mDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
      override fun onDown(e: MotionEvent): Boolean {
        return true
      }

      override fun onSingleTapUp(e: MotionEvent): Boolean {
        if (!isInEdit) {
          return false
        }
        val x = e.x
        val y = e.y
        queueEvent {
          val id = mRenderer.getId(x.toInt(), y.toInt())

          Log.d(TAG, "onTouchEvent: $id")
          synchronized(scene) {
            val obj = scene.getObj(id)
            val lastObj = scene.getObj(lastEditId)
            if (lastObj != null) {
              lastObj.isHighlight = false
            }
            if (obj != null) {
              obj.isHighlight = true
            }
          }

          lastEditId = id
        }

        return true
      }

      override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {

        if (isEditLight) {
          val lightPos = scene.lightPos
          lightPos[0] += LIGHT_MOVE_FACTOR * distanceX
          lightPos[1] += LIGHT_MOVE_FACTOR * distanceY
          return true
        }
        if (isInEdit) {
          val obj = scene.getObj(lastEditId)
          if (obj != null) {
            val translate = obj.translate
            val angle = mRenderer.angle
            var yAngle = angle[1] - 90
            yAngle *= (Math.PI / 180.0f).toFloat()
            translate[0] += (LIGHT_MOVE_FACTOR.toDouble() * distanceX.toDouble() * Math.sin(yAngle.toDouble())).toFloat()
            translate[1] += LIGHT_MOVE_FACTOR * distanceY
            translate[2] += (LIGHT_MOVE_FACTOR.toDouble() * distanceX.toDouble() * Math.cos(yAngle.toDouble())).toFloat()
          }
          return true
        }
        val angle = mRenderer.angle
        angle[1] += distanceX * TOUCH_SCALE_FACTOR
        if (angle[1] > 360) {
          angle[1] -= 360f
        }
        angle[0] -= distanceY * TOUCH_SCALE_FACTOR
        if (angle[0] < -89.9) {
          angle[0] = -89.9f
        }
        if (angle[0] > 89.9) {
          angle[0] = 89.9f
        }
        return true
      }

      override fun onLongPress(e: MotionEvent) {

      }
    })
    mScaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
      override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (mRenderer.isFlying) {
          return true
        }
        if (isInEdit) {
          val obj = scene.getObj(lastEditId)
          if (obj != null) {
            obj.size = obj.size * detector.scaleFactor
          }
          return true
        }
        mRenderer.distance = mRenderer.distance / (1.00f * detector.scaleFactor)
        return true
      }
    })
  }

//  override fun onTouchEvent(event: MotionEvent): Boolean {
//    this.mDetector.onTouchEvent(event)
//    this.mScaleDetector.onTouchEvent(event)
//    return true
//  }

  companion object {
    private val TAG = VRView::class.java.simpleName
  }
}
