package cn.bingoogolapple.camera

import android.content.Context
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaActionSound
import android.media.MediaRecorder
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * 作者:王浩
 * 创建时间:2018/9/28
 * 描述:
 */
class CameraPreview(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    private var mCamera: Camera? = null
    private var mOutputMediaFileUri: Uri? = null
    private var mOutputMediaFileType: String? = null
    private var mMediaRecorder: MediaRecorder? = null

    init {
        holder.addCallback(this)
    }

    fun getCamera(): Camera? {
        return mCamera
    }

    private fun log(parameters: Camera.Parameters) {
        val log = StringBuilder()
        log.append("\n相机信息如下：")
        log.append("\n    支持的预览尺寸有：\n    ")
        for (previewSize in parameters.supportedPreviewSizes) {
            log.append("${previewSize.width}x${previewSize.height}").append("、")
        }
        log.append("\n支持的拍照尺寸有：\n    ")
        for (pictureSize in parameters.supportedPictureSizes) {
            log.append("${pictureSize.width}x${pictureSize.height}").append("、")
        }
        log.append("\n支持的录像尺寸有：\n    ")
        for (videoPictureSize in parameters.supportedVideoSizes) {
            log.append("${videoPictureSize.width}x${videoPictureSize.height}").append("、")
        }
        log.append("\n支持的对焦模式有：\n    ")
        for (focusMode in parameters.supportedFocusModes) {
            log.append(focusMode).append("、")
        }
        log.append("\n支持的白平衡有：\n    ")
        for (whiteBalance in parameters.supportedWhiteBalance) {
            log.append(whiteBalance).append("、")
        }
        log.append("\n支持的场景模式有：\n    ")
        for (sceneMode in parameters.supportedSceneModes) {
            log.append(sceneMode).append("、")
        }
        log.append("\n支持的闪光灯模式有：\n    ")
        for (flashMode in parameters.supportedFlashModes) {
            log.append(flashMode).append("、")
        }
        log.append("\n曝光补偿范围为：\n    ${parameters.minExposureCompensation}~${parameters.maxExposureCompensation}")
        Log.d(TAG, log.toString())
    }

    private fun openCamera() {
        if (mCamera != null) {
            return
        }

        try {
            mCamera = Camera.open()
        } catch (e: Exception) {
            Log.e(TAG, "相机资源被占用：" + e.message)
        }
    }

    public fun startPreview() {
        openCamera()
        try {
            mCamera?.apply {
                // 告知将预览帧数据交给谁
                setPreviewDisplay(holder)
                // 开始预览
                startPreview()
                log(parameters)
            }
        } catch (e: IOException) {
            Log.e(TAG, "开始预览失败：" + e.message)
        }
    }

    public fun stopPreview() {
        holder.removeCallback(this)
        // 相机是共享资源，使用完后需要释放相机资源
        mCamera?.apply {
            setPreviewCallback(null)
            stopPreview()
            release()
        }
        mCamera = null
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        startPreview()
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        stopPreview()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
    }

    private fun getAppName(): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).applicationInfo.loadLabel(context.packageManager).toString()
        } catch (e: Exception) {
            // 利用系统api getPackageName()得到的包名，这个异常根本不可能发生
            ""
        }

    }

    private fun getOutputMediaFile(type: Int): File? {
        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getAppName())
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "创建媒体文件目录失败，请检查存储权限")
                return null
            }
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val mediaFile: File
        when (type) {
            MEDIA_TYPE_IMAGE -> {
                mediaFile = File(mediaStorageDir.path + File.separator + "IMG_" + timeStamp + ".jpg")
                mOutputMediaFileType = "image/*"
            }
            MEDIA_TYPE_VIDEO -> {
                mediaFile = File(mediaStorageDir.path + File.separator + "VID_" + timeStamp + ".mp4")
                mOutputMediaFileType = "video/*"
            }
            else -> return null
        }
        mOutputMediaFileUri = Uri.fromFile(mediaFile)
        return mediaFile
    }

    fun getOutputMediaFileUri(): Uri? {
        return mOutputMediaFileUri
    }

    fun getOutputMediaFileType(): String? {
        return mOutputMediaFileType
    }

    fun takePicture(previewIv: ImageView) {
        mCamera?.apply {
            takePicture(Camera.ShutterCallback {
                Log.d(TAG, "按下了快门，播放声音")
                MediaActionSound().play(MediaActionSound.SHUTTER_CLICK)
            }, Camera.PictureCallback { data, camera ->
                Log.d(TAG, "原始数据，不知道为什么返回的 data 一直为空")
            }, Camera.PictureCallback { data, camera ->
                Log.d(TAG, "jpeg 数据。主线程回调的 " + data.size)
                // TODO 子线程保存图片文件
                val pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE)
                if (pictureFile == null) {
                    Log.d(TAG, "创建媒体文件失败，请检查存储权限")
                    return@PictureCallback
                }
                try {
                    FileOutputStream(pictureFile).use {
                        it.write(data)
                    }

                    previewIv.setImageURI(mOutputMediaFileUri)

                    camera.startPreview()
                } catch (e: Exception) {
                    Log.d(TAG, "保存图片失败 " + e.message)
                }
            })
        }
    }

    fun startRecording(): Boolean {
        if (prepareVideoRecorder()) {
            mMediaRecorder?.start()
            return true
        } else {
            releaseMediaRecorder()
        }
        return false
    }

    fun stopRecording(previewIv: ImageView) {
        mMediaRecorder?.stop()
        mOutputMediaFileUri?.apply {
            val thumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND)
            previewIv.setImageBitmap(thumbnail)
        }
        releaseMediaRecorder()
    }

    fun isRecording(): Boolean {
        return mMediaRecorder != null
    }

    private fun prepareVideoRecorder(): Boolean {
        openCamera()
        mMediaRecorder = MediaRecorder()

        mCamera?.unlock()
        mMediaRecorder?.apply {

            setCamera(mCamera)

            setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            setVideoSource(MediaRecorder.VideoSource.CAMERA)
            setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val prefVideoSize = prefs.getString("video_size", "")
            if (prefVideoSize.isNotBlank()) {
                val split = prefVideoSize.split("x".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                setVideoSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]))
            }
            setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString())
            setPreviewDisplay(holder.surface)

            try {
                prepare()
            } catch (e: IllegalStateException) {
                Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.message)
                releaseMediaRecorder()
                return false
            } catch (e: IOException) {
                Log.d(TAG, "IOException preparing MediaRecorder: " + e.message)
                releaseMediaRecorder()
                return false
            }
        }
        return true
    }

    private fun releaseMediaRecorder() {
        mMediaRecorder?.apply {
            reset()
            release()
            mMediaRecorder = null
        }
        mCamera?.apply { lock() }
    }

    companion object {
        private val TAG = CameraPreview::class.java.simpleName
        private const val MEDIA_TYPE_IMAGE = 1
        private const val MEDIA_TYPE_VIDEO = 2
    }
}