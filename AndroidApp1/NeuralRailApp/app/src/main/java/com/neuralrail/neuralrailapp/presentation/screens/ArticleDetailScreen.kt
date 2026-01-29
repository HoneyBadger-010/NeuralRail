package com.neuralrail.neuralrailapp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.data.models.ContentType
import com.neuralrail.neuralrailapp.data.models.EducationContent
import com.neuralrail.neuralrailapp.presentation.theme.*

@Composable
fun ArticleDetailScreen(
    content: EducationContent,
    onBack: () -> Unit = {}
) {
    var isBookmarked by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf((50..200).random()) }
    var isLiked by remember { mutableStateOf(false) }
    
    val typeIcon = when (content.type) {
        ContentType.ARTICLE -> Icons.Default.Article
        ContentType.INFOGRAPHIC -> Icons.Default.BarChart
        ContentType.VIDEO -> Icons.Default.PlayCircle
        ContentType.QUIZ -> Icons.Default.Quiz
    }
    val typeColor = when (content.type) {
        ContentType.ARTICLE -> AccentCyan
        ContentType.INFOGRAPHIC -> AccentOrange
        ContentType.VIDEO -> AccentRed
        ContentType.QUIZ -> AccentYellow
    }

    val appColors = LocalAppColors.current
    Column(modifier = Modifier.fillMaxSize().background(appColors.background)) {
        // Header with gradient
        ArticleHeader(
            title = content.title,
            type = content.type,
            typeIcon = typeIcon,
            typeColor = typeColor,
            isBookmarked = isBookmarked,
            onBack = onBack,
            onBookmark = { isBookmarked = !isBookmarked },
            onShare = { showShareSheet = true }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type badge and reading time
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = typeColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(typeIcon, null, tint = typeColor, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                content.type.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = typeColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            null,
                            tint = LocalAppColors.current.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.min_read_format, (content.content.length / 200).coerceAtLeast(1)),
                            color = LocalAppColors.current.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            
            // Main content card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = LocalAppColors.current.backgroundCard
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Parse and display content sections
                        val sections = parseContent(content.content)
                        sections.forEachIndexed { index, section ->
                            when (section) {
                                is ContentSection.Paragraph -> {
                                    Text(
                                        section.text,
                                        color = LocalAppColors.current.textPrimary,
                                        fontSize = 15.sp,
                                        lineHeight = 24.sp
                                    )
                                }
                                is ContentSection.Heading -> {
                                    if (index > 0) Spacer(Modifier.height(16.dp))
                                    Text(
                                        section.text,
                                        color = LocalAppColors.current.textPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                is ContentSection.Bullet -> {
                                    Row(
                                        modifier = Modifier.padding(start = 8.dp, top = 8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .padding(top = 8.dp)
                                                .size(6.dp),
                                            shape = CircleShape,
                                            color = typeColor
                                        ) {}
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            section.text,
                                            color = LocalAppColors.current.textPrimary,
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                                is ContentSection.Highlight -> {
                                    Spacer(Modifier.height(12.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = typeColor.copy(alpha = 0.1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                Icons.Default.Lightbulb,
                                                null,
                                                tint = AccentYellow,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                section.text,
                                                color = LocalAppColors.current.textPrimary,
                                                fontSize = 14.sp,
                                                lineHeight = 22.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                            if (section is ContentSection.Paragraph && index < sections.size - 1) {
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
            
            // Key takeaways
            item {
                KeyTakeawaysCard(content.type, typeColor)
            }
            
            // Engagement card
            item {
                EngagementCard(
                    likeCount = likeCount,
                    isLiked = isLiked,
                    isBookmarked = isBookmarked,
                    onLike = { 
                        isLiked = !isLiked
                        likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                    },
                    onBookmark = { isBookmarked = !isBookmarked },
                    onShare = { showShareSheet = true },
                    typeColor = typeColor
                )
            }
            
            // Related topics
            item {
                RelatedTopicsCard(typeColor)
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ArticleHeader(
    title: String,
    type: ContentType,
    typeIcon: ImageVector,
    typeColor: Color,
    isBookmarked: Boolean,
    onBack: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BluePrimaryDark, BluePrimary)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, stringResource(R.string.share), tint = Color.White.copy(alpha = 0.8f))
                }
                IconButton(onClick = onBookmark) {
                    Icon(
                        if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, 
                        stringResource(R.string.bookmark), 
                        tint = if (isBookmarked) AccentYellow else Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Title section
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = typeColor.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(typeIcon, null, tint = typeColor, modifier = Modifier.size(30.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp
                )
            }
        }
    }
}

@Composable
private fun KeyTakeawaysCard(type: ContentType, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = LocalAppColors.current.backgroundCard
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = AccentGreen.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.key_takeaways),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = LocalAppColors.current.textPrimary
                )
            }
            Spacer(Modifier.height(14.dp))
            
            val takeaways = listOf(
                stringResource(R.string.takeaway_1),
                stringResource(R.string.takeaway_2),
                stringResource(R.string.takeaway_3)
            )
            
            takeaways.forEach { takeaway ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Check,
                        null,
                        tint = AccentGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        takeaway,
                        color = LocalAppColors.current.textSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EngagementCard(
    likeCount: Int,
    isLiked: Boolean,
    isBookmarked: Boolean,
    onLike: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    typeColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = LocalAppColors.current.backgroundCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onLike)
                    .padding(12.dp)
            ) {
                Icon(
                    if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    stringResource(R.string.like),
                    tint = if (isLiked) AccentRed else TextSecondary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$likeCount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isLiked) AccentRed else TextSecondary
                )
            }
            
            // Bookmark button
            val savedText = stringResource(R.string.saved)
            val saveText = stringResource(R.string.save_action)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onBookmark)
                    .padding(12.dp)
            ) {
                Icon(
                    if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    stringResource(R.string.bookmark),
                    tint = if (isBookmarked) AccentYellow else TextSecondary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (isBookmarked) savedText else saveText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isBookmarked) AccentYellow else TextSecondary
                )
            }
            
            // Share button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onShare)
                    .padding(12.dp)
            ) {
                Icon(
                    Icons.Default.Share,
                    stringResource(R.string.share),
                    tint = typeColor,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.share),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = typeColor
                )
            }
        }
    }
}

@Composable
private fun RelatedTopicsCard(color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = LocalAppColors.current.backgroundCard
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                stringResource(R.string.related_topics),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = LocalAppColors.current.textPrimary
            )
            Spacer(Modifier.height(12.dp))
            
            val topics = listOf(
                stringResource(R.string.energy_efficiency),
                stringResource(R.string.green_travel),
                stringResource(R.string.solar_power)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                topics.forEach { topic ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = BlueAccent.copy(alpha = 0.12f)
                    ) {
                        Text(
                            topic,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = BlueAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Content parsing helpers
private sealed class ContentSection {
    data class Paragraph(val text: String) : ContentSection()
    data class Heading(val text: String) : ContentSection()
    data class Bullet(val text: String) : ContentSection()
    data class Highlight(val text: String) : ContentSection()
}

private fun parseContent(content: String): List<ContentSection> {
    val sections = mutableListOf<ContentSection>()
    val lines = content.split("\n").filter { it.isNotBlank() }
    
    lines.forEach { line ->
        when {
            line.startsWith("##") -> sections.add(ContentSection.Heading(line.removePrefix("##").trim()))
            line.startsWith("•") || line.startsWith("-") -> sections.add(ContentSection.Bullet(line.removePrefix("•").removePrefix("-").trim()))
            line.startsWith("💡") || line.startsWith("!") -> sections.add(ContentSection.Highlight(line.removePrefix("💡").removePrefix("!").trim()))
            else -> sections.add(ContentSection.Paragraph(line.trim()))
        }
    }
    
    return sections
}
