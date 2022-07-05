package com.example.cally

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout

//앱 로고 애니메이션 실행 액티비티: 최초 앱 실행시, 로그아웃시
class SplashActivity : Activity() {
    //애니메이션,이미지파일 선언
    var anim_ball: Animation? = null
    var constraintLayout: ConstraintLayout? = null
    var iv: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        constraintLayout = findViewById(R.id.constraintLayout)
        iv = findViewById(R.id.iv)

        //저장한 애니매이션 효과 불러오기
        anim_ball = AnimationUtils.loadAnimation(this, R.anim.anim_splash_ball)
        this.anim_ball?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                this@SplashActivity.finish()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        iv?.startAnimation(anim_ball)

    }
}