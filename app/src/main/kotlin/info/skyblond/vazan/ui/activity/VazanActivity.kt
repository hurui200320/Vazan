package info.skyblond.vazan.ui.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.ui.intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentLinkedQueue

@AndroidEntryPoint
abstract class VazanActivity : ComponentActivity() {
    protected abstract val permissionExplanation: Map<String, String>

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensurePermissions(permissionExplanation.keys.toList())
    }

    private fun ensurePermissions(permissions: List<String>) {
        val array = permissions
            .filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
            .onEach { require(permissionExplanation.containsKey(it)) { "Unexplainable permission $it" } }
            .toTypedArray()
        if (array.isNotEmpty())
            requestPermissionLauncher.launch(array)
    }

    private val scannerCallbackQueue = ConcurrentLinkedQueue<Pair<(String) -> Unit, () -> Unit>>()

    private val scannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val (onSuccess, onFailed) = scannerCallbackQueue.poll() ?: error("Missing callback")
            val barcode = it.data?.getStringExtra("barcode")
            if (it.resultCode == RESULT_OK && barcode != null)
                onSuccess(barcode)
            else onFailed()
        }

    protected fun scanBarcode(onSuccess: (String) -> Unit, onFailed: () -> Unit) =
        synchronized(scannerCallbackQueue) {
            scannerCallbackQueue.offer(onSuccess to onFailed)
            scannerLauncher.launch(intent(ScannerActivity::class))
        }

    private val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME)

    /**
     * @param toneType [ToneGenerator]
     * */
    protected fun playTone(toneType: Int) = lifecycleScope.launch {
        withContext(Dispatchers.Main) {
            toneGen.startTone(toneType)
        }
    }

    protected fun playToneOk() = playTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY_ONE_SHOT)
    protected fun playToneErr() = playTone(ToneGenerator.TONE_CDMA_PIP)

}