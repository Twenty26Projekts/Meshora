package com.bitchat.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitchat.android.ui.theme.ThemePreference
import com.bitchat.android.ui.theme.ThemePreferenceManager
import kotlinx.coroutines.launch

private enum class SimpleTab(val label: String) {
    NEARBY("Nearby"),
    CHATS("Chats"),
    PEOPLE("People"),
    PROFILE("Profile")
}

@Composable
fun SimpleMainScreen(viewModel: ChatViewModel) {
    val selectedPrivatePeer by viewModel.selectedPrivateChatPeer.collectAsStateWithLifecycle()

    if (selectedPrivatePeer != null) {
        BackHandler {
            viewModel.endPrivateChat()
        }
        ChatScreen(viewModel = viewModel)
        return
    }

    var selectedTab by remember { mutableStateOf(SimpleTab.NEARBY) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp,
            ) {
                SimpleTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    SimpleTab.NEARBY -> Icons.Default.Wifi
                                    SimpleTab.CHATS -> Icons.Default.ChatBubble
                                    SimpleTab.PEOPLE -> Icons.Default.Link
                                    SimpleTab.PROFILE -> Icons.Default.Person
                                },
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            SimpleTab.NEARBY -> NearbyTab(viewModel, Modifier.padding(padding))
            SimpleTab.CHATS -> ChatsTab(viewModel, Modifier.padding(padding))
            SimpleTab.PEOPLE -> PeopleTab(viewModel, Modifier.padding(padding))
            SimpleTab.PROFILE -> ProfileTab(viewModel, Modifier.padding(padding))
        }
    }
}

@Composable
private fun NearbyTab(viewModel: ChatViewModel, modifier: Modifier = Modifier) {
    val peers by viewModel.connectedPeers.collectAsStateWithLifecycle()
    val nicknames by viewModel.peerNicknames.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SimpleSettingsDialog(onDismiss = { showSettings = false })
    }

    Column(modifier = modifier.fillMaxSize()) {
        SimpleTopBar(
            title = "Nearby Devices",
            actionIcon = Icons.Default.Settings,
            onAction = { showSettings = true }
        )

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Device Discovery",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Discover nearby people using Meshora's local mesh network.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            if (peers.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Wifi,
                    title = "No one nearby",
                    subtitle = "Keep Meshora open while nearby devices are discovered."
                )
            } else {
                Text(
                    "Nearby (${peers.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(peers, key = { it }) { peerId ->
                        PersonRow(
                            name = nicknames[peerId]?.takeIf { it.isNotBlank() } ?: shortPeerId(peerId),
                            subtitle = "Available nearby",
                            onClick = { scope.launch { viewModel.startPrivateChat(peerId) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatsTab(viewModel: ChatViewModel, modifier: Modifier = Modifier) {
    val privateChats by viewModel.privateChats.collectAsStateWithLifecycle()
    val nicknames by viewModel.peerNicknames.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        SimpleTopBar(title = "Chats")

        if (privateChats.isEmpty()) {
            EmptyState(
                icon = Icons.Default.ChatBubble,
                title = "No conversations yet",
                subtitle = "Start a private chat from the Nearby or People tab."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(privateChats.keys.toList(), key = { it }) { peerId ->
                    val messages = privateChats[peerId].orEmpty()
                    val lastMessage = messages.lastOrNull()?.content.orEmpty()
                    PersonRow(
                        name = nicknames[peerId]?.takeIf { it.isNotBlank() } ?: shortPeerId(peerId),
                        subtitle = lastMessage.ifBlank { "Private conversation" },
                        onClick = { scope.launch { viewModel.startPrivateChat(peerId) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun PeopleTab(viewModel: ChatViewModel, modifier: Modifier = Modifier) {
    val peers by viewModel.connectedPeers.collectAsStateWithLifecycle()
    val nicknames by viewModel.peerNicknames.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        SimpleTopBar(title = "People")
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                "Your local network",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "People currently visible to Meshora.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            if (peers.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Link,
                    title = "No people connected",
                    subtitle = "Nearby peers will appear here automatically."
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(peers, key = { it }) { peerId ->
                        PersonRow(
                            name = nicknames[peerId]?.takeIf { it.isNotBlank() } ?: shortPeerId(peerId),
                            subtitle = "Connected through mesh",
                            onClick = { scope.launch { viewModel.startPrivateChat(peerId) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTab(viewModel: ChatViewModel, modifier: Modifier = Modifier) {
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showAppearance by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showEditNickname by remember { mutableStateOf(false) }

    if (showSettings) SimpleSettingsDialog(onDismiss = { showSettings = false })
    if (showPrivacy) PrivacyDialog(onDismiss = { showPrivacy = false })
    if (showAppearance) AppearanceDialog(onDismiss = { showAppearance = false })
    if (showAbout) AboutDialog(onDismiss = { showAbout = false })
    if (showEditNickname) {
        EditNicknameDialog(
            currentNickname = nickname,
            onDismiss = { showEditNickname = false },
            onSave = { newNickname ->
                viewModel.setNickname(newNickname)
                showEditNickname = false
            }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        SimpleTopBar(
            title = "Profile",
            actionIcon = Icons.Default.Settings,
            onAction = { showSettings = true }
        )

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nickname.firstOrNull()?.uppercase() ?: "B",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    nickname.ifBlank { "Meshora user" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = { showEditNickname = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit username",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(28.dp))

            ProfileItem(
                title = "Privacy",
                subtitle = "Security and private communication",
                icon = Icons.Default.Security,
                onClick = { showPrivacy = true }
            )
            ProfileItem(
                title = "Appearance",
                subtitle = "Dark and light theme",
                icon = Icons.Default.Palette,
                onClick = { showAppearance = true }
            )
            ProfileItem(
                title = "About",
                subtitle = "About Meshora, support and feedback",
                icon = Icons.Default.Info,
                onClick = { showAbout = true }
            )
        }
    }
}

@Composable
private fun SimpleTopBar(
    title: String,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        if (actionIcon != null && onAction != null) {
            IconButton(onClick = onAction) {
                Icon(actionIcon, contentDescription = "Settings")
            }
        }
    }
}

@Composable
private fun PersonRow(name: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                name.firstOrNull()?.uppercase() ?: "?",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    HorizontalDivider()
}

@Composable
private fun PrivacyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Privacy & Security") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Meshora is designed for private, nearby communication without requiring a traditional centralized messaging account.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Communication can travel through the local mesh network using Bluetooth-based peer-to-peer connections. The app does not need a central server to discover nearby peers.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Private mesh messages use the existing encrypted communication architecture. Your conversations and identity data are handled by the app on your device rather than being presented as a public profile.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Privacy note: nearby communication still depends on the permissions and capabilities of your device. Only share information you are comfortable communicating to another peer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

@Composable
private fun AppearanceDialog(onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val themePreference by ThemePreferenceManager.themeFlow.collectAsStateWithLifecycle()
    val darkModeEnabled = themePreference == ThemePreference.Dark

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Appearance") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dark theme", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (darkModeEnabled) "Dark mode is on" else "Light mode is on",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = { enabled ->
                        ThemePreferenceManager.set(
                            context,
                            if (enabled) ThemePreference.Dark else ThemePreference.Light
                        )
                    }
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About Meshora") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Meshora is a privacy-focused nearby communication app built around local mesh networking.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Built with Kotlin, Jetpack Compose, Material 3, MVVM, Coroutines and Flow, with Bluetooth mesh networking and encrypted peer-to-peer communication underneath.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Support Meshora",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Optional donations can help support continued development, maintenance and future improvements to Meshora.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Feature requests & bug reports",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Have an idea or found a problem? Use the button below to open your email app with a ready-made Meshora feedback subject.",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_SENDTO,
                            Uri.parse("mailto:")
                        ).apply {
                            putExtra(Intent.EXTRA_SUBJECT, "Meshora Feature Request / Bug Report")
                        }
                        try { context.startActivity(intent) } catch (_: Exception) { }
                    }
                ) {
                    Text("Send feedback by email")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

@Composable
private fun EditNicknameDialog(
    currentNickname: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var value by remember(currentNickname) { mutableStateOf(currentNickname.take(8)) }
    val trimmed = value.trim()
    val isValid = trimmed.isNotEmpty() && trimmed.length <= 8

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit username") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it.take(8) },
                    singleLine = true,
                    label = { Text("Username") },
                    supportingText = { Text("${value.length}/8 characters") }
                )
                Text(
                    "Your new username will be announced to nearby Meshora users.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = { onSave(trimmed) }
            ) {
                Text("Save")
            }
        }
    )
}

@Composable
private fun SimpleSettingsDialog(onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val themePreference by ThemePreferenceManager.themeFlow.collectAsStateWithLifecycle()
    val darkModeEnabled = themePreference == ThemePreference.Dark

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Dark theme",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            if (darkModeEnabled) "Dark mode is on" else "Light mode is on",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = darkModeEnabled,
                        onCheckedChange = { enabled ->
                            ThemePreferenceManager.set(
                                context,
                                if (enabled) ThemePreference.Dark else ThemePreference.Light
                            )
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "More settings will be added here later.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

private fun shortPeerId(peerId: String): String {
    if (peerId.length <= 12) return peerId
    return "${peerId.take(6)}…${peerId.takeLast(4)}"
}

