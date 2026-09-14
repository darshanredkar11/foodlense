package com.foodlense.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foodlense.android.camera.CameraAnalyzer
import com.foodlense.android.camera.CameraPreview
import com.foodlense.android.scan.MlKitBarcodeScanner
import com.foodlense.android.scan.MlKitTextScanner
import com.foodlense.shared.domain.scan.ScanPayload
import com.foodlense.shared.domain.scan.ScanResult

@Composable
fun FoodLenseApp() {
    var scanning by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<ScanResult?>(null) }

    if (scanning) {
        val analyzer = remember {
            CameraAnalyzer(
                barcodeScanner = MlKitBarcodeScanner(),
                textScanner = MlKitTextScanner(),
                onResult = { result -> scanResult = result },
            )
        }

        DisposableEffect(Unit) {
            onDispose { analyzer.close() }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(analyzer = analyzer)
            Button(
                onClick = { scanning = false },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
            ) {
                Text("Close scanner")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("FoodLense")
        Text("Ready to scan")
        scanResult?.let { result ->
            Text(scanSummary(result))
        }
        Button(onClick = {
            scanResult = null
            scanning = true
        }) {
            Text("Scan food")
        }
    }
}

private fun scanSummary(result: ScanResult): String = when (val payload = result.payload) {
    is ScanPayload.Barcode -> "Found barcode: ${payload.rawValue}"
    is ScanPayload.Text -> "Found text: ${payload.value.take(120)}"
}
