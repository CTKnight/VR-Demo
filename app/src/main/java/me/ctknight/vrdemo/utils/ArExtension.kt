package me.ctknight.vrdemo.utils

import com.google.ar.core.Pose

fun Pose.toViewPoseTranslation(): Pose = inverse().extractTranslation()
