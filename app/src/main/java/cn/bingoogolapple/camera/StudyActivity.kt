package cn.bingoogolapple.camera

import android.app.Activity
import android.os.Bundle

class StudyActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        fragmentManager.beginTransaction().replace(R.id.fl_study_container, StudyFragment()).commit()
    }
}
