/*
 * Copyright (C) 2024 Shubham Panchal
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

package io.shubham0204.smollmandroid.ui.screens.manage_tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import compose.icons.FeatherIcons
import compose.icons.feathericons.Check
import io.shubham0204.smollmandroid.R
import io.shubham0204.smollmandroid.ui.screens.chat.SelectModelsList

@Composable
fun EditTaskDialog(viewModel: TasksViewModel) {
    val selectedTask by remember { viewModel.selectedTaskState }
    selectedTask?.let { task ->
        var taskName by remember { mutableStateOf(task.name) }
        var systemPrompt by remember { mutableStateOf(task.systemPrompt) }
        var selectedModel by remember {
            mutableStateOf(viewModel.modelsRepository.getModelFromId(task.modelId))
        }
        var isModelListDialogVisible by remember { mutableStateOf(false) }
        val modelsList = viewModel.modelsRepository.getAvailableModelsList()
        val (focusRequestor) = FocusRequester.createRefs()
        val keyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(selectedTask) {
            taskName = task.name
            systemPrompt = task.systemPrompt
        }
        var showEditTaskDialog by remember { viewModel.showEditTaskDialogState }
        if (showEditTaskDialog) {
            Surface {
                Dialog(onDismissRequest = {
                    showEditTaskDialog = false
                }) {
                    Column(
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.task_edit_task_title),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            value = taskName,
                            onValueChange = { taskName = it },
                            label = { Text(stringResource(R.string.task_create_task_name)) },
                            keyboardOptions =
                                KeyboardOptions.Default.copy(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next,
                                ),
                            keyboardActions =
                                KeyboardActions(
                                    onNext = {
                                        focusRequestor.requestFocus()
                                    },
                                ),
                        )

                        TextField(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).focusRequester(focusRequestor),
                            value = systemPrompt,
                            onValueChange = { systemPrompt = it },
                            label = { Text(stringResource(R.string.task_create_task_sys_prompt)) },
                            keyboardOptions =
                                KeyboardOptions.Default.copy(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Done,
                                ),
                            keyboardActions =
                                KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                    },
                                ),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .border(width = 1.dp, Color.DarkGray)
                                    .clickable { isModelListDialogVisible = true }
                                    .padding(8.dp),
                            text =
                                if (selectedModel == null) {
                                    stringResource(R.string.task_create_task_select_model)
                                } else {
                                    selectedModel!!
                                        .name
                                },
                        )

                        if (isModelListDialogVisible) {
                            SelectModelsList(
                                onDismissRequest = { isModelListDialogVisible = false },
                                modelsList = modelsList,
                                onModelListItemClick = { model ->
                                    isModelListDialogVisible = false
                                    selectedModel = model
                                },
                                onModelDeleteClick = { // Not applicable, as showModelDeleteIcon is set to false
                                },
                                showModelDeleteIcon = false,
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            enabled = taskName.isNotBlank() && systemPrompt.isNotBlank(),
                            onClick = {
                                selectedModel?.let { model ->
                                    viewModel.updateTask(
                                        task.copy(
                                            name = taskName,
                                            systemPrompt = systemPrompt,
                                            modelId = model.id,
                                            modelName = model.name,
                                        ),
                                    )
                                }
                                showEditTaskDialog = false
                            },
                        ) {
                            Icon(FeatherIcons.Check, contentDescription = "Update")
                            Text(stringResource(R.string.task_edit_task_update))
                        }
                    }
                }
            }
        }
    }
}
