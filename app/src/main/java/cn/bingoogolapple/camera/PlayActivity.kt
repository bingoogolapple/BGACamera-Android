package cn.bingoogolapple.camera

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import android.widget.MediaController
import android.widget.RelativeLayout
import android.widget.VideoView


/**
 * 作者:王浩
 * 创建时间:2018/9/29
 * 描述:
 */
class PlayActivity : Activity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val relativeLayout = RelativeLayout(this)
        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
        val uri = intent.data
        if (intent.type == "image/*") {
            val view = ImageView(this)
            view.setImageURI(uri)
            view.layoutParams = layoutParams
            relativeLayout.addView(view)
        } else {
            val mc = MediaController(this)
            val view = VideoView(this)
            mc.setAnchorView(view)
            mc.setMediaPlayer(view)
            view.setMediaController(mc)
            view.setVideoURI(uri)
            view.start()
            view.layoutParams = layoutParams
            relativeLayout.addView(view)
        }
        setContentView(relativeLayout, layoutParams)
    }
}