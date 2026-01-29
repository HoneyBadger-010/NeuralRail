package com.neuralrail.neuralrailapp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.*
import com.neuralrail.neuralrailapp.presentation.theme.*
import com.neuralrail.neuralrailapp.presentation.viewmodels.EducationHubViewModel

@Composable
fun EducationHubScreen(
    viewModel: EducationHubViewModel, 
    onBack: () -> Unit = {},
    onArticleClick: (EducationContent) -> Unit = {}
) {
    val factState by viewModel.factState.collectAsState()
    val contentState by viewModel.contentState.collectAsState()
    val quizState by viewModel.quizState.collectAsState()
    var showQuiz by remember { mutableStateOf(false) }
    val appColors = LocalAppColors.current

    Column(modifier = Modifier.fillMaxSize().background(appColors.background)) {
        // Header
        LearnHeader(onBack = onBack)
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fact of the Day
            item {
                when (val fact = factState) {
                    is UiState.Success -> FactCard(fact.data)
                    is UiState.Loading -> LoadingCard()
                    is UiState.Error -> ErrorCard(fact.message)
                }
            }
            
            // Daily Quiz Banner
            item { 
                QuizBanner(
                    questionsCount = when (val quiz = quizState) {
                        is UiState.Success -> quiz.data.size
                        else -> 5
                    },
                    onClick = { showQuiz = true }
                ) 
            }
            
            // Learn & Explore Section
            item {
                Text(
                    stringResource(R.string.learn_explore),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = LocalAppColors.current.textPrimary
                )
            }
            
            when (val content = contentState) {
                is UiState.Success -> items(content.data) { article -> 
                    ContentCard(article, onClick = { onArticleClick(article) }) 
                }
                is UiState.Loading -> item { LoadingCard() }
                is UiState.Error -> item { ErrorCard(content.message) }
            }
            
            // India Progress Card
            item { IndiaProgressCard() }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
    
    // Quiz Dialog
    if (showQuiz) {
        when (val quiz = quizState) {
            is UiState.Success -> QuizDialog(quiz.data, onDismiss = { showQuiz = false })
            else -> {}
        }
    }
}

@Composable
private fun LearnHeader(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BluePrimary,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Color.White)
            }
            Spacer(Modifier.width(4.dp))
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.School, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    stringResource(R.string.learn),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    stringResource(R.string.rail_sustainability_knowledge),
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun FactCard(fact: FactOfTheDay) {
    val sourceText = stringResource(R.string.source, fact.source)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(BluePrimaryDark, BluePrimary, BlueSecondary)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = AccentYellow.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Lightbulb, null, tint = AccentYellow, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.fact_of_the_day),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    fact.fact,
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Text(
                        fact.relatedStat,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    sourceText,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun QuizBanner(questionsCount: Int, onClick: () -> Unit) {
    val questionsNewDailyText = stringResource(R.string.questions_new_daily, questionsCount)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = AccentYellow.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = AccentYellow.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Psychology, null, tint = AccentYellow, modifier = Modifier.size(30.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.daily_green_quiz),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = LocalAppColors.current.textPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    questionsNewDailyText,
                    fontSize = 13.sp,
                    color = LocalAppColors.current.textSecondary
                )
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(stringResource(R.string.start), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            }
        }
    }
}

@Composable
private fun ContentCard(content: EducationContent, onClick: () -> Unit = {}) {
    val articleText = stringResource(R.string.article)
    val infographicText = stringResource(R.string.infographic)
    val videoText = stringResource(R.string.video)
    val quizText = stringResource(R.string.quiz)
    val readText = stringResource(R.string.read)
    val minReadText = stringResource(R.string.min_read, (content.content.length / 200).coerceAtLeast(1))
    
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
    val typeLabel = when (content.type) {
        ContentType.ARTICLE -> articleText
        ContentType.INFOGRAPHIC -> infographicText
        ContentType.VIDEO -> videoText
        ContentType.QUIZ -> quizText
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = LocalAppColors.current.backgroundCard,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                // Type icon with gradient background
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = typeColor.copy(alpha = 0.15f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(typeColor.copy(alpha = 0.2f), typeColor.copy(alpha = 0.08f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(typeIcon, null, tint = typeColor, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // Type badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = typeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            typeLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = typeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        content.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = LocalAppColors.current.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 22.sp
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = LocalAppColors.current.textMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Content preview
            Text(
                content.content.take(120).replace("\n", " ") + "...",
                fontSize = 13.sp,
                color = LocalAppColors.current.textSecondary,
                maxLines = 2,
                lineHeight = 20.sp,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Footer with metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        null,
                        tint = LocalAppColors.current.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        minReadText,
                        fontSize = 12.sp,
                        color = LocalAppColors.current.textMuted
                    )
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        Icons.Default.Visibility,
                        null,
                        tint = LocalAppColors.current.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${(100..500).random()}",
                        fontSize = 12.sp,
                        color = LocalAppColors.current.textMuted
                    )
                }
                
                // Read more button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = typeColor.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            readText,
                            color = typeColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ArrowForward,
                            null,
                            tint = typeColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IndiaProgressCard() {
    val electrificationText = stringResource(R.string.electrification)
    val routesElectrifiedText = stringResource(R.string.routes_electrified)
    val solarStationsText = stringResource(R.string.solar_stations)
    val solarPoweredText = stringResource(R.string.solar_powered_stations)
    val netZeroText = stringResource(R.string.net_zero_goal)
    val target2030Text = stringResource(R.string.target_2030)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = LocalAppColors.current.backgroundCard
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = AccentOrange.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Flag, null, tint = AccentOrange, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        stringResource(R.string.india_green_railway),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = LocalAppColors.current.textPrimary
                    )
                    Text(
                        stringResource(R.string.progress_sustainability),
                        fontSize = 12.sp,
                        color = LocalAppColors.current.textSecondary
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            ProgressItem(Icons.Default.Bolt, electrificationText, 0.90f, routesElectrifiedText, AccentCyan)
            Spacer(Modifier.height(14.dp))
            ProgressItem(Icons.Default.WbSunny, solarStationsText, 0.65f, solarPoweredText, AccentYellow)
            Spacer(Modifier.height(14.dp))
            ProgressItem(Icons.Default.GpsFixed, netZeroText, 0.35f, target2030Text, AccentGreen)
        }
    }
}

@Composable
private fun ProgressItem(icon: ImageVector, label: String, progress: Float, description: String, color: Color) {
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = LocalAppColors.current.textPrimary)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Text(
                    "${(progress * 100).toInt()}%",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.12f)
        )
        Spacer(Modifier.height(4.dp))
        Text(description, fontSize = 12.sp, color = LocalAppColors.current.textSecondary)
    }
}


@Composable
private fun QuizDialog(questions: List<QuizQuestion>, onDismiss: () -> Unit) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var showResult by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var quizCompleted by remember { mutableStateOf(false) }
    
    val question = questions.getOrNull(currentIndex)
    val totalQuestions = questions.size
    val progress = (currentIndex + 1).toFloat() / totalQuestions
    
    val questionOfText = stringResource(R.string.question_of, currentIndex + 1, totalQuestions)
    val scoreText = stringResource(R.string.score, score)
    val quizCompleteText = stringResource(R.string.quiz_complete)
    val scoredOutOfText = stringResource(R.string.scored_out_of, score, totalQuestions)
    val excellentText = stringResource(R.string.excellent_eco_expert)
    val goodJobText = stringResource(R.string.good_job_learning)
    val niceTryText = stringResource(R.string.nice_try_improve)
    val keepExploringText = stringResource(R.string.keep_exploring)
    val checkAnswerText = stringResource(R.string.check_answer)
    val nextQuestionText = stringResource(R.string.next_question)
    val seeResultsText = stringResource(R.string.see_results)
    val doneText = stringResource(R.string.done)
    val exitQuizText = stringResource(R.string.exit_quiz)
    
    AlertDialog(
        onDismissRequest = { if (!quizCompleted) onDismiss() },
        containerColor = LocalAppColors.current.backgroundCard,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = AccentYellow.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Psychology, null, tint = AccentYellow, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            questionOfText,
                            color = LocalAppColors.current.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BlueAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            scoreText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = BlueAccent,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AccentYellow,
                    trackColor = AccentYellow.copy(alpha = 0.15f)
                )
            }
        },
        text = {
            if (quizCompleted) {
                // Quiz completed view
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val percentage = (score.toFloat() / totalQuestions * 100).toInt()
                    val emoji = when {
                        percentage >= 80 -> "🏆"
                        percentage >= 60 -> "🌟"
                        percentage >= 40 -> "👍"
                        else -> "📚"
                    }
                    
                    Text(emoji, fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        quizCompleteText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = LocalAppColors.current.textPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        scoredOutOfText,
                        fontSize = 16.sp,
                        color = LocalAppColors.current.textSecondary
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            percentage >= 80 -> AccentGreen.copy(alpha = 0.15f)
                            percentage >= 60 -> AccentYellow.copy(alpha = 0.15f)
                            else -> BlueAccent.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            when {
                                percentage >= 80 -> excellentText
                                percentage >= 60 -> goodJobText
                                percentage >= 40 -> niceTryText
                                else -> keepExploringText
                            },
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = LocalAppColors.current.textPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (question != null) {
                Column {
                    Text(
                        question.question,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = LocalAppColors.current.textPrimary,
                        lineHeight = 24.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    
                    question.options.forEachIndexed { index, option ->
                        val isCorrect = showResult && index == question.correctAnswer
                        val isWrong = showResult && index == selectedAnswer && selectedAnswer != question.correctAnswer
                        val isSelected = selectedAnswer == index
                        val localAppColors = LocalAppColors.current
                        
                        val bgColor = when {
                            isCorrect -> AccentGreen.copy(alpha = 0.15f)
                            isWrong -> AccentRed.copy(alpha = 0.15f)
                            isSelected -> BlueAccent.copy(alpha = 0.15f)
                            else -> localAppColors.backgroundCard
                        }
                        val iconTint = when {
                            isCorrect -> AccentGreen
                            isWrong -> AccentRed
                            isSelected -> BlueAccent
                            else -> TextMuted
                        }
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = !showResult) { selectedAnswer = index },
                            shape = RoundedCornerShape(12.dp),
                            color = bgColor
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    when {
                                        isCorrect -> Icons.Default.CheckCircle
                                        isWrong -> Icons.Default.Cancel
                                        isSelected -> Icons.Default.RadioButtonChecked
                                        else -> Icons.Default.RadioButtonUnchecked
                                    },
                                    null,
                                    tint = iconTint,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    option,
                                    fontSize = 14.sp,
                                    color = LocalAppColors.current.textPrimary
                                )
                            }
                        }
                    }
                    
                    // Explanation
                    if (showResult) {
                        Spacer(Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = AccentCyan.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    null,
                                    tint = AccentYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    question.explanation,
                                    fontSize = 13.sp,
                                    color = LocalAppColors.current.textPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (quizCompleted) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(doneText, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        if (!showResult && selectedAnswer >= 0) {
                            showResult = true
                            if (selectedAnswer == question?.correctAnswer) score++
                        } else if (showResult) {
                            if (currentIndex < questions.size - 1) {
                                currentIndex++
                                selectedAnswer = -1
                                showResult = false
                            } else {
                                quizCompleted = true
                            }
                        }
                    },
                    enabled = selectedAnswer >= 0 || showResult || quizCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentYellow,
                        disabledContainerColor = AccentYellow.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        when {
                            !showResult -> checkAnswerText
                            currentIndex < questions.size - 1 -> nextQuestionText
                            else -> seeResultsText
                        },
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            if (!quizCompleted) {
                TextButton(onClick = onDismiss) {
                    Text(exitQuizText, color = LocalAppColors.current.textSecondary)
                }
            }
        }
    )
}

@Composable
private fun LoadingCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        color = LocalAppColors.current.backgroundCard
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentYellow)
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AccentRed.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = AccentRed.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Error, null, tint = AccentRed, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                message,
                color = AccentRed,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
