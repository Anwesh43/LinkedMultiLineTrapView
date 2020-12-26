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

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class MLTNode(var i : Int, val state : State = State()) {

        private var next : MLTNode? = null
        private var prev : MLTNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = MLTNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawMLTNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : MLTNode {
            var curr : MLTNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class MultiLineTrap(var i : Int) {

        private var curr : MLTNode = MLTNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : MultiLineTrapView) {

        private val mlt : MultiLineTrap = MultiLineTrap(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            mlt.draw(canvas, paint)
            animator.animate {
                mlt.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            mlt.startUpdating {
                animator.start()
            }
        }
    }
}