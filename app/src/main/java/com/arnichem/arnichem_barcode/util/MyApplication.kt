import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class MyApplication : LifecycleObserver {

    private val TAG = javaClass.simpleName

    // To observe the onCreate state of MainActivity
    // and perform the assigned tasks
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreatePerformTask() {
        // here for demonstration purpose the Log messages are printed in logcat
        // one may perform their own custom tasks
        Log.i(TAG, "I\'m inside Observer of MainActivity ON_CREATE")
    }

    // To observe the onResume state of MainActivity
    // and perform the assigned tasks
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResumePerformTask() {
        // here for demonstration purpose the Log messages are printed in logcat
        // one may perform their own custom tasks
        Log.i(TAG, "I\'m inside Observer of MainActivity ON_RESUME")
    }
}
