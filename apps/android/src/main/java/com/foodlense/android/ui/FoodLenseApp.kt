package com.foodlense.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.foodlense.android.ai.LocalFoodAnswerProvider
import com.foodlense.android.camera.CameraAnalyzer
import com.foodlense.android.camera.CameraPreview
import com.foodlense.android.scan.MlKitBarcodeScanner
import com.foodlense.android.scan.MlKitTextScanner
import com.foodlense.shared.domain.scan.ScanPayload
import com.foodlense.shared.domain.scan.ScanResult
import kotlinx.coroutines.delay

private enum class Screen { HOME, CAMERA, RESULT }
private data class ChatMessage(val fromUser: Boolean, val text: String)

@Composable
fun FoodLenseApp() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var scanResult by remember { mutableStateOf<ScanResult?>(null) }

    when (screen) {
        Screen.HOME -> HomeScreen(onScan = { screen = Screen.CAMERA })
        Screen.CAMERA -> IngredientCamera(
            onResult = { scanResult = it; screen = Screen.RESULT },
            onClose = { screen = Screen.HOME },
        )
        Screen.RESULT -> ResultScreen(result = scanResult, onScanAgain = { screen = Screen.CAMERA })
    }
}

@Composable
private fun HomeScreen(onScan: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 22.dp, vertical = 18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) { Text("F", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("FoodLense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Food, decoded.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text("PRIVATE", modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(Modifier.weight(1f))
            Text("Know what's", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text("really in your food.", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(14.dp))
            Text(
                "Point your camera at any packaged food. I'll read the label and tell you what actually matters — without the fearmongering.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(19.dp)) {
                    Text("Your quick food check", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    InsightRow("01", "Ingredients decoded")
                    InsightRow("02", "Things worth knowing highlighted")
                    InsightRow("03", "A simple answer you can actually use")
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onScan,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) { Text("📷  Scan a food", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(9.dp))
            Text("Nothing is uploaded in this demo.", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InsightRow(number: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 5.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.background, modifier = Modifier.size(28.dp)) {
            Box(contentAlignment = Alignment.Center) { Text(number, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
        }
        Spacer(Modifier.width(11.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun IngredientCamera(onResult: (ScanResult) -> Unit, onClose: () -> Unit) {
    val analyzer = remember {
        CameraAnalyzer(MlKitBarcodeScanner(), MlKitTextScanner(), onResult)
    }
    DisposableEffect(Unit) { onDispose { analyzer.close() } }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        CameraPreview(analyzer = analyzer)

        // A framing guide is intentionally persistent: users should know exactly
        // where to place the ingredients panel before OCR is allowed to win.
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                Modifier.fillMaxWidth(.86f).fillMaxHeight(.46f)
                    .border(2.dp, Color.White.copy(alpha = .9f), RoundedCornerShape(24.dp)),
            )
        }

        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color.Black.copy(alpha = .48f)) {
                    IconButton(onClick = onClose) { Text("×", color = Color.White, style = MaterialTheme.typography.headlineMedium) }
                }
                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(50), color = Color.Black.copy(alpha = .55f)) {
                    Text("INGREDIENTS", color = Color.White, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(28.dp))
            Surface(shape = RoundedCornerShape(20.dp), color = Color.Black.copy(alpha = .58f)) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 13.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Keep the ingredients inside the frame", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(3.dp))
                    Text("Move closer • straighten the label • hold steady", color = Color.White.copy(alpha = .84f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.weight(1f))
            Text("Waiting for a clear, stable read…", color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(modifier = Modifier.width(150.dp).clip(CircleShape), color = Color.White, trackColor = Color.White.copy(alpha = .25f))
            Spacer(Modifier.height(18.dp))
            Box(Modifier.size(82.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Box(Modifier.size(68.dp).clip(CircleShape).background(Color.Black.copy(alpha = .08f)))
            }
            Spacer(Modifier.height(8.dp))
            Text("I'll wait for 3 consistent reads", color = Color.White.copy(alpha = .78f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ResultScreen(result: ScanResult?, onScanAgain: () -> Unit) {
    val detectedText = remember(result) { extractText(result) }
    val analysis = remember(detectedText) { analyzeIngredients(detectedText) }
    val answerProvider = remember { LocalFoodAnswerProvider() }
    val messages = remember(analysis) { mutableStateListOf(ChatMessage(false, analysis.opening)) }
    var question by remember { mutableStateOf("") }
    var thinking by remember { mutableStateOf(false) }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) { Text("F", color = Color.White, fontWeight = FontWeight.Black) }
                Spacer(Modifier.width(10.dp))
                Text("FoodLense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onScanAgain) { Text("Scan again") }
            }

            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text("Your food, decoded.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Here's what stood out from the label.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item { IngredientCard(analysis) }
                item {
                    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text("💡  Tip: ask me about an ingredient, or ask whether you'd eat this every day.", modifier = Modifier.padding(13.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                item { Text("Ask me anything", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                items(messages) { ChatBubble(it) }
                if (thinking) item { ChatBubble(ChatMessage(false, "Let me check that against the label… 👀")) }
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.Bottom) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Is this okay to eat?") },
                    shape = RoundedCornerShape(22.dp),
                    maxLines = 3,
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val q = question.trim()
                        if (q.isEmpty() || thinking) return@IconButton
                        messages.add(ChatMessage(true, q)); question = ""; thinking = true
                    },
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                ) { Text("↑", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            }
        }
    }

    LaunchedEffect(thinking) {
        if (thinking) {
            delay(500)
            val latestQuestion = messages.lastOrNull { it.fromUser }?.text.orEmpty()
            messages.add(ChatMessage(false, answerProvider.answer(detectedText, latestQuestion)))
            thinking = false
        }
    }
}

@Composable
private fun IngredientCard(analysis: IngredientAnalysis) {
    Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(19.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(46.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge) }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(analysis.verdict, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(if (analysis.isPartial) "Only a partial label read" else "Based on a stable label read", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(13.dp))
            Text(analysis.summary, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            analysis.flags.forEach { flag ->
                Surface(shape = RoundedCornerShape(18.dp), color = flag.background, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(flag.title, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(3.dp))
                        Text(flag.detail, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("LABEL TEXT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(analysis.ingredients, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 5)
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (message.fromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(message.text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = if (message.fromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class Flag(val title: String, val detail: String, val background: Color)
private data class IngredientAnalysis(
    val verdict: String,
    val summary: String,
    val opening: String,
    val ingredients: String,
    val flags: List<Flag>,
    val isPartial: Boolean,
)

private fun extractText(result: ScanResult?): String = when (val payload = result?.payload) {
    is ScanPayload.Text -> payload.value
    is ScanPayload.Barcode -> "Product barcode ${payload.rawValue}"
    null -> ""
}

private fun analyzeIngredients(text: String): IngredientAnalysis {
    val normalized = text.lowercase()
    val flags = mutableListOf<Flag>()
    if (listOf("e621", "monosodium glutamate", "msg").any(normalized::contains)) flags += Flag("E621 · MSG", "Generally considered safe at normal dietary levels. If you eat this often, the bigger question is the overall food and sodium load.", Color(0xFFFFF1D6))
    if (listOf("palmolein", "palm oil", "hydrogenated", "partially hydrogenated").any(normalized::contains)) flags += Flag("Palm / hydrogenated fat", "Worth knowing rather than panicking about. Frequency and the rest of your diet matter more than one ingredient alone.", Color(0xFFFFE4E1))
    if (listOf("aspartame", "sucralose", "acesulfame", "e951", "e955", "e950").any(normalized::contains)) flags += Flag("Sweeteners detected", "These are regulated food additives, but people can have different preferences or sensitivities. I wouldn't call them automatically harmful.", Color(0xFFE9F4FF))

    val partial = text.trim().length < 20 && !listOf("ingredients", "contains").any(normalized::contains)
    if (flags.isEmpty()) {
        flags += if (partial) {
            Flag("Not enough label detail yet", "I could read only a short piece of the label, so I won't tell you the product is fine. Scan the full ingredients panel for a meaningful check.", Color(0xFFFFF1D6))
        } else {
            Flag("No obvious red flags in this read", "I didn't spot a common additive that I'd immediately call a concern from the text I could read. That still isn't the same as saying the whole product is healthy.", Color(0xFFE8F6EA))
        }
    }
    val verdict = when {
        partial -> "I need a clearer read 📸"
        flags.any { it.title.startsWith("E621") } -> "A few things worth knowing 👀"
        flags.any { it.title.startsWith("Palm") } -> "Worth a closer look 🧐"
        else -> "Looks fairly straightforward 🙂"
    }
    val summary = if (partial) {
        "I got a partial label read, so I'm not going to pretend I can judge the food yet."
    } else {
        "I read a stable label and found ${flags.size} thing${if (flags.size == 1) "" else "s"} worth talking about."
    }
    return IngredientAnalysis(
        verdict = verdict,
        summary = summary,
        opening = "$verdict\n\n$summary Ask me about any ingredient and I'll break it down without the chemistry lecture.",
        ingredients = text.ifBlank { "No readable ingredient text yet." },
        flags = flags,
        isPartial = partial,
    )
}
