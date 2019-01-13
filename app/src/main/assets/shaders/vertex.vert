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
uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uProjectionMatrix;

attribute vec4 aPosition;
attribute vec3 aNormal;
attribute vec2 aTextureCoord;

varying vec4 vPosition;
varying vec3 vNormal;
varying vec2 vTextureCoord;
varying vec3 vFragPosition;

void main() {
  mat4 MVMatrix = uViewMatrix * uModelMatrix;
  vPosition = aPosition;
  vPosition.w  = 1.f;
  vFragPosition = vec3(uModelMatrix * vPosition);
  vNormal = (MVMatrix * vec4(aNormal, 1.0)).xyz;
  vTextureCoord = aTextureCoord;
  gl_Position = uProjectionMatrix * MVMatrix * vPosition;
}