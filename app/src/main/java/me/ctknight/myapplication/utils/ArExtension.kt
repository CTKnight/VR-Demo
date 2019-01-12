package me.ctknight.myapplication.utils

import com.google.ar.core.Pose

fun Pose.toViewPoseTranslation(): Pose = inverse().extractTranslation()
