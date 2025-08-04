package com.aaloke.feetracker

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManager(private val context: Context) {

    private var rewardedAd: RewardedAd? = null

    // This is the Ad Unit ID you got from the AdMob dashboard
    // IMPORTANT: Replace this with your real Ad Unit ID before publishing.
    private val adUnitId = "ca-app-pub-3940256099942544/5224354917" // Google's Test Ad Unit ID

    init {
        loadRewardedAd()
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
            }
        })
    }

    fun showRewardedAd(activity: Activity, onAdEarned: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Ad was dismissed. Pre-load the next one.
                    loadRewardedAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Ad failed to show.
                    rewardedAd = null
                }
            }

            rewardedAd?.show(activity) {
                // User finished the ad and earned the reward.
                onAdEarned()
            }
        } else {
            Toast.makeText(context, "Ad not ready yet. Please try again.", Toast.LENGTH_SHORT).show()
            // Try to load another ad
            loadRewardedAd()
        }
    }
}