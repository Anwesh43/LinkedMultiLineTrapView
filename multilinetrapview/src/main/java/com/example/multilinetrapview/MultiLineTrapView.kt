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
val lines : Int = 4
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20
val sizeFactor : Float = 4.9f
val trapFactor : Float = 8.7f
val strokeFactor : Float = 90f
val parts : Int = 4
val scGap : Float = 0.02f / parts

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawMultiLineTrap(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    val size : Float = Math.min(w, h) / sizeFactor
    val trapSize : Float = Math.min(w, h) / trapFactor
    val gap : Float = (2 * size) / (lines - 1)
    save()
    translate(w / 2, h)
    for (j in 0..1) {
        save()
        scale(1f - 2 * j, 1f)
        translate(-size, 0f)
        drawLine(0f, 0f, trapSize * sf1, -trapSize * sf1, paint)
        restore()
    }
    save()
    translate(-size, -trapSize)
    drawLine(0f, 0f, 2 * size * sf2, 0f, paint)
    for (j in 0..(lines - 1)) {
        save()
        translate(gap * j, 0f)
        drawLine(0f, 0f, 0f, -size * sf3, paint)
        restore()
    }
    restore()
    restore()
}

fun Canvas.drawMLTNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawMultiLineTrap(scale, w, h, paint)
}

class MultiLineTrapView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }
}