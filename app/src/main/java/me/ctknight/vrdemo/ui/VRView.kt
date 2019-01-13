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
