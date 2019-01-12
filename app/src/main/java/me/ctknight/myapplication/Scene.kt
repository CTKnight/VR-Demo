package me.ctknight.myapplication

import android.util.LongSparseArray
import java.util.concurrent.atomic.AtomicLong

class Scene {

  val lightPos = floatArrayOf(0f, 3f, -2f)
  private val counter = AtomicLong(1)
  private val initPosition = FloatArray(3)
  val objMap = LongSparseArray<ModelData>()

  private var left = false

  fun addObj(modelData: ModelData): Long {
    val id = counter.getAndIncrement()
    objMap.append(id, modelData)
    modelData.id = id
    return id
  }

  fun removeObj(id: Long) {
    objMap.remove(id)
  }

  fun getObj(id: Long): ModelData? {
    return objMap[id]
  }

  // update object coord
  fun updateObjMotion() {

  }

}
