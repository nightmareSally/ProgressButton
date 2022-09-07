package com.sally.progressbuttonsample

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import com.sally.progressbuttonsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private var animator: ValueAnimator? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mBinding.sbProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                if (!mBinding.cbAuto.isChecked) {
                    mBinding.pbProgress.setProgress(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        mBinding.cbAuto.setOnCheckedChangeListener { compoundButton, checked ->
            if (checked) {
                startAni()
            } else {
                stopAni()
            }
        }
    }

    fun startAni() {
        animator?.pause() ?: run {
            animator = ValueAnimator.ofInt(mBinding.pbProgress.min(), mBinding.pbProgress.max())
            animator?.addUpdateListener {
                val value = it.animatedValue as Int
                mBinding.pbProgress.setProgress(value)
            }
            animator?.repeatMode = ObjectAnimator.RESTART
            animator?.repeatCount = ObjectAnimator.INFINITE
            animator?.duration = 2000L
            animator?.start()
        }
    }

    fun stopAni() {
        animator?.pause()
    }
}