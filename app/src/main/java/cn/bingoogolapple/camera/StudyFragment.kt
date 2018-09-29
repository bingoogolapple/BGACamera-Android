package cn.bingoogolapple.camera

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.ListPreference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.util.Log


/**
 * 作者:王浩
 * 创建时间:2018/9/28
 * 描述:
 */
class StudyFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.study_preference_fragment)

        val oneOne: ListPreference = findPreference("one_one") as ListPreference
        oneOne.setOnPreferenceChangeListener { preference, newValue ->
            Log.d("BGA", "newValue is " + newValue)
            return@setOnPreferenceChangeListener true
        }
        val oneTwo: SwitchPreference = findPreference("one_two") as SwitchPreference
        oneTwo.setOnPreferenceClickListener {
            Log.d("BGA", "点击了 " + it.title)
            return@setOnPreferenceClickListener false
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key.endsWith("two")) {
            Log.d("BGA", key + " is " + sharedPreferences.getBoolean(key, false))
        } else {
            Log.d("BGA", key + " is " + sharedPreferences.getString(key, ""))
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    companion object {
    }
}