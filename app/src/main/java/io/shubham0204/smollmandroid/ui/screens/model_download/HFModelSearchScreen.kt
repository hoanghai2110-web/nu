/*
 * Copyright (C) 2025 Shubham Panchal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shubham0204.smollmandroid.ui.screens.model_download

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Search
import io.shubham0204.hf_model_hub_api.HFModelSearch
import io.shubham0204.smollmandroid.R
import io.shubham0204.smollmandroid.ui.components.AppBarTitleText
import io.shubham0204.smollmandroid.ui.theme.SmolLMAndroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HFModelDownloadScreen(
    viewModel: DownloadModelsViewModel,
    onBackClicked: () -> Unit,
    onModelClick: (String) -> Unit,
) {
    SmolLMAndroidTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { AppBarTitleText(stringResource(R.string.download_model_hf_screen_title)) },
                    navigationIcon = {
                        IconButton(onClick = { onBackClicked() }) {
                            Icon(
                                FeatherIcons.ArrowLeft,
                                contentDescription = "Navigate Back",
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    },
                )
            },
        ) { innerPadding ->
            Column(
                modifier =
                    Modifier
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.surface),
            ) {
                var query by remember { mutableStateOf("") }
                TextField(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    value = query,
                    onValueChange = { query = it },
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        TextFieldDefaults.colors(
                            disabledTextColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        ),
                    leadingIcon = {
                        Icon(
                            imageVector = FeatherIcons.Search,
                            contentDescription =
                                "Search for models",
                        )
                    },
                    placeholder = {
                        Text(text = stringResource(R.string.download_model_hf_search_label))
                    },
                    keyboardOptions =
                        KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences),
                    singleLine = true,
                )
                ModelList(query, viewModel, onModelClick)
            }
        }
    }
}

@Composable
private fun ModelList(
    query: String,
    viewModel: DownloadModelsViewModel,
    onModelClick: (String) -> Unit,
) {
    val models = viewModel.getModels(query).collectAsLazyPagingItems()
    LazyColumn(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        items(count = models.itemCount) { index ->
            models[index]?.let { model ->
                ModelListItem(model, onModelClick = onModelClick)
            }
        }
    }
}

@Composable
private fun ModelListItem(
    model: HFModelSearch.ModelSearchResult,
    onModelClick: (String) -> Unit,
) {
    val modelAuthor = model.id.split("/")[0]
    val modelName = model.id.split("/")[1]
    Column(
        modifier =
            Modifier
                .clickable { onModelClick(model.id) }
                .padding(8.dp)
                .fillMaxWidth(),
    ) {
        Text(
            text = modelAuthor,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = modelName,
            style = MaterialTheme.typography.labelSmall,
        )
        LazyRow {
            items(
                model.tags.filter { !listOf("GGUF", "conversational").contains(it) },
            ) {
                Text(
                    modifier =
                        Modifier
                            .padding(horizontal = 2.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(4.dp),
                            ).padding(horizontal = 2.dp),
                    text = it,
                    fontSize = 8.sp,
                )
            }
        }
    }
    HorizontalDivider(modifier = Modifier.fillMaxWidth())
}