package com.example.jettrivia.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jettrivia.model.QuestionItem
import com.example.jettrivia.screens.QuestionsViewModel
import com.example.jettrivia.util.AppColors

@Composable
fun Questions(viewModel: QuestionsViewModel) {
    val questions = viewModel.data.value.data?.toMutableList()
    val questionIndex = remember { mutableStateOf(0) }

    if (viewModel.data.value.loading == true) {
        CircularProgressIndicator()
    } else {
        val question = try {
            questions?.get(questionIndex.value)
        } catch (e: Exception) {
            null
        }

        if (questions != null) {
            QuestionDisplay(
                question = question!!,
                questionIndex = questionIndex,
                viewModel = viewModel
            ) {
                questionIndex.value = questionIndex.value + 1
            }
        }
    }
}

@Composable
fun QuestionDisplay(
    modifier: Modifier = Modifier,
    question: QuestionItem,
    questionIndex: MutableState<Int>,
    viewModel: QuestionsViewModel,
    onNextClicked: (Int) -> Unit
) {
    val choicesState = remember(question) { question.choices.toMutableList() }
    val answerState = remember(question) { mutableStateOf<Int?>(null) }
    val correctAnswerState = remember(question) { mutableStateOf<Boolean?>(null) }
    val updateAnswer: (Int) -> Unit = remember(question) {
        { index ->
            answerState.value = index
            correctAnswerState.value = choicesState[index] == question.answer
        }
    }
    val pathEffect = PathEffect.dashPathEffect(
        intervals = floatArrayOf(10f, 10f),
        phase = 0f
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppColors.mDarkPurple
    ) {
        Column(
            modifier = modifier.padding(12.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            if (questionIndex.value >= 1) ShowProgress(score = questionIndex.value)
            QuestionTracker(
                counter = questionIndex.value,
                outOf = viewModel.getTotalQuestionCount()
            )
            DrawDottedLine(pathEffect = pathEffect)
            Column {
                Text(
                    modifier = modifier
                        .padding(20.dp)
                        .align(alignment = Alignment.Start),
                    text = question.question,
                    color = AppColors.mOffWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )

                choicesState.forEachIndexed { index, answer ->
                    Answer(
                        answer = answer,
                        index = index,
                        correctAnswerState = correctAnswerState,
                        answerState = answerState,
                        onClick = { updateAnswer(it) }
                    )
                }
                Button(
                    modifier = modifier
                        .padding(3.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(34.dp),
                    colors = buttonColors(backgroundColor = AppColors.mLightBlue),
                    onClick = { onNextClicked(questionIndex.value) }
                ) {
                    Text(
                        modifier = modifier.padding(4.dp),
                        color = AppColors.mOffWhite,
                        fontSize = 17.sp,
                        text = "Next"
                    )
                }
            }
        }
    }
}

@Composable
fun ShowProgress(
    modifier: Modifier = Modifier,
    score: Int
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            AppColors.gradientPink,
            AppColors.gradientPurple
        )
    )
    val progress by remember(score) {
        mutableStateOf(score * 0.005f)
    }

    Row(
        modifier = modifier
            .padding(vertical = 3.dp, horizontal = 12.dp)
            .fillMaxWidth()
            .height(45.dp)
            .border(
                width = 4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        AppColors.mLightPurple,
                        AppColors.mLightPurple
                    )
                ),
                shape = RoundedCornerShape(34.dp)
            )
            .clip(RoundedCornerShape(50))
            .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            modifier = modifier
                .fillMaxWidth(progress)
                .background(brush = gradient),
            enabled = false,
            elevation = null,
            contentPadding = PaddingValues(1.dp),
            colors = buttonColors(
                backgroundColor = Color.Transparent,
                disabledBackgroundColor = Color.Transparent
            ),
            onClick = { }
        ) {}
    }
}

@Composable
fun QuestionTracker(
    modifier: Modifier = Modifier,
    counter: Int,
    outOf: Int
) {
    Text(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 10.dp),
        text = buildAnnotatedString {
            withStyle(style = ParagraphStyle(textIndent = TextIndent.None)) {
                withStyle(
                    style = SpanStyle(
                        color = AppColors.mLightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 27.sp
                    ),
                ) {
                    append("Answered questions $counter/")
                    withStyle(
                        style = SpanStyle(
                            color = AppColors.mLightGray,
                            fontWeight = FontWeight.Light,
                            fontSize = 14.sp
                        )
                    ) {
                        append(outOf.toString())
                    }
                }
            }
        }
    )
}

@Composable
fun DrawDottedLine(
    modifier: Modifier = Modifier,
    pathEffect: PathEffect
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 20.dp)
    ) {
        drawLine(
            color = AppColors.mLightGray,
            start = Offset(x = 0f, y = 0f),
            end = Offset(x = size.width, y = 0f),
            pathEffect = pathEffect
        )
    }
}

@Composable
fun Answer(
    modifier: Modifier = Modifier,
    answer: String,
    index: Int,
    correctAnswerState: MutableState<Boolean?>,
    answerState: MutableState<Int?>,
    onClick: (Int) -> Unit
) {
    Row(
        modifier = modifier
            .padding(vertical = 3.dp)
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(55.dp)
            .border(
                width = 4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        AppColors.mOffDarkPurple,
                        AppColors.mOffDarkPurple
                    )
                ),
                shape = RoundedCornerShape(15.dp)
            )
            .clip(
                RoundedCornerShape(
                    topStartPercent = 50,
                    topEndPercent = 50,
                    bottomEndPercent = 50,
                    bottomStartPercent = 50
                )
            )
            .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = (answerState.value == index),
            onClick = { onClick(index) },
            modifier = Modifier.padding(start = 16.dp),
            colors = RadioButtonDefaults
                .colors(
                    selectedColor = if (correctAnswerState.value == true && index == answerState.value) {
                        Color.Green.copy(alpha = 0.5f)
                    } else {
                        Color.Red.copy(alpha = 0.5f)
                    }
                )
        )

        val annotatedString = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Light,
                    color = if (correctAnswerState.value == true && index == answerState.value) {
                        Color.Green
                    } else if (correctAnswerState.value == false && index == answerState.value) {
                        Color.Red
                    } else {
                        AppColors.mOffWhite
                    },
                    fontSize = 17.sp
                )
            ) {

                append(answer)
            }
        }
        Text(text = annotatedString, modifier = Modifier.padding(6.dp))
    }
}