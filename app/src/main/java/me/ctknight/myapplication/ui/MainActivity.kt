package me.ctknight.myapplication.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
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

    try {
//      val obj1 = ObjReader.read(resources.openRawResource(R.raw.cube))
//      val id1 = mGLView.scene.addObj(this, obj1, "models/andy.png")
//      val model1 = mGLView.scene.getObj(id1)
//      model1.translated[0] = 5f
      val obj2 = ObjReader.read(resources.assets.open("models/airboat.obj"))
      val id2 = mGLView.scene.addObj(this, obj2, "models/andy.png")
      val model2 = mGLView.scene.getObj(id2)
//      model2.translate[1] = 3f
      val obj3 = ObjReader.read(resources.assets.open("models/andy.obj"))
      val id3 = mGLView.scene.addObj(this, obj3, "models/andy.png")
      val model3 = mGLView.scene.getObj(id3)
      if (model3 != null) {
        model3.size = 30f
        model3.translate[2] = -5f
      }
      val obj4 = ObjReader.read(resources.assets.open("models/earth.obj"))
      val id4 = mGLView.scene.addObj(this, obj4, "models/4096_earth.jpg")
      val model4 = mGLView.scene.getObj(id4)

      if (model4 != null) {
        model4.size = 0.01f
        model4.translate[1] = 10f
      }
    } catch (e: IOException) {
      Log.e(TAG, "onCreate: ", e)
    }

  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    if (hasFocus) {
      mGLView.onResume()
    } else {
      mGLView.onPause()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_main_activity, menu)
    return true
  }

  companion object {
    private val TAG = MainActivity::class.java.simpleName
  }
}
