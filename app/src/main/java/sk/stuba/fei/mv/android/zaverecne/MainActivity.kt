package sk.stuba.fei.mv.android.zaverecne

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.activity_video_preview.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //TEST CRASHLYTICS
//        FirebaseCrashlytics.getInstance().log("App Started")
//        val crashButton = Button(this)
//        crashButton.text = "Crash!"
//        crashButton.setOnClickListener {
//            throw RuntimeException("Test Crash") // Force a crash
//        }
//
//        addContentView(crashButton, ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT))
   }

}