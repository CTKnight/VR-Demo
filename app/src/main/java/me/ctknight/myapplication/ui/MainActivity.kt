package me.ctknight.myapplication.ui

import android.os.Bundle
import android.util.Log
import com.google.vr.sdk.base.GvrActivity
import de.javagl.obj.ObjReader
import me.ctknight.myapplication.R
import java.io.IOException

class MainActivity : GvrActivity() {
  private lateinit var mGLView: VRView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)
    mGLView = findViewById(R.id.surface_view)
    gvrView = mGLView
    try {
//      val obj1 = ObjReader.read(resources.openRawResource(R.raw.cube))
//      val id1 = mGLView.scene.addObj(this, obj1, "models/andy.png")
//      val model1 = mGLView.scene.getObj(id1)
//      model1.translated[0] = 5f

      with(mGLView.scene) {
        val obj2 = ObjReader.read(resources.assets.open("models/airboat.obj"))
        val id2 = addObj(this@MainActivity, obj2, "models/andy.png")
        val model2 = getObj(id2)

        val obj3 = ObjReader.read(resources.assets.open("models/andy.obj"))
        val id3 = addObj(this@MainActivity, obj3, "models/andy.png")
        val model3 = getObj(id3)
        if (model3 != null) {
          model3.size = 30f
          model3.translate[2] = -5f
        }
        val obj4 = ObjReader.read(resources.assets.open("models/earth.obj"))
        val id4 = addObj(this@MainActivity, obj4, "models/4096_earth.jpg")
        val model4 = getObj(id4)

        if (model4 != null) {
          model4.size = 0.01f
          model4.translate[1] = 10f
        }
      }

    } catch (e: IOException) {
      Log.e(TAG, "onCreate: ", e)
      throw RuntimeException(e)
    }
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    if (hasFocus) {
      mGLView.onResume()
    } else {
      mGLView.onPause()
    }
  }

  companion object {
    private val TAG = MainActivity::class.java.simpleName
  }
}
