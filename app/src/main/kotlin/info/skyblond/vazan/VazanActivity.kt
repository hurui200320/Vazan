package info.skyblond.vazan

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.setPadding
import java.util.concurrent.atomic.AtomicBoolean

abstract class VazanActivity : ComponentActivity() {
    protected abstract val permissionExplanation: Map<String, String>

    private val pendingPermissionFlag = AtomicBoolean(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        pendingPermissionFlag.set(false)
        permissions.forEach { (permission, isGranted) ->
            if (!isGranted) {
                AlertDialog.Builder(this)
                    .setTitle("Failed to grant permission")
                    .setMessage(
                        "Permission: $permission is not granted.\n" +
                                "This permission is required for ${permissionExplanation[permission]}."
                    )
                    .setCancelable(false)
                    .setNeutralButton("Fine") { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                        finish()
                    }
                    .create()
                    .show()
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ensurePermissions(permissionExplanation.keys.toList())
    }

    private fun ensurePermissions(permissions: List<String>) {
        pendingPermissionFlag.set(true)
        val array = permissions
            .filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
            .onEach { require(permissionExplanation.containsKey(it)) { "Unexplainable permission $it" } }
            .toTypedArray()
        requestPermissionLauncher.launch(array)
    }

    private val scale: Float
        get() = resources.displayMetrics.density

    protected fun Int.dpToPx(): Int = (this * scale + 0.5f).toInt()

    protected fun createSingleLineEditText(): EditText = EditText(this)
        .apply {
            setPadding(20.dpToPx())
            isSingleLine = true
            isFocusedByDefault = true
        }

    protected fun AlertDialog.Builder.setCancelButton(title: String = "Cancel"): AlertDialog.Builder =
        this.apply {
            setNegativeButton(title) { dialog, _ -> dialog.cancel() }
        }

    protected fun showToast(content: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(this, content, duration).show()
    }

}