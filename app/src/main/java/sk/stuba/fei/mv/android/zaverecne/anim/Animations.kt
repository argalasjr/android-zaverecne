package sk.stuba.fei.mv.android.zaverecne.anim

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import sk.stuba.fei.mv.android.zaverecne.R

object Animations {

    fun animateViewFadeOut(view: View) {
        val fadeOut = AnimationUtils.loadAnimation(view.context, R.anim.scale_down)
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                view.visibility = View.GONE
            }

            override fun onAnimationEnd(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
        })
        view.startAnimation(fadeOut)
    }

    fun animateViewFadeIn(view: View) {
        val fadeIn = AnimationUtils.loadAnimation(view.context, R.anim.scale_up)
        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                view.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
        })
        view.startAnimation(fadeIn)
    }
}