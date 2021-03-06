package com.example.experiment


import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.experiment.databinding.ActivityMainBinding
import com.example.experiment.tablayout.SecondActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.lang.Math.ceil

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var job: Job

    private var bannerPosition = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val list: ArrayList<DataPage> = ArrayList<DataPage>().let {
            it.apply {
                add(DataPage(android.R.color.holo_red_light, "1 Page"))
                add(DataPage(android.R.color.holo_orange_dark, "2 Page"))
                add(DataPage(android.R.color.holo_green_dark, "3 Page"))
                add(DataPage(android.R.color.holo_blue_light, "4 Page"))
                add(DataPage(android.R.color.holo_blue_bright, "5 Page"))
                add(DataPage(android.R.color.black, "6 Page"))
            }
        }
        bannerPosition = Int.MAX_VALUE / 2 - ceil(list.size.toDouble() / 2).toInt()

        binding.viewPager.setCurrentItem(bannerPosition, false)

        with(binding) {
            viewPager.adapter = ViewPagerAdapter(list)
            viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            txtCurrentBanner.text = getString(R.string.viewpager2_banner, 1, list.size)
        }


        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            //???????????? ????????? ????????? position ??????
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bannerPosition = position

                binding.txtCurrentBanner.text = getString(
                    R.string.viewpager2_banner,
                    (bannerPosition % list.size) + 1,
                    list.size
                )
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                when (state) {
                    ViewPager2.SCROLL_STATE_IDLE -> {
                        if (!job.isActive) scrollJobCreate()
                    }  // ???????????????

                    ViewPager2.SCROLL_STATE_DRAGGING -> job.cancel() //????????? ??? ???

                    ViewPager2.SCROLL_STATE_SETTLING -> {} // ???????????? ?????? ????????? ?????? ???
                }
            }
        })
        binding.buttonToViewpager.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }

    private fun scrollJobCreate() {
        job = lifecycleScope.launchWhenResumed {
            delay(1500)
            binding.viewPager.setCurrentItemWithDuration(++bannerPosition, 2500)
        }
    }

    fun ViewPager2.setCurrentItemWithDuration(
        item: Int, duration: Long,
        interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
        pagePxWidth: Int = width // ViewPager2 View ??? getWidth()?????? ????????? ?????????
    ) {
        val pxToDrag: Int = pagePxWidth * (item - currentItem)
        val animator = ValueAnimator.ofInt(0, pxToDrag)
        var previousValue = 0

        animator.addUpdateListener { valueAnimator ->
            val currentValue = valueAnimator.animatedValue as Int
            val currentPxToDrag = (currentValue - previousValue).toFloat()
            fakeDragBy(-currentPxToDrag)
            previousValue = currentValue
        }

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) { beginFakeDrag() }
            override fun onAnimationEnd(animation: Animator?) { endFakeDrag() }
            override fun onAnimationCancel(animation: Animator?) { /* Ignored */ }
            override fun onAnimationRepeat(animation: Animator?) { /* Ignored */ }
        })

        animator.interpolator = interpolator
        animator.duration = duration
        animator.start()
    }


    override fun onResume() {
        super.onResume()
        scrollJobCreate()
    }

    override fun onPause() {
        super.onPause()
        job.cancel()

    }
}
