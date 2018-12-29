package me.ctknight.myapplication

import android.content.Context
import android.util.LongSparseArray
import de.javagl.obj.Obj
import java.util.concurrent.atomic.AtomicLong

class Scene {

  val lightPos = floatArrayOf(0f, 3f, -2f)
  private val counter = AtomicLong(1)

  val objMap = LongSparseArray<ModelData>()

  private var left = false

  fun addObj(context: Context, obj: Obj, mtl: String): Long {
    val id = counter.getAndIncrement()
    objMap.append(id, ModelData(context, id, obj, mtl))
    return id
  }

  fun removeObj(id: Long) {
    objMap.remove(id)
  }

  fun getObj(id: Long): ModelData? {
    return objMap[id]
  }

  // update object coord
  fun updateOObjMotion() {
    //      getObj(0).getAngle()[2] += 0.1;
    //      getObj(0).getAngle()[2] += 0.1;
    val airboat = getObj(1)

    if (airboat != null) {
      if (left && airboat.translate[0] > 15 || !left && airboat.translate[0] < -15) {
        left = !left
      }

      if (left) {
        airboat.translate[0] += 0.1f
      } else {
        airboat.translate[0] -= 0.1f
      }
    }

    val earth = getObj(3)
    if (earth != null) {
      earth.angle[2] += 0.1f
    }

  }

}
