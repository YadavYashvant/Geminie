package com.example.imgc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.ImageResult
import com.example.imgc.animations.AnimatedPreloaderDog
import com.example.imgc.animations.AnimatedPreloaderLoading
import com.google.ai.client.generativeai.GenerativeModel
import com.example.imgc.ui.theme.ImGCTheme
import com.example.imgc.ui.theme.blackV
import com.example.imgc.ui.theme.blueV
import com.example.imgc.ui.theme.nunitofont
import com.example.imgc.ui.theme.redV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //WindowCompat.setDecorFitsSystemWindows(window,false)

        setContent {
            ImGCTheme {
                Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                ) {
                    val generativeModel = GenerativeModel(
                        modelName = "gemini-1.5-flash",
//                            modelName = "gemini-pro-vision",
                            apiKey = BuildConfig.apiKey
                    )
                    val viewModel = GetImgContextViewmodel(generativeModel)
                    GetImgContextRoute(viewModel)
                }
            }
        }
    }
}

@Composable
internal fun GetImgContextRoute(
        getImgContextViewmodel: GetImgContextViewmodel = viewModel()
) {
    val summarizeUiState by getImgContextViewmodel.uiState.collectAsState()

    GetImgContextScreen(summarizeUiState, onSummarizeClicked = { inputImage,prompt, isAdv->
        getImgContextViewmodel.findContextOfImage(inputImage, prompt, isAdv)
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetImgContextScreen(
        uiState: SummarizeUiState = SummarizeUiState.Initial,
        onSummarizeClicked: (Bitmap, String, Boolean) -> Unit = { bitmap: Bitmap, s: String ,isAdv: Boolean ->}
) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(color = blackV)
                /*.paint(
                    painterResource(id = R.drawable.background),
                    contentScale = ContentScale.FillBounds
                ),*/
        ) {

            val coroutineScope = rememberCoroutineScope()
            var bitmap by remember { mutableStateOf<Bitmap?>(null) }
            val mContext = LocalContext.current

            var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
            val photoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia(),
                onResult = {
                    selectedImageUri = it
                })

            LaunchedEffect(selectedImageUri) {
                if (selectedImageUri != null) {
                    // Passing the coroutineScope to loadBitmapFromUri
                    loadBitmapFromUri(mContext, selectedImageUri!!, coroutineScope) { loadedBitmap ->
                        bitmap = loadedBitmap
                    }
                }
            }

            Column(
                modifier = Modifier
                    //.fillMaxSize()

            ) {

                    Text(
                        text = "Choose a random photo",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = redV
                        ),
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.size(20.dp))

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = redV,
                            contentColor = Color.White
                        )
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(imageVector = (Icons.Default.Add), contentDescription = null)

                            Text(
                                text = "Choose Photo",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            )
                        }

                    }

                    Spacer(modifier = Modifier.size(20.dp))

                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                        model = selectedImageUri,
                        contentDescription = null
                    )

                    val checkedState = remember { mutableStateOf(false) }
                    var value by remember { mutableStateOf("") }
                    val onValueChange: (String) -> Unit = { value = it }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checkedState.value,
                            modifier = Modifier.padding(horizontal = 16.dp),

                            colors = CheckboxDefaults.colors(
                                checkedColor = redV,
                                uncheckedColor = Color.White,
                                checkmarkColor = Color.White
                            ),
                            onCheckedChange = { checkedState.value = it },
                        )

                        Text(
                            text = "Advanced Mode",
                            modifier = Modifier.align(Alignment.CenterVertically),
                            color = Color.White
                        )
                    }

                    if (checkedState.value) {
                        TextField(
                            value = value,
                            onValueChange = onValueChange,
                            shape = MaterialTheme.shapes.medium,
                            placeholder = {
                                Text(
                                    text = "Ask anthing about the image!",
                                    //fontFamily = com.example.codev.spacefamily,
                                    textAlign = TextAlign.Center,
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .defaultMinSize(minHeight = 56.dp),
                            minLines = 2,
                            colors = TextFieldDefaults.textFieldColors(
                                unfocusedIndicatorColor = blueV,
                                focusedIndicatorColor = Color.Transparent,
                            )
                        )
                    }

                    Spacer(modifier = Modifier.size(8.dp))

                    Button(
                        onClick = {
                            onSummarizeClicked(bitmap!!, value, checkedState.value)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .height(56.dp)
                            .padding(bottom = 12.dp)
                        ,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = blueV,
                            contentColor = Color.White
                        )
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "Get Context",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            )
                        }

                    }
                }

            when (uiState) {
                SummarizeUiState.Initial -> {
                    AnimatedPreloaderDog(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(16.dp))

                    )
                }

                SummarizeUiState.Loading -> {

                    AnimatedPreloaderLoading(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                            .height(250.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }

                is SummarizeUiState.Success -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .padding(horizontal = 20.dp),

                    ) {
                        Row(modifier = Modifier.padding(all = 8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = "Person Icon",
                                tint = redV
                            )
                            Text(
                                text = uiState.outputText,
                                fontFamily = nunitofont,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }

                is SummarizeUiState.Error -> {
                    Text(
                        text = uiState.errorMessage,
                        color = Color.Red,
                        modifier = Modifier.padding(all = 8.dp)
                    )
                }

                else -> {
                    Log.d("GetImgContextScreen", "Unknown state: $uiState")
                }
            }
        }
}

suspend fun loadBitmapFromUri(
    context: Context,
    uri: Uri,
    coroutineScope: CoroutineScope,
    onBitmapLoaded: (Bitmap) -> Unit
) {
    withContext(coroutineScope.coroutineContext + Dispatchers.IO) {
        try {
            val request = ImageRequest.Builder(context)
                .data(uri)
                .build()

            val result: ImageResult = context.imageLoader.execute(request)
            val drawable = result.drawable

            if (drawable != null) {
                val bitmap = (drawable as BitmapDrawable).bitmap
                onBitmapLoaded(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
