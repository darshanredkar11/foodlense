package com.foodlense.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
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
            onResult = {
                scanResult = it
                screen = Screen.RESULT
            },
            onClose = { screen = Screen.HOME },
        )
        Screen.RESULT -> ResultScreen(
            result = scanResult,
            onScanAgain = { screen = Screen.CAMERA },
        )
    }
}

@Composable
private fun HomeScreen(onScan: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) { Text("F", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.size(12.dp))
                Text("FoodLense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.weight(1f))
            Text(
                "Know what's\nreally in your food.",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Take a photo of the ingredients.\nI'll read them and explain what matters — simply.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(28.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("What you'll get", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(14.dp))
                    InsightRow("🔎", "Ingredients decoded")
                    InsightRow("⚠️", "Potential concerns called out")
                    InsightRow("💬", "A conversation, not a lecture")
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onScan,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(18.dp),
            ) { Text("📷  Take a photo", style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.height(8.dp))
            Text(
                "Your photo stays on your device for this demo.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun InsightRow(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 5.dp)) {
        Text(icon, modifier = Modifier.widthCompat(28.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun Modifier.widthCompat(width: Int): Modifier = this.then(Modifier.size(width.dp, 24.dp))

@Composable
private fun IngredientCamera(onResult: (ScanResult) -> Unit, onClose: () -> Unit) {
    val analyzer = remember {
        CameraAnalyzer(
            barcodeScanner = MlKitBarcodeScanner(),
            textScanner = MlKitTextScanner(),
            onResult = onResult,
        )
    }
    DisposableEffect(Unit) { onDispose { analyzer.close() } }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        CameraPreview(analyzer = analyzer)
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Text("×", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(50), color = Color.Black.copy(alpha = .55f)) {
                    Text("INGREDIENTS", color = Color.White, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
            Surface(shape = RoundedCornerShape(18.dp), color = Color.Black.copy(alpha = .55f)) {
                Text(
                    "Point at the ingredients list\nand hold steady",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.weight(1f))
            Text("Reading the label…", color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = .95f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(62.dp).clip(CircleShape).background(Color.Black.copy(alpha = .08f)))
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun ResultScreen(result: ScanResult?, onScanAgain: () -> Unit) {
    val detectedText = remember(result) { extractText(result) }
    val analysis = remember(detectedText) { analyzeIngredients(detectedText) }
    val answerProvider = remember { LocalFoodAnswerProvider() }
    val messages = remember(analysis) {
        mutableStateListOf(
            ChatMessage(false, analysis.opening),
        )
    }
    var question by remember { mutableStateOf("") }
    var thinking by remember { mutableStateOf(false) }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("FoodLense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onScanAgain) { Text("Scan again") }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text("Your food, decoded.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Here's what stood out from the label.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item { IngredientCard(analysis) }
                item {
                    Text("Ask me anything", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                items(messages) { message -> ChatBubble(message) }
                if (thinking) item { ChatBubble(ChatMessage(false, "Give me a second… 👀")) }
            }

            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Is this okay to eat?") },
                    shape = RoundedCornerShape(22.dp),
                    maxLines = 3,
                )
                Spacer(Modifier.size(8.dp))
                IconButton(
                    onClick = {
                        val q = question.trim()
                        if (q.isEmpty() || thinking) return@IconButton
                        messages.add(ChatMessage(true, q))
                        question = ""
                        thinking = true
                    },
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                ) { Text("↑", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleLarge) }
            }
        }
    }

    LaunchedEffect(thinking) {
        if (thinking) {
            delay(650)
            val latestQuestion = messages.lastOrNull { it.fromUser }?.text.orEmpty()
            messages.add(ChatMessage(false, answerProvider.answer(detectedText, latestQuestion)))
            thinking = false
        }
    }
}

@Composable
private fun IngredientCard(analysis: IngredientAnalysis) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(20.dp)) {
            Text(analysis.verdict, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(analysis.summary, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            analysis.flags.forEach { flag ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = flag.background,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(flag.title, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(3.dp))
                        Text(flag.detail, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Ingredients read", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(analysis.ingredients, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Text(
                message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = if (message.fromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
)

private fun extractText(result: ScanResult?): String = when (val payload = result?.payload) {
    is ScanPayload.Text -> payload.value
    is ScanPayload.Barcode -> "Product barcode ${payload.rawValue}"
    null -> ""
}

private fun analyzeIngredients(text: String): IngredientAnalysis {
    val normalized = text.lowercase()
    val flags = mutableListOf<Flag>()
    if (listOf("e621", "monosodium glutamate", "msg").any(normalized::contains)) {
        flags += Flag("E621 · MSG", "Generally considered safe at normal dietary levels. If you eat this product often, the bigger question is the overall food and sodium load.", Color(0xFFFFF1D6))
    }
    if (listOf("palmolein", "palm oil", "hydrogenated", "partially hydrogenated").any(normalized::contains)) {
        flags += Flag("Palm / hydrogenated fat", "Worth knowing rather than panicking about. Frequency and the rest of your diet matter more than one ingredient in isolation.", Color(0xFFFFE4E1))
    }
    if (listOf("aspartame", "sucralose", "acesulfame", "e951", "e955", "e950").any(normalized::contains)) {
        flags += Flag("Sweeteners detected", "These are regulated food additives, but people can have different preferences or sensitivities. I wouldn't call them automatically harmful.", Color(0xFFE9F4FF))
    }
    if (flags.isEmpty()) {
        flags += Flag("No obvious red flags", "I didn't spot a common additive that I'd immediately call a concern from the text I could read. That isn't the same as saying the whole product is healthy.", Color(0xFFE8F6EA))
    }
    val verdict = when {
        flags.any { it.title.startsWith("E621") } -> "A few things worth knowing 👀"
        flags.any { it.title.startsWith("Palm") } -> "Worth a closer look 🧐"
        else -> "Looks fairly straightforward 🙂"
    }
    val summary = "I read the label and found ${flags.size} thing${if (flags.size == 1) "" else "s"} worth talking about."
    return IngredientAnalysis(verdict, summary, "$verdict\n\n$summary Ask me about any ingredient and I'll break it down without the chemistry lecture.", text.ifBlank { "No readable ingredient text yet." }, flags)
}
