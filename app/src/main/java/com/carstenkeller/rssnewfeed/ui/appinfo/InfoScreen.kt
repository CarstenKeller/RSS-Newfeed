package com.carstenkeller.rssnewfeed.ui.appinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carstenkeller.rssnewfeed.BuildConfig
import com.carstenkeller.rssnewfeed.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    viewModel: InfoViewModel,
    onBack: () -> Unit,
) {
    val lines by viewModel.planLines.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.info_title)) },
                colors = com.carstenkeller.rssnewfeed.ui.theme.brandedTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    text = stringResource(R.string.version_build_label, BuildConfig.VERSION_NAME, BuildConfig.BUILD_NUMBER.toString()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.info_plan_heading),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
            }

            items(lines) { line -> PlanLineRow(line) }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun PlanLineRow(line: PlanLine) {
    when (line) {
        is PlanLine.Blank -> Spacer(Modifier.height(8.dp))
        is PlanLine.Heading -> Text(
            text = line.text,
            style = if (line.level == 1) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        )
        is PlanLine.Bullet -> Text(
            text = "•  ${line.text}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 2.dp),
        )
        is PlanLine.Paragraph -> Text(
            text = line.text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 2.dp),
        )
        is PlanLine.Todo -> TodoRow(line)
    }
}

@Composable
private fun TodoRow(todo: PlanLine.Todo) {
    val backgroundColor = if (todo.isNextStep) Color(0xFFFFF3CD) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .padding(vertical = 4.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            todo.done -> Icon(
                Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.status_done_cd),
                tint = Color(0xFF2E7D32),
            )
            todo.isNextStep -> Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.status_next_cd),
                tint = Color(0xFFB8860B),
            )
            else -> Icon(
                Icons.Filled.RadioButtonUnchecked,
                contentDescription = stringResource(R.string.status_open_cd),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = todo.text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (todo.isNextStep) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
