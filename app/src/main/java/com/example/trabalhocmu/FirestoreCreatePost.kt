package com.example.trabalhocmu

import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.trabalhocmu.database.NominatimAPI
import com.example.trabalhocmu.database.Post
import com.example.trabalhocmu.database.PostCategory
import com.example.trabalhocmu.database.RetrofitHelper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.storage.storage
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Math.random
import java.util.UUID
import kotlin.math.nextDown

/**
 * Screen para adicionar um novo post ao Firestore.
 *
 * @param navController O NavController para navegação entre screens.
 */
@Composable
fun FirestoreScreen(navController: NavController) {
    val id = rememberSaveable { mutableStateOf("") }
    val title = rememberSaveable { mutableStateOf("") }
    val description = rememberSaveable { mutableStateOf("") }
    val photo = rememberSaveable { mutableStateOf<String?>(null) }
    val address = rememberSaveable { mutableStateOf<String?>(null) }
    val lat = rememberSaveable { mutableStateOf<Double>(0.0) }
    val lon = rememberSaveable { mutableStateOf<Double>(0.0) }
    val lifespan = rememberSaveable { mutableStateOf(30) }
    val category = rememberSaveable { mutableStateOf(PostCategory.NORMAL) }
    val viewModel: FirestorePostViewModel = viewModel()
    val list = viewModel.postsLiveData.observeAsState()
    val user = Firebase.auth.currentUser

    if (user != null) {
        Column {
            AddPost(
                id = id.value,
                onIdChange = { id.value = it },
                title = title.value,
                onTitleChange = { title.value = it },
                description = description.value,
                onDescriptionChange = { description.value = it },
                photo = photo.value,
                onPhotoChange = { photo.value = it },
                address = address.value,
                onAddressChange = { address.value = it },
                lat = lat.value,
                onLatChange = { lat.value = it },
                lon = lon.value,
                onLonChange = { lon.value = it },
                lifespan = lifespan.value,
                onLifespanChange = { lifespan.value = it },
                category = category.value,
                onCategoryChange = { category.value = it },
                onSaveClick = {
                    viewModel.insertPost(
                        Post(
                            id.value,
                            title.value,
                            description.value,
                            photo.value,
                            address.value,
                            lat.value,
                            lon.value,
                            lifespan.value,
                            category.value,
                            0,
                            0,
                            emptyList(),
                            emptyList(),
                            emptyList(),
                            emptyList(),
                            emptyList()

                        )
                    )
                    viewModel.insert(
                        Post(
                            id.value,
                            title.value,
                            description.value,
                            photo.value,
                            address.value,
                            lat.value,
                            lon.value,
                            lifespan.value,
                            category.value,
                            0,
                            0,
                            emptyList(),
                            emptyList(),
                            emptyList(),
                            emptyList(),
                            emptyList()
                        )
                    )
                    navController.navigate("HomeScreen")
                }
            )
        }

    } else {
        NotLoggedInScreen(navController)
    }
}

/**
 * Composable para adicionar um novo post.
 *
 * @param id ID do post.
 * @param onIdChange Função de callback para atualizar o ID do post.
 * @param title Título do post.
 * @param onTitleChange Função de callback para atualizar o título do post.
 * @param description Descrição do post.
 * @param onDescriptionChange Função de callback para atualizar a descrição do post.
 * @param photo URL da foto do post.
 * @param onPhotoChange Função de callback para atualizar a URL da foto do post.
 * @param address Endereço do post.
 * @param onAddressChange Função de callback para atualizar o endereço do post.
 * @param lat Latitude do post.
 * @param onLatChange Função de callback para atualizar a latitude do post.
 * @param lon Longitude do post.
 * @param onLonChange Função de callback para atualizar a longitude do post.
 * @param lifespan Duração do post em dias.
 * @param onLifespanChange Função de callback para atualizar a duração do post.
 * @param category Categoria do post.
 * @param onCategoryChange Função de callback para atualizar a categoria do post.
 * @param onSaveClick Função de callback para salvar o post.
 */
@Composable
fun AddPost(
    id: String,
    onIdChange: (String) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    photo: String?,
    onPhotoChange: (String?) -> Unit,
    address: String?,
    onAddressChange: (String?) -> Unit,
    lat: Double,
    onLatChange: (Double) -> Unit,
    lon: Double,
    onLonChange: (Double) -> Unit,
    lifespan: Int,
    onLifespanChange: (Int) -> Unit,
    category: PostCategory,
    onCategoryChange: (PostCategory) -> Unit,
    onSaveClick: (Post) -> Unit
) {
    val context = LocalContext.current

    var user = Firebase.auth.currentUser

    val nominatim = RetrofitHelper.getInstance().create(NominatimAPI::class.java)

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { imageUri ->
                val storageRef = Firebase.storage.reference.child("images/${UUID.randomUUID()}.jpg")

                Log.d("TAG", "URI da imagem: $imageUri")

                storageRef.putFile(imageUri)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            storageRef.downloadUrl
                                .addOnSuccessListener { downloadUri ->
                                    val photoUrl = downloadUri.toString()
                                    onPhotoChange(photoUrl)
                                    Toast.makeText(
                                        context,
                                        "Imagem enviada com sucesso!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(
                                        "TAG",
                                        "Falha ao obter URL da imagem: ${exception.message}",
                                        exception
                                    )
                                    Toast.makeText(
                                        context,
                                        "Falha ao obter URL da imagem",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            Log.e(
                                "TAG",
                                "Falha ao enviar imagem: ${task.exception?.message}",
                                task.exception
                            )
                            Toast.makeText(context, "Falha ao enviar imagem", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            }
        }

    val permissionGiven = remember {
        mutableStateOf(0)
    }

    val ctx = LocalContext.current

    if (ActivityCompat.checkSelfPermission(
            ctx,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            ctx,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        permissionGiven.value = 2
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                permissionGiven.value += 1
            }
        }

    LaunchedEffect(key1 = "Permission") {
        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissionLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    if (permissionGiven.value == 2) {
        DisposableEffect(Unit) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onLatChange(location.latitude)
                    onLonChange(location.longitude)
                } else {
                    onLatChange(0.0)
                    onLonChange(0.0)
                }
            }.addOnFailureListener {
                onLatChange(0.0)
                onLonChange(0.0)
            }

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000,
            ).setMinUpdateIntervalMillis(30000).build();

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locations: LocationResult) {
                    for (location in locations.locations) {
                        onLatChange(location.latitude)
                        onLonChange(location.longitude)
                        nominatim.reverseGeocode(location.latitude, location.longitude)
                            .enqueue(object : Callback<JsonObject> {
                                override fun onResponse(
                                    call: Call<JsonObject>,
                                    response: Response<JsonObject>
                                ) {
                                    val jsonObject = response.body()
                                    val addressObject = jsonObject?.getAsJsonObject("address")
                                    val postalCode = addressObject?.get("postcode")?.toString()
                                    if (postalCode != null) {
                                        onAddressChange(postalCode)
                                    } else {
                                        onAddressChange("")
                                    }
                                }

                                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                                    Log.e("API Request", "Erro: ${t.message}", t)
                                }
                            })
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            onDispose { fusedLocationClient.removeLocationUpdates(locationCallback) }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Add Post")
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(text = "Title") }
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(text = "Description") }
        )
        Button(onClick = {
            galleryLauncher.launch("image/*")
        }) {
            Text("Select Image")
        }
        OutlinedTextField(
            value = lifespan.toString(),
            onValueChange = { onLifespanChange(it.toIntOrNull() ?: 30) },
            label = { Text(text = "Lifespan (days)") }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 5.dp)
        ) {
            PostCategory.values().forEach { categoryItem ->
                Row(
                    modifier = Modifier
                        .padding(3.dp)
                        .clickable {
                            onCategoryChange(categoryItem)
                        }
                ) {
                    RadioButton(
                        selected = (categoryItem == category),
                        onClick = {
                            onCategoryChange(categoryItem)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = categoryItem.name)
                }
            }
        }

        Button(onClick = {
            if (title.isNotBlank() && description.isNotBlank()) {
                val newId = "${random().nextDown().toString()}-${user?.email}"
                onIdChange(newId)
                val post =
                    Post(
                        id,
                        title,
                        description,
                        photo,
                        address,
                        lat,
                        lon,
                        lifespan,
                        category,
                        0,
                        0,
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        emptyList()
                    )
                onSaveClick(post)
            } else {
                Toast.makeText(
                    context,
                    "Please enter both title and description",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }) {
            Text("Save Post")
        }
    }
}

/**
 * Composable para exibir a tela quando o utilizador não está logado, dado que este terá de fazer login
 * para criar um post.
 *
 * @param navController NavController para navegação entre screens.
 */
@Composable
fun NotLoggedInScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login not completed",
            style = TextStyle(fontSize = 24.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = {
                navController.navigate("Profile")
            }
        ) {
            Text(text = "Login")
        }
    }
}



