package info.skyblond.vazan

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
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

    protected fun ensurePermissions(permissions: List<String>) {
        pendingPermissionFlag.set(true)
        val array = permissions
            .filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
            .onEach { require(permissionExplanation.containsKey(it)) { "Unexplainable permission $it" } }
            .toTypedArray()
        requestPermissionLauncher.launch(array)
    }

    protected fun pendingPermission(): Boolean = pendingPermissionFlag.get()
}