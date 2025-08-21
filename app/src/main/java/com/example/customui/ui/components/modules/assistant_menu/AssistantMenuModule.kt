package com.example.customui.ui.components.modules.assistant_menu

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import compose.icons.FeatherIcons
import compose.icons.feathericons.Menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.unit.IntOffset
import com.example.customui.ui.components.custom.WrapContentGrid

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import com.example.customui.R
import com.example.customui.services.assistantmenu.AssistantMenuServiceHelper

@Composable
fun AssistantMenuModule(
    isExpanded: Boolean,
    menuOffset: IntOffset,
    onToggleExpand: () -> Unit,
    onToggleClose: () -> Unit,
    onCloseMenu: () -> Unit,
    menuManager: AssistantMenuServiceHelper,
) {
    Log.d("AssistantMenu", "🔍 Expanded menuOffset: $menuOffset")
    if (isExpanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            Log.d(
                                "AssistantMenuModule",
                                "Tap outside detected by Box, closing menu."
                            )
                            onToggleClose() // Gọi hàm để đóng menu
                        }
                    )
                }
        ) {
            val density = LocalDensity.current
            val configuration = LocalConfiguration.current

            // Get screen dimensions
            val screenWidthDp = configuration.screenWidthDp.dp
            val screenHeightDp = configuration.screenHeightDp.dp
            val screenWidthPx = with(density) { screenWidthDp.toPx() }
            val screenHeightPx = with(density) { screenHeightDp.toPx() }

            // Menu size estimation (you can adjust based on your content)
            val menuWidthDp = 260.dp  // Adjust based on your menu content
            val menuHeightDp = 160.dp // Adjust based on your menu content
            val menuWidthPx = with(density) { menuWidthDp.toPx() }
            val menuHeightPx = with(density) { menuHeightDp.toPx() }
            val paddingDp = 16.dp
            val paddingPx = with(density) { paddingDp.toPx() }

            // Button position from Service
            val buttonX = menuOffset.x.toFloat()
            val buttonY = menuOffset.y.toFloat()
            val buttonSize = with(density) { 48.dp.toPx() }

            // Smart positioning logic
            val smartOffset = remember(menuOffset, screenWidthPx, screenHeightPx) {
                val buttonX = menuOffset.x.toFloat()
                val buttonY = menuOffset.y.toFloat()

                // Giữ menu ngay tại vị trí button
                var targetX = buttonX
                var targetY = buttonY

                // Ràng buộc biên để menu không tràn ra ngoài màn hình
                val finalX = targetX.coerceIn(
                    0f,
                    (screenWidthPx - buttonSize - menuWidthPx).coerceAtLeast(0f)
                )
                val finalY = targetY.coerceIn(
                    0f,
                    (screenHeightPx - buttonSize - menuHeightPx).coerceAtLeast(0f)
                )

                Log.d("AssistantMenu", "finalX: $finalX finalY: $finalY")

                IntOffset(finalX.toInt(), finalY.toInt())
            }


            // Menu container with smart positioning
            var actualMenuSize by remember { mutableStateOf(IntSize.Zero) }

            Box(
                modifier = Modifier
                    .offset { smartOffset }
                    .widthIn(max = screenWidthDp - paddingDp * 2)
                    .heightIn(max = screenHeightDp - paddingDp * 2)
                    .wrapContentSize()
                    .wrapContentSize()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Prevent click through to background */ }
                    .onGloballyPositioned { coordinates ->
                        // Get actual menu size for future calculations
                        actualMenuSize = coordinates.size
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg_menu),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )

                val features = menuManager.listAvailableFeatures()

                WrapContentGrid(
                    columns = 3,
                    horizontalSpacing = 8.dp,
                    padding = 8.dp,
                    verticalSpacing = 8.dp
                ) {
                    features.forEachIndexed { index, label ->
                        val currentItem = menuManager.findModuleByName(label)

                        IconButton(
                            onClick = {
                                menuManager.toggleFeature(
                                    label,
                                    !currentItem!!.isEnabled()
                                )
                            },
                            modifier = Modifier
                                .padding(4.dp)
                                .background(Color(0xB5222222), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (currentItem!!.isEnabled())
                                    currentItem.getChangedIcon()
                                else
                                    currentItem.getDefaultIcon(),
                                contentDescription = label,
                                tint = if (currentItem.isEnabled()) Color.White else Color.Gray,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(48.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = FeatherIcons.Menu,
                contentDescription = "Assistant Menu",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.bg_menu),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopStart,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp)),
            )
        }
    }
}


@Composable
fun FunctionalButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

