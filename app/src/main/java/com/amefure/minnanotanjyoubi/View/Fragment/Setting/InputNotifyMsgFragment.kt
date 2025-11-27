package com.amefure.minnanotanjyoubi.View.Fragment.Setting

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import com.amefure.minnanotanjyoubi.R
import com.amefure.minnanotanjyoubi.View.Compose.BackUpperBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.amefure.minnanotanjyoubi.View.Compose.ActionDownBar
import com.amefure.minnanotanjyoubi.View.Compose.components.CustomText
import com.amefure.minnanotanjyoubi.ViewModel.InputNotifyMsgViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.SharedFlow

@AndroidEntryPoint
class InputNotifyMsgFragment : Fragment() {
    private val viewModel: InputNotifyMsgViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                InputNotifyMsgScreenRoot(
                    uiEvent = viewModel.uiEvent,
                    hideKeyboard = {
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view?.windowToken, 0)
                    },
                    onSaved = {
                        android.app.AlertDialog.Builder(context)
                            .setTitle("Success")
                            .setMessage("通知メッセージを変更しました。")
                            .setOnDismissListener {
                                parentFragmentManager.popBackStack()
                            }
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }.show()
                    },
                    editingMsg = viewModel.editingMsg,
                    onMsgChange = viewModel::updateEditingMsg,
                    onSave = viewModel::saveNotifyMsg,
                    onBack = {
                        parentFragmentManager.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun InputNotifyMsgScreenRoot(
    uiEvent: SharedFlow<InputNotifyMsgViewModel.UiEvent>,
    hideKeyboard: () -> Unit,
    onSaved: () -> Unit,
    editingMsg: String,
    onMsgChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorResource(id = R.color.thema_gray_light)
        ) {
            // UI イベントを受け取る
            LaunchedEffect(Unit) {
                uiEvent.collect { event ->
                    when (event) {
                        InputNotifyMsgViewModel.UiEvent.HideKeyboard -> hideKeyboard
                        InputNotifyMsgViewModel.UiEvent.Saved -> onSaved
                    }
                }
            }

            InputNotifyMsgScreen(
                editingMsg = editingMsg,
                onMsgChange = onMsgChange,
                onSave = onSave,
                onBack = onBack
            )
        }
    }
}


@Composable
private fun InputNotifyMsgScreen(
    editingMsg: String,
    onMsgChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackUpperBar(onBack)

        CustomText(
            stringResource(R.string.edit_notify_msg_title),
            color = Color.White,
            modifier = Modifier
                .padding(vertical = 10.dp)
        )

        Spacer(
            modifier = Modifier
                .weight(1f)
        )

        TextField(
            value = editingMsg,
            onValueChange = onMsgChange,
            singleLine = true,
            maxLines = 1,
            placeholder = { Text(stringResource(R.string.edit_notify_msg_hint)) },
            modifier = Modifier
                .fillMaxWidth()
                 .padding(horizontal = 20.dp)
        )

        CustomText(
            stringResource(R.string.edit_notify_msg_desc),
            color = Color.White,
            maxLines = 2,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .padding(horizontal = 20.dp)
        )

        Spacer(
            modifier = Modifier
                .weight(1f)
        )


        ActionDownBar(onClick = onSave)
    }
}

@Preview(showBackground = false)
@Composable
private fun InputNotifyMsgScreenPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorResource(id = R.color.thema_gray_light)
        ) {
            InputNotifyMsgScreen(
                editingMsg = "プレビューのテキスト",
                onMsgChange = {},
                onSave = {},
                onBack = {}
            )
        }
    }
}