package uk.co.puce4.passgen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load words from assets/words.txt
        val dictionary = loadWordsFromAssets()
        val generator = PasswordGenerator(dictionary)
        //val generator = PasswordGenerator(listOf("test", "word", "pass"))

        setContent {
            MaterialTheme {
                PasswordGenScreen(generator)
            }
        }
    }

    private fun loadWordsFromAssets(): List<String> {
        return try {
            assets.open("words.txt").bufferedReader().useLines { lines ->
                lines.filter { it.length in 4..6 && it.all { char -> char.isLetter() } }.toList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGenScreen(generator: PasswordGenerator) {
    // State management
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.smallestScreenWidthDp
    val itemsToShow = if (screenWidth >= 600) 14 else 7
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var wordCount by rememberSaveable { mutableStateOf(3f) }
    var includeNumber by rememberSaveable { mutableStateOf(false) }
    var separator by rememberSaveable { mutableStateOf("-") }

    var isDarkMode by remember { mutableStateOf(true) } // Manual toggle state

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Change passwords to a mutableStateListOf so Compose tracks individual removals
    val passwords = remember { mutableStateListOf<String>() }

    val refreshList: () -> Unit = {
        passwords.clear()
        repeat(itemsToShow) {
            val newPass = generator.generate(wordCount.toInt(), includeNumber, separator)
            passwords.add(newPass) // .add returns Boolean, but the lambda result is ignored now
        }
    }

    LaunchedEffect(Unit) { refreshList() }

    // Wrap everything in a custom theme provider
    MaterialTheme(colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("PassGen Pro") },
                        actions = {
                            // Dark Mode Toggle Switch in Top Bar
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { isDarkMode = it }
                            )
                        }
                    )
                }
            ) { padding ->
                // A single container for everything
                Box(
                    modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp)
                ) {
                    if (isLandscape) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Column 1: Settings
                            Column(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .verticalScroll(rememberScrollState()) // THIS IS OK
                            ) {
                                SettingsCard(
                                    wordCount,
                                    includeNumber,
                                    separator,
                                    {
                                        newValue -> wordCount = newValue
                                        refreshList()
                                    },
                                    {
                                        newValue -> includeNumber = newValue
                                        refreshList()
                                    },
                                    {
                                        newValue -> separator = newValue
                                        refreshList()
                                    }
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Button(
                                    onClick = refreshList,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                ) {
                                    Text("GENERATE")
                                }

                            }
                            // Sibling 2: List
                            PasswordListArea(
                                passwords, context, scope, snackbarHostState,
                                modifier = Modifier.weight(0.6f) // THIS IS OK
                            )
                        }
                    } else {
                        // PORTRAIT: Stacked
                        Column(modifier = Modifier.fillMaxSize()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            SettingsCard(
                                wordCount,
                                includeNumber, separator,
                                {
                                    newValue -> wordCount = newValue
                                    refreshList()
                                },
                                {
                                    newValue -> includeNumber = newValue
                                    refreshList()
                                },
                                {
                                    newValue -> separator = newValue
                                    refreshList()
                                }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Give the list all the middle space
                            PasswordListArea(
                                passwords, context, scope, snackbarHostState,
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = refreshList,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                            ) {
                                Text("GENERATE NEW PASSWORDS")
                            }
                        }

                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordListArea(
    passwords: MutableList<String>,
    context: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = passwords,
            // Using 'it' is safe now because we know words are unique
            key = { it }
        ) { password ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.EndToStart) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        passwords.remove(password)
                        true
                    } else {
                        false
                    }
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false, // Only swipe left to delete
                backgroundContent = {
                    val color = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                        else -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)) // Match your card corner
                            .background(color),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            ) {
                // Your existing PasswordItem component
                PasswordItem(password) {
                    copyToClipboard(context, password)
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar("Copied!")
                    }
                }
            }
        }
    }
}


@Composable
fun SettingsCard(
    wordCount: Float,
    includeNumber: Boolean,
    separator: String,
    onCountChange: (Float) -> Unit,
    onNumChange: (Boolean) -> Unit,
    onSeparatorChange: (String) -> Unit
) {
    val separators = listOf("-", ".", "_", "/", " ")
    val labels = listOf("-", ".", "_", "/", "Space")
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Complexity Settings", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Words: ${wordCount.toInt()}", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = wordCount,
                onValueChange = onCountChange,
                valueRange = 2f..6f,
                steps = 3
            )
            Text("Separator", style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                separators.forEachIndexed { index, s ->
                    FilterChip(
                        selected = separator == s,
                        onClick = { onSeparatorChange(s) },
                        label = { Text(labels[index]) }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = includeNumber, onCheckedChange = onNumChange)
                Text("Append digits (0-99)")
            }
        }
    }
}

@Composable
fun PasswordItem(password: String, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = password,
                modifier = Modifier.weight(1f),
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text("📋", fontSize = 20.sp) // Simple icon
        }
    }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("password", text)
    clipboard.setPrimaryClip(clip)
}