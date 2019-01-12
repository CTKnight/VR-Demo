package me.ctknight.myapplication.ui

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.widget.Toast
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.*
import com.google.vr.sdk.audio.GvrAudioEngine
import com.google.vr.sdk.base.GvrActivity
import de.javagl.obj.*
import me.ctknight.myapplication.ModelData
import me.ctknight.myapplication.R
import me.ctknight.myapplication.Scene
import me.ctknight.myapplication.VRRenderer
import permissions.dispatcher.*
import java.io.IOException

@RuntimePermissions
class MainActivity : GvrActivity() {
  private lateinit var mRenderer: VRRenderer
  private lateinit var mScene: Scene
  private lateinit var mBackgroundThread: HandlerThread
  private lateinit var mBackgroundHandler: Handler
  private lateinit var mAudioEngine: GvrAudioEngine

  private var mARSession: Session? = null

  private var quit = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)
    gvrView = findViewById(R.id.surface_view)
    mBackgroundThread = HandlerThread("MainActivity-background")
    mBackgroundThread.start()
    mBackgroundHandler = Handler(mBackgroundThread.looper)

    mAudioEngine = GvrAudioEngine(this, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY)
    mScene = Scene()
    mBackgroundHandler.post {
      initScene(mScene)
    }
    mRenderer = VRRenderer(applicationContext, mScene, this, mARSession, mAudioEngine)
    gvrView.setRenderer(mRenderer)
  }

  private fun initScene(scene: Scene) = try {

    with(scene) {
      val obj1 = ObjReader.read(resources.assets.open("models/plane.obj"))
      val id1 = addObj(ModelData(this@MainActivity, ObjUtils.convertToRenderable(obj1), null, null))
      val model1 = getObj(id1)
      if (model1 != null) {
        model1.translate[1] = -1.7f
      }

//      val modelGroup = buildModelGroup("models/model.obj", "models/model.mtl")
//      modelGroup.forEach {
//        it.translate[0] = -1.3f
//        it.angle[2] = 1f
//        addObj(it)
//      }
//      val treeGroup = buildModelGroup("models/tree.obj", "models/tree.mtl")
//      treeGroup.forEach {
//        it.translate[2] = -0.5f
//        addObj(it)
//      }
//      val tree2Group = buildModelGroup("models/tree.obj", "models/tree.mtl")
//      tree2Group.forEach {
//        it.translate[2] = -0.3f
//        it.translate[0] = -0.5f
//        addObj(it)
//      }
//
//      val tree3Group = buildModelGroup("models/tree.obj", "models/tree.mtl")
//      tree3Group.forEach {
//        it.translate[2] = -0.3f
//        it.translate[0] = 0.5f
//        addObj(it)
//      }

      val islandGroup = buildModelGroup("models/island.obj", "models/island.mtl")
      islandGroup.forEach {
        it.translate[0] = 1f
        it.translate[1] = -1f
        it.translate[2] = -2f
        addObj(it)
      }
    }
    Log.d(TAG, "initScene: all done")
  } catch (e: IOException) {
    Log.e(TAG, "initScene: ", e)
    throw RuntimeException(e)
  }

  private fun buildModelGroup(objPath: String, mtlPath: String): List<ModelData> {
    val originalObj = ObjReader.read(resources.assets.open(objPath))
    val mtl = MtlReader.read(resources.assets.open(mtlPath))
    val materialGroups = ObjSplitting.splitByMaterialGroups(originalObj)
    return materialGroups
        .filter { ObjData.getTotalNumFaceVertices(it.value) < 50000 }
        .map { (name, obj) ->
          ModelData(this, ObjUtils.convertToRenderable(obj), mtl.findLast { it.name == name }, null)
        }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    onRequestPermissionsResult(requestCode, grantResults)
  }

  @NeedsPermission(Manifest.permission.CAMERA)
  fun resumeArSession() {
    if (mARSession == null) {
      val message: String?
      try {
        mARSession = Session(this)
        val config = Config(mARSession)
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        mARSession?.configure(config)
        mRenderer.mARSession = mARSession
      } catch (e: Exception) {
        when (e) {
          is UnavailableArcoreNotInstalledException -> {
            message = "Please install ARCore"
          }
          is UnavailableUserDeclinedInstallationException -> {
            message = "Please install ARCore"
          }
          is UnavailableApkTooOldException -> {
            message = "Please update ARCore"
          }
          is UnavailableSdkTooOldException -> {
            message = "Please update this app"
          }
          is UnavailableDeviceNotCompatibleException -> {
            message = "This device does not support AR"
          }
          else -> {
            message = "Failed to create AR session"
          }
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e(TAG, "Exception creating AR session", e)
      }
    }
    try {
      mARSession?.resume()
    } catch (e: CameraNotAvailableException) {
      Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
    }
    mRenderer.frameUpdater = this
  }

  override fun onStart() {
    super.onStart()
    resumeArSessionWithPermissionCheck()
  }

  override fun onResume() {
    super.onResume()
    mAudioEngine.resume()
  }

  override fun onPause() {
    super.onPause()
    mAudioEngine.pause()
    mARSession?.pause()
  }

  override fun onDestroy() {
    super.onDestroy()
    mBackgroundThread.quitSafely()
    synchronized(this) {
      quit = true
    }
  }

  @OnShowRationale(Manifest.permission.CAMERA)
  fun showRationaleForCamera(request: PermissionRequest) {
//    showRationaleDialog(R.string.permission_camera_rationale, request)
  }

  @OnPermissionDenied(Manifest.permission.CAMERA)
  fun onCameraDenied() {
    Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_SHORT).show()
  }

  @OnNeverAskAgain(Manifest.permission.CAMERA)
  fun onCameraNeverAskAgain() {
    Toast.makeText(this, R.string.permission_camera_never_askagain, Toast.LENGTH_SHORT).show()
  }

  companion object {
    private val TAG = MainActivity::class.java.simpleName
  }
}
