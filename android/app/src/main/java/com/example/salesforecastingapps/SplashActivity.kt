package com.example.salesforecastingapps
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.salesforecastingapps.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())

    private val SPLASH_DURATION    = 2800L
    private val ANIM_LOGO_DURATION = 700L
    private val ANIM_DOTS_DURATION = 400L

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)

        // ② Full screen — cara modern, tidak deprecated
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ③ Nonaktifkan tombol back — cara modern, tidak deprecated
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { /* no-op */ }
        })

        startAnimations()
        navigateAfterDelay()
    }

    // ─────────────────────────────────────────────────────────
    // Animasi masuk: logo scale+fade → dots fade in
    // ─────────────────────────────────────────────────────────
    private fun startAnimations() {
        val logoScaleX = ObjectAnimator.ofFloat(binding.centerGroup, View.SCALE_X, 0.5f, 1f)
        val logoScaleY = ObjectAnimator.ofFloat(binding.centerGroup, View.SCALE_Y, 0.5f, 1f)
        val logoFade   = ObjectAnimator.ofFloat(binding.centerGroup, View.ALPHA,   0f,  1f)

        val logoAnimSet = AnimatorSet().apply {
            playTogether(logoScaleX, logoScaleY, logoFade)
            duration    = ANIM_LOGO_DURATION
            interpolator = OvershootInterpolator(1.2f)
        }

        val dotsFade    = ObjectAnimator.ofFloat(binding.loadingDots, View.ALPHA, 0f, 1f)
        val loadingFade = ObjectAnimator.ofFloat(binding.tvLoading,   View.ALPHA, 0f, 1f)

        val dotsAnimSet = AnimatorSet().apply {
            playTogether(dotsFade, loadingFade)
            duration   = ANIM_DOTS_DURATION
            startDelay = ANIM_LOGO_DURATION + 300L
        }

        AnimatorSet().apply {
            playTogether(logoAnimSet, dotsAnimSet)
            start()
        }

        startDotsAnimation()
    }

    private fun startDotsAnimation() {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3)
        var index = 0

        val dotRunnable = object : Runnable {
            override fun run() {
                dots.forEach { it.alpha = 0.35f }
                val current = dots[index % dots.size]
                current.animate()
                    .alpha(1f).scaleX(1.3f).scaleY(1.3f)
                    .setDuration(200)
                    .withEndAction {
                        current.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(150).start()
                    }.start()
                index++
                handler.postDelayed(this, 350)
            }
        }

        handler.postDelayed({ handler.post(dotRunnable) }, ANIM_LOGO_DURATION + 400L)
    }

    // ─────────────────────────────────────────────────────────
    // Navigasi ke MainActivity setelah SPLASH_DURATION
    // ─────────────────────────────────────────────────────────
    private fun navigateAfterDelay() {
        handler.postDelayed({
            binding.root.animate()
                .alpha(0f)
                .setDuration(400)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { goToMain() }
                .start()
        }, SPLASH_DURATION)
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(0, 0)   // seamless, tanpa animasi transisi
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
