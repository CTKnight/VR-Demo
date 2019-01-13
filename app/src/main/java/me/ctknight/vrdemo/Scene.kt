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

package me.ctknight.vrdemo

import android.opengl.Matrix
import android.util.LongSparseArray
import java.util.concurrent.atomic.AtomicLong

class Scene {

  val lightPos = floatArrayOf(0f, 3f, 0f)
  private val counter = AtomicLong(1)
  val viewPosition = floatArrayOf(0f, 1.7f, 2f)
  private val viewPositionMatrixArray = FloatArray(16)
  val viewPositionMatrix: FloatArray
    get() {
      Matrix.setIdentityM(viewPositionMatrixArray, 0)
      // inverse the translation
      Matrix.translateM(viewPositionMatrixArray, 0, -viewPosition[0], -viewPosition[1], -viewPosition[2])
      return viewPositionMatrixArray
    }
  val objMap = LongSparseArray<ModelData>()

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
