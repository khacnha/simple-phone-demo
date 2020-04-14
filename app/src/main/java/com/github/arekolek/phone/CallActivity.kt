package com.github.arekolek.phone

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telecom.Call
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_call.*
import java.util.concurrent.TimeUnit

class CallActivity : AppCompatActivity() {

    private val disposables = CompositeDisposable()

    private lateinit var number: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        number = intent.data.schemeSpecificPart
    }

    override fun onStart() {
        super.onStart()

        answer.setOnClickListener {
            OngoingCall.answer()
        }

        hangup.setOnClickListener {
            OngoingCall.hangup()
        }

        OngoingCall.state
            .subscribe(::updateUi)
            .addTo(disposables)

        OngoingCall.state
            .filter { it == Call.STATE_DISCONNECTED }
            .delay(1, TimeUnit.SECONDS)
            .firstElement()
            .subscribe { finish() }
            .addTo(disposables)
    }

    @SuppressLint("SetTextI18n")
    private fun updateUi(state: Int) {

        closeBanner.setOnClickListener {
            popupBanner.visibility = View.GONE
        }
        Glide.with(this)
            .load("https://previews.123rf.com/images/garagestock/garagestock1701/garagestock170124577/69379761-technology-concept-banner-demo.jpg")
//            .listener(object: RequestListener<Drawable> {
//                override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {
//                    print("IMG ERR")
//                    return false
//                }
//                override fun onResourceReady(
//                    resource: Drawable?,
//                    model: Any?,
//                    target: com.bumptech.glide.request.target.Target<Drawable>?,
//                    dataSource: com.bumptech.glide.load.DataSource?,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    print("IMG OK")
//                    popupBanner.visibility = View.VISIBLE
//                    return true
//                }
//            })
            .into(banner)
        popupBanner.isVisible = state == Call.STATE_RINGING

        callInfo.text = "${state.asString().toLowerCase().capitalize()}\n$number"

        answer.isVisible = state == Call.STATE_RINGING
        hangup.isVisible = state in listOf(
            Call.STATE_DIALING,
            Call.STATE_RINGING,
            Call.STATE_ACTIVE
        )
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    companion object {
        fun start(context: Context, call: Call) {
            Intent(context, CallActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(call.details.handle)
                .let(context::startActivity)
        }
    }
}
