package cn.bingoogolapple.camera

import android.content.SharedPreferences
import android.hardware.Camera
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceGroup

/**
 * 作者:王浩
 * 创建时间:2018/9/28
 * 描述:
 */
class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        mParameters?.let {
            loadSupportedPreviewSize(it)
            loadSupportedPictureSize(it)
            loadSupportedVideoSize(it)
            loadSupportedFlashMode(it)
            loadSupportedFocusMode(it)
            loadSupportedWhiteBalance(it)
            loadSupportedSceneMode(it)
            loadSupportedExposeCompensation(it)
        }

        initPreferenceSummary(preferenceScreen)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            KEY_PREF_PREV_SIZE -> setPreviewSize(sharedPreferences.getString(key, ""))
            KEY_PREF_PIC_SIZE -> setPictureSize(sharedPreferences.getString(key, ""))
            KEY_PREF_FOCUS_MODE -> setFocusMode(sharedPreferences.getString(key, ""))
            KEY_PREF_FLASH_MODE -> setFlashMode(sharedPreferences.getString(key, ""))
            KEY_PREF_WHITE_BALANCE -> setWhiteBalance(sharedPreferences.getString(key, ""))
            KEY_PREF_SCENE_MODE -> setSceneMode(sharedPreferences.getString(key, ""))
            KEY_PREF_EXPOS_COMP -> setExposComp(sharedPreferences.getString(key, ""))
            KEY_PREF_JPEG_QUALITY -> setJpegQuality(sharedPreferences.getString(key, ""))
            KEY_PREF_GPS_DATA -> setGpsData(sharedPreferences.getBoolean(key, false))
        }
        mCamera?.let {
            it.stopPreview()
            it.parameters = mParameters
            it.startPreview()
        }

        updatePreferenceSummary(findPreference(key))
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun loadSupportedPreviewSize(parameters: Camera.Parameters) {
        cameraSizeListToListPreference(parameters.supportedPreviewSizes, KEY_PREF_PREV_SIZE)
    }

    private fun loadSupportedPictureSize(parameters: Camera.Parameters) {
        cameraSizeListToListPreference(parameters.supportedPictureSizes, KEY_PREF_PIC_SIZE)
    }

    private fun loadSupportedVideoSize(parameters: Camera.Parameters) {
        cameraSizeListToListPreference(parameters.supportedVideoSizes, KEY_PREF_VIDEO_SIZE)
    }

    private fun loadSupportedFlashMode(parameters: Camera.Parameters) {
        stringListToListPreference(parameters.supportedFlashModes, KEY_PREF_FLASH_MODE)
    }

    private fun loadSupportedFocusMode(parameters: Camera.Parameters) {
        stringListToListPreference(parameters.supportedFocusModes, KEY_PREF_FOCUS_MODE)

    }

    private fun loadSupportedWhiteBalance(parameters: Camera.Parameters) {
        stringListToListPreference(parameters.supportedWhiteBalance, KEY_PREF_WHITE_BALANCE)
    }

    private fun loadSupportedSceneMode(parameters: Camera.Parameters) {
        stringListToListPreference(parameters.supportedSceneModes, KEY_PREF_SCENE_MODE)
    }

    private fun loadSupportedExposeCompensation(parameters: Camera.Parameters) {
        val exposComp = arrayListOf<String>()
        for (value in parameters.minExposureCompensation..parameters.maxExposureCompensation) {
            exposComp.add(Integer.toString(value))
        }
        stringListToListPreference(exposComp, KEY_PREF_EXPOS_COMP)
    }

    private fun cameraSizeListToListPreference(list: List<Camera.Size>, key: String) {
        val stringList = arrayListOf<String>()
        for (size in list) {
            stringList.add(size.width.toString() + "x" + size.height)
        }
        stringListToListPreference(stringList, key)
    }

    private fun stringListToListPreference(list: List<String>, key: String) {
        val charSeq = list.toTypedArray<CharSequence>()
        val listPref = preferenceScreen.findPreference(key) as ListPreference
        listPref.entries = charSeq
        listPref.entryValues = charSeq
    }

    companion object {
        private val TAG = SettingsFragment::class.java.simpleName
        private const val KEY_PREF_PREV_SIZE = "preview_size"
        private const val KEY_PREF_PIC_SIZE = "picture_size"
        private const val KEY_PREF_VIDEO_SIZE = "video_size"
        private const val KEY_PREF_FLASH_MODE = "flash_mode"
        private const val KEY_PREF_FOCUS_MODE = "focus_mode"
        private const val KEY_PREF_WHITE_BALANCE = "white_balance"
        private const val KEY_PREF_SCENE_MODE = "scene_mode"
        private const val KEY_PREF_GPS_DATA = "gps_data"
        private const val KEY_PREF_EXPOS_COMP = "exposure_compensation"
        private const val KEY_PREF_JPEG_QUALITY = "jpeg_quality"

        private var mCamera: Camera? = null
        private var mParameters: Camera.Parameters? = null

        fun passCamera(camera: Camera?) {
            mCamera = camera
            mParameters = camera?.parameters
        }

        fun init(sharedPref: SharedPreferences) {
            setPreviewSize(sharedPref.getString(KEY_PREF_PREV_SIZE, "")!!)
            setPictureSize(sharedPref.getString(KEY_PREF_PIC_SIZE, "")!!)
            setFlashMode(sharedPref.getString(KEY_PREF_FLASH_MODE, ""))
            setFocusMode(sharedPref.getString(KEY_PREF_FOCUS_MODE, ""))
            setWhiteBalance(sharedPref.getString(KEY_PREF_WHITE_BALANCE, ""))
            setSceneMode(sharedPref.getString(KEY_PREF_SCENE_MODE, ""))
            setExposComp(sharedPref.getString(KEY_PREF_EXPOS_COMP, ""))
            setJpegQuality(sharedPref.getString(KEY_PREF_JPEG_QUALITY, ""))
            setGpsData(sharedPref.getBoolean(KEY_PREF_GPS_DATA, false))
            mCamera?.let {
                it.stopPreview()
                it.parameters = mParameters
                it.startPreview()
            }
        }

        fun setPreviewSize(value: String) {
            mParameters?.apply {
                val split = value.split("x".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                setPreviewSize(split[0].toInt(), split[1].toInt())
            }
        }

        fun setPictureSize(value: String) {
            mParameters?.apply {
                val split = value.split("x".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                setPictureSize(split[0].toInt(), split[1].toInt())
            }
        }

        fun setFocusMode(value: String?) {
            mParameters?.apply { focusMode = value }
        }

        fun setFlashMode(value: String?) {
            mParameters?.apply { flashMode = value }
        }

        fun setWhiteBalance(value: String?) {
            mParameters?.apply { whiteBalance = value }
        }

        fun setSceneMode(value: String?) {
            mParameters?.apply { sceneMode = value }
        }

        fun setExposComp(value: String?) {
            mParameters?.apply { exposureCompensation = Integer.parseInt(value!!) }
        }

        fun setJpegQuality(value: String?) {
            mParameters?.apply { jpegQuality = Integer.parseInt(value!!) }
        }

        fun setGpsData(value: Boolean) {
            if (!value) {
                mParameters?.apply { removeGpsData() }
            }
        }

        fun setDefault(sharedPreferences: SharedPreferences) {
            val valPreviewSize = sharedPreferences.getString(KEY_PREF_PREV_SIZE, null)
            if (valPreviewSize == null) {
                mParameters?.apply {
                    val editor = sharedPreferences.edit()
                    editor.putString(KEY_PREF_PREV_SIZE, previewSize.width.toString() + "x" + previewSize.height)
                    editor.putString(KEY_PREF_PIC_SIZE, pictureSize.width.toString() + "x" + pictureSize.height)
                    editor.putString(KEY_PREF_VIDEO_SIZE, preferredPreviewSizeForVideo.width.toString() + "x" + preferredPreviewSizeForVideo.height)
                    if (supportedFocusModes.contains("continuous-picture")) {
                        editor.putString(KEY_PREF_FOCUS_MODE, "continuous-picture")
                    } else {
                        editor.putString(KEY_PREF_FOCUS_MODE, "continuous-video")
                    }
                    editor.apply()
                }
            }
        }

        private fun initPreferenceSummary(preference: Preference) {
            if (preference is PreferenceGroup) {
                for (preferenceIndex in 0 until preference.preferenceCount) {
                    initPreferenceSummary(preference.getPreference(preferenceIndex))
                }
            } else {
                updatePreferenceSummary(preference)
            }
        }

        private fun updatePreferenceSummary(preference: Preference) {
            if (preference is ListPreference) {
                preference.setSummary(preference.entry)
            }
        }
    }
}