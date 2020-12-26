package com.example.multilinetrapview

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas
import android.app.Activity

val colors : Array<Int> = arrayOf(
    "#F44336",
    "#4CAF50",
    "#3F51B5",
    "#009688",
    "#795548"
).map {
    Color.parseColor(it)
}.toTypedArray()
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20
val sizeFactor : Float = 4.9f
val trapFactor : Float = 8.7f
val strokeFactor : Float = 90f
val parts : Int = 4
val scGap : Float = 0.02f / parts
