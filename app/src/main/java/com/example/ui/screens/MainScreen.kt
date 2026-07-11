package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.DownloadRecord
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: DownloadViewModel = viewModel(),
    initialSharedUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val inputUrl by viewModel.inputUrl.collectAsState()
    val isExtracting by viewModel.isExtracting.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val downloadsHistory by viewModel.downloadsHistory.collectAsState()
    val showTelegramPopup by viewModel.showTelegramPopup.collectAsState()

    val cobaltApiUrl by viewModel.cobaltApiUrl.collectAsState()
    val videoQuality by viewModel.videoQuality.collectAsState()
    val audioOnly by viewModel.audioOnly.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    // If a shared URL was passed via system share intent, load it!
    LaunchedEffect(initialSharedUrl) {
        if (!initialSharedUrl.isNullOrBlank()) {
            viewModel.updateInputUrl(initialSharedUrl)
            viewModel.extractAndDownload(initialSharedUrl)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(GlowTeal, ElectricIndigo)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Logo",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Apon Downloader",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = LightText
                            )
                            Text(
                                text = "No Watermark Video Downloader",
                                fontSize = 11.sp,
                                color = GlowTeal,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = GlowTeal
                        )
                    }
                    IconButton(
                        onClick = { viewModel.showTelegramPopupAgain() },
                        modifier = Modifier.testTag("telegram_shortcut_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Telegram Channel",
                            tint = ElectricIndigo
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBackground,
                    titleContentColor = LightText
                )
            )
        },
        containerColor = DeepBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background glow effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x1A00D2C4),
                                Color.Transparent
                            ),
                            radius = 1200f
                        )
                    )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header spacer
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Platforms Supported Row
                item {
                    PlatformSection()
                }

                // Main Link Paste Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "ভিডিওর লিংক পেস্ট করুন (Paste Video Link)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LightText
                            )

                            OutlinedTextField(
                                value = inputUrl,
                                onValueChange = { viewModel.updateInputUrl(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("link_input_field"),
                                placeholder = {
                                    Text(
                                        text = "YouTube, TikTok, Facebook, Instagram...",
                                        color = GrayText,
                                        fontSize = 13.sp
                                    )
                                },
                                singleLine = true,
                                trailingIcon = {
                                    if (inputUrl.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.clearInputUrl() }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear text",
                                                tint = GrayText
                                            )
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GlowTeal,
                                    unfocusedBorderColor = MutedGray,
                                    focusedTextColor = LightText,
                                    unfocusedTextColor = LightText
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Paste from clipboard button
                                Button(
                                    onClick = {
                                        val clipboardText = clipboardManager.getText()?.text ?: ""
                                        if (clipboardText.isNotBlank()) {
                                            viewModel.updateInputUrl(clipboardText)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("paste_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste",
                                        tint = GlowTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Paste",
                                        color = GlowTeal,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                // Extract & Download button
                                Button(
                                    onClick = { viewModel.extractAndDownload(inputUrl) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("download_button"),
                                    enabled = !isExtracting && inputUrl.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GlowTeal,
                                        disabledContainerColor = MutedGray
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isExtracting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.Black,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "Download",
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Download",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Error / Success Message Banners
                item {
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        errorMessage?.let {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0x33EF4444)),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = RedError
                                    )
                                    Text(
                                        text = it,
                                        color = LightText,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = successMessage != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        successMessage?.let {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0x3310B981)),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = GreenSuccess
                                    )
                                    Text(
                                        text = it,
                                        color = LightText,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // History / Active Downloads header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ডাউনলোড তালিকা (Download List)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowTeal
                        )
                        if (downloadsHistory.isNotEmpty()) {
                            TextButton(
                                onClick = { viewModel.clearHistory() },
                                colors = ButtonDefaults.textButtonColors(contentColor = RedError)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear All",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear All", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // List of Downloads or empty state
                if (downloadsHistory.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudDownload,
                                    contentDescription = "Empty",
                                    tint = MutedGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "কোন ডাউনলোড হিস্ট্রি নেই",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GrayText
                                )
                                Text(
                                    text = "যেকোনো লিংক দিন অথবা অন্য অ্যাপের Share থেকে সিলেক্ট করুন",
                                    fontSize = 11.sp,
                                    color = MutedGray,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                } else {
                    items(downloadsHistory, key = { it.id }) { record ->
                        DownloadRecordItem(
                            record = record,
                            onDelete = { viewModel.deleteRecord(record.id) },
                            onShare = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Download Link: ${record.downloadUrl}\nDownloaded via Apon Downloader")
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Link"))
                            },
                            onCopy = {
                                record.downloadUrl?.let { link ->
                                    clipboardManager.setText(AnnotatedString(link))
                                }
                            }
                        )
                    }
                }

                // Footer margin
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            // Settings Dialog popup
            if (showSettingsDialog) {
                SettingsDialog(
                    apiUrl = cobaltApiUrl,
                    quality = videoQuality,
                    audioOnly = audioOnly,
                    onApiUrlChange = { viewModel.updateCobaltApiUrl(it) },
                    onQualityChange = { viewModel.updateVideoQuality(it) },
                    onAudioOnlyChange = { viewModel.updateAudioOnly(it) },
                    onDismiss = { showSettingsDialog = false }
                )
            }

            // Telegram channel popup
            if (showTelegramPopup) {
                TelegramJoinDialog(
                    onJoinClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/TeamWithApon"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback if telegram app is not installed - open in browser
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/TeamWithApon"))
                            context.startActivity(intent)
                        }
                    },
                    onDismiss = { viewModel.dismissTelegramPopup() }
                )
            }
        }
    }
}

@Composable
fun PlatformSection() {
    val platforms = listOf(
        Triple("YouTube", Icons.Default.PlayArrow, Color(0xFFEF4444)),
        Triple("Instagram", Icons.Default.CameraAlt, Color(0xFFEC4899)),
        Triple("TikTok", Icons.Default.MusicNote, Color(0xFF06B6D4)),
        Triple("Facebook", Icons.Default.Facebook, Color(0xFF3B82F6))
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "সমর্থিত প্ল্যাটফর্ম (Supported Platforms)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GrayText,
                modifier = Modifier.padding(start = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                platforms.forEach { (name, icon, color) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = name,
                                tint = color,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = LightText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadRecordItem(
    record: DownloadRecord,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val formattedDate = sdf.format(Date(record.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (record.status == "DOWNLOADING") DarkSurfaceVariant else DarkSurface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon based on platform
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when (record.platform) {
                                "YouTube" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                "Instagram" -> Color(0xFFEC4899).copy(alpha = 0.2f)
                                "TikTok" -> Color(0xFF06B6D4).copy(alpha = 0.2f)
                                "Facebook" -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                else -> GlowTeal.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (record.platform) {
                            "YouTube" -> Icons.Default.PlayArrow
                            "Instagram" -> Icons.Default.CameraAlt
                            "TikTok" -> Icons.Default.MusicNote
                            "Facebook" -> Icons.Default.Facebook
                            else -> Icons.Default.CloudDownload
                        },
                        contentDescription = record.platform,
                        tint = when (record.platform) {
                            "YouTube" -> Color(0xFFEF4444)
                            "Instagram" -> Color(0xFFEC4899)
                            "TikTok" -> Color(0xFF06B6D4)
                            "Facebook" -> Color(0xFF3B82F6)
                            else -> GlowTeal
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Details column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = record.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LightText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = record.platform,
                            fontSize = 11.sp,
                            color = GlowTeal,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 10.sp,
                            color = GrayText
                        )
                    }
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete record",
                        tint = GrayText.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Downloader State Section
            when (record.status) {
                "DOWNLOADING", "PENDING" -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = GlowTeal,
                                    strokeWidth = 1.5.dp
                                )
                                Text(
                                    text = if (record.status == "PENDING") "একাউন্টিং..." else "ডাউনলোড হচ্ছে...",
                                    fontSize = 11.sp,
                                    color = GlowTeal,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "${record.progress}%",
                                fontSize = 11.sp,
                                color = GlowTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { record.progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = GlowTeal,
                            trackColor = MutedGray.copy(alpha = 0.3f)
                        )
                    }
                }
                "COMPLETED" -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = GreenSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "ডাউনলোড সম্পন্ন",
                                fontSize = 11.sp,
                                color = GreenSuccess,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Share / copy button row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (record.downloadUrl != null) {
                                IconButton(
                                    onClick = onCopy,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy link",
                                        tint = GlowTeal,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                IconButton(
                                    onClick = onShare,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share link",
                                        tint = ElectricIndigo,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                "FAILED" -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RedError.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Failed",
                            tint = RedError,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "ডাউনলোড ব্যর্থ হয়েছে। সার্ভার চেক করুন।",
                            fontSize = 11.sp,
                            color = RedError,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TelegramJoinDialog(
    onJoinClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Telegram Logo",
                    tint = ElectricIndigo,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "টেলিগ্রাম চ্যানেলে জয়েন হউন",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightText
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "আমাদের সকল আপডেট এবং নতুন নতুন ট্রিকস পেতে এখনই 'Team With Apon' টেলিগ্রাম চ্যানেলে জয়েন হউন।",
                    fontSize = 13.sp,
                    color = GrayText,
                    lineHeight = 18.sp
                )
                Text(
                    text = "চ্যানেল লিংক: https://t.me/TeamWithApon",
                    fontSize = 12.sp,
                    color = GlowTeal,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onJoinClick()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
            ) {
                Text("Join Channel", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = GrayText)
            }
        },
        containerColor = DarkSurface,
        textContentColor = GrayText,
        titleContentColor = LightText,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun SettingsDialog(
    apiUrl: String,
    quality: String,
    audioOnly: Boolean,
    onApiUrlChange: (String) -> Unit,
    onQualityChange: (String) -> Unit,
    onAudioOnlyChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val publicInstances = listOf(
        "https://api.cobalt.tools",
        "https://cobalt.api.red",
        "https://co.wuk.ko"
    )

    var customApiInput by remember { mutableStateOf(apiUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings Icon",
                    tint = GlowTeal,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "সেটিংস (Settings)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightText
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // API Server Instance Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Cobalt API Server Instance",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LightText
                    )
                    Text(
                        text = "সার্ভার পরিবর্তন করতে পারেন যদি কোনোটা কাজ না করে",
                        fontSize = 10.sp,
                        color = GrayText
                    )

                    // Instance radio buttons
                    publicInstances.forEach { instance ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    customApiInput = instance
                                    onApiUrlChange(instance)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = apiUrl == instance,
                                onClick = {
                                    customApiInput = instance
                                    onApiUrlChange(instance)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = GlowTeal)
                            )
                            Text(
                                text = instance,
                                fontSize = 12.sp,
                                color = LightText,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    // Custom URL input
                    OutlinedTextField(
                        value = customApiInput,
                        onValueChange = {
                            customApiInput = it
                            onApiUrlChange(it)
                        },
                        label = { Text("Custom Cobalt URL", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowTeal,
                            unfocusedBorderColor = MutedGray,
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText
                        )
                    )
                }

                // Video Quality dropdown/selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ভিডিও রেজোলিউশন (Video Quality)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LightText
                    )
                    val qualities = listOf("1080", "720", "480", "360")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        qualities.forEach { q ->
                            val isSelected = quality == q
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) GlowTeal else DarkSurfaceVariant
                                    )
                                    .clickable { onQualityChange(q) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${q}p",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else LightText
                                )
                            }
                        }
                    }
                }

                // Audio Only Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "শুধু অডিও ডাউনলোড (Audio Only)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LightText
                        )
                        Text(
                            text = "ভিডিওর বদলে শুধুমাত্র MP3 ফাইল হিসেবে ডাউনলোড হবে",
                            fontSize = 10.sp,
                            color = GrayText
                        )
                    }
                    Switch(
                        checked = audioOnly,
                        onCheckedChange = onAudioOnlyChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = GlowTeal)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GlowTeal)
            ) {
                Text("Save & Close", color = Color.Black)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(18.dp)
    )
}
