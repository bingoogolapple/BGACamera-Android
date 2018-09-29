package cn.bingoogolapple.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : Activity(), EasyPermissions.PermissionCallbacks, View.OnClickListener {
    private lateinit var mPreViewContainerFl: FrameLayout
    private lateinit var mPreview: CameraPreview
    private lateinit var mPreviewIv: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPreViewContainerFl = findViewById(R.id.fl_main_preview_container)

        mPreview = CameraPreview(this)
        mPreViewContainerFl.addView(mPreview)

        mPreviewIv = findViewById(R.id.iv_main_preview)

        findViewById<Button>(R.id.btn_main_settings).setOnClickListener(this)
        findViewById<Button>(R.id.btn_main_take_picture).setOnClickListener(this)
        findViewById<Button>(R.id.btn_main_capture_video).setOnClickListener(this)
        findViewById<Button>(R.id.btn_main_study).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_main_settings -> {
                initCamera()
                fragmentManager.beginTransaction().replace(R.id.fl_main_preview_container,
                        SettingsFragment()).addToBackStack(null).commit()
            }
            R.id.btn_main_take_picture -> mPreview.takePicture(mPreviewIv)
            R.id.btn_main_capture_video -> {
                if (mPreview.isRecording()) {
                    mPreview.stopRecording(mPreviewIv)
                    findViewById<Button>(R.id.btn_main_capture_video).text = "录像"
                } else if (mPreview.startRecording()) {
                    findViewById<Button>(R.id.btn_main_capture_video).text = "停止"
                }
            }
            R.id.btn_main_study -> startActivity(Intent(this, StudyActivity::class.java))
        }
    }

    private fun initCamera() {
        SettingsFragment.passCamera(mPreview.getCamera())
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        SettingsFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this))
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this))
    }

    override fun onStart() {
        super.onStart()
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        mPreview.startPreview()
    }

    override fun onPause() {
        mPreview.stopPreview()
        super.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private fun requestPermissions() {
        val perms = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!EasyPermissions.hasPermissions(this, *perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和散光灯的权限", REQUEST_CODE_QRCODE_PERMISSIONS, *perms)
        }
    }

    companion object {
        const val REQUEST_CODE_QRCODE_PERMISSIONS = 1
    }
}
