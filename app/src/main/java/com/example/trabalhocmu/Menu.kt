package com.example.trabalhocmu


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.trabalhocmu.database.Post
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
        }
    }

}

var lastNotificationId = 0


/**
 * Tela que exibe uma lista de posts.
 *
 * @param viewModel O ViewModel responsável por fornecer os dados dos posts.
 * @param navController O controller de navegação usado para navegar para outros screens.
 */
@Composable
fun PostListScreen(viewModel: FirestorePostViewModel, navController: NavController) {

    val posts = viewModel.postsRoomLiveData.observeAsState(emptyList()).value

    LazyColumn {
        itemsIndexed(posts) { _, post ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, Color.Gray)
            ) {
                PostItem(post = post, viewModel = viewModel, navController = navController)
            }
        }
    }

}

/**
 * Componente Composable que representa um item de post, que está a ser exibido na PostListScreen.
 *
 * @param post O objeto Post a ser exibido neste item.
 * @param viewModel O ViewModel que fornece lógica para manipulação de posts.
 * @param navController O controller de navegação usado para navegar para outros screens.
 */
@Composable
fun PostItem(post: Post, viewModel: FirestorePostViewModel, navController : NavController) {



    val context = LocalContext.current

    //viewModel.delete(post)

    val (upvotes, setUpvotes) = remember { mutableStateOf(post.upvotes) }
    val (downvotes, setDownvotes) = remember { mutableStateOf(post.downvotes) }

    val user = Firebase.auth.currentUser

    val permissionGiven = remember {
        mutableStateOf(0)
    }

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
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
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    val (userLat, setUserLat) = remember { mutableStateOf(0.0) }
    val (userLon, setUserLon) = remember { mutableStateOf(0.0) }

    if (permissionGiven.value == 2) {
        DisposableEffect(Unit) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    setUserLat(it.latitude)
                    setUserLon(it.longitude)
                } ?: run {
                    setUserLat(0.0)
                    setUserLon(0.0)
                }
            }.addOnFailureListener {
                setUserLat(0.0)
                setUserLon(0.0)
            }

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10,
            ).setMinUpdateIntervalMillis(10).build();

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locations: LocationResult) {
                    for (location in locations.locations) {
                        setUserLat(location.latitude)
                        setUserLon(location.longitude)

                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            onDispose { fusedLocationClient.removeLocationUpdates(locationCallback) }


        }


        val distance = distanceBetweenCoordinates(
            post.lat, post.lon,
            userLat, userLon
        )
        if (distance < 0.1) {
            lastNotificationId++
            val channelId = "channel_id"
            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.newshubicon)
                .setContentTitle("Evento próximo")
                .setContentText(post.title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                val name = "Evento próximo"
                val descriptionText = post.title
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel("channel_id", name, importance).apply {
                    description = descriptionText
                }

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)

                with(NotificationManagerCompat.from(context)) {
                    notify(lastNotificationId, notificationBuilder.build())
                }
            }
        }


    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = "Título: ${post.title}", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Texto: ${post.description}")
        Spacer(modifier = Modifier.height(4.dp))
        post.photo?.let { url ->
            AsyncImage(
                model = post.photo,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Código Postal: ${post.address}")
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Coordenadas: ${post.lat}, ${post.lon}")
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Upvotes: ${upvotes}")
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Downvotes: ${downvotes}")
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Categoria: ${post.category}")
        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                if (user != null) {
                    if (!post.userUp.contains(user.email)) {
                        setUpvotes(upvotes + 1)
                        if (post.userDown.contains(user.email)){ setDownvotes(downvotes - 1) }
                        viewModel.upvotePost(post = post, viewModel = viewModel)
                    } else if (post.userUp.contains(user.email)) {
                        setUpvotes(upvotes - 1)
                        viewModel.removeUpvotePost(post = post, viewModel = viewModel)
                    }
                }
            },
            contentPadding = PaddingValues(),
            colors = ButtonDefaults.buttonColors()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Button(
            onClick = {
                if (user != null) {
                    if (!post.userDown.contains(user.email)) {
                        setDownvotes(downvotes + 1)
                        if (post.userUp.contains(user.email)){setUpvotes(upvotes - 1)}
                        viewModel.downvotePost(post = post, viewModel = viewModel)
                    } else if (post.userDown.contains(user.email)) {
                        setDownvotes(downvotes - 1)
                        viewModel.removeDownvotePost(post = post, viewModel = viewModel)
                    }
                }
            },
            contentPadding = PaddingValues(),
            colors = ButtonDefaults.buttonColors()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        var selectedRating by remember { mutableStateOf(1) }

        RadioGroup(
            selectedRating = selectedRating,
            onSelectedRatingChange = { selectedRating = it }
        )

        Button(
            onClick = {
                if (user != null) {
                    if (!post.userRate.contains(user.email)) {
                        viewModel.ratePost(post, selectedRating, viewModel)
                    }
                    else if (post.userRate.contains(user.email)){
                        Toast.makeText(context, "Já avaliou este post!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Avaliar Post")
        }
        Button(
            onClick = {
                post?.Id?.let { postId ->
                    navController.navigate("CommentsScreen/$postId")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver Comentários")
        }
    }

}

/**
 * Screen que exibe os comentários de um post específico.
 *
 * @param post O objeto Post cujos comentários serão exibidos.
 * @param viewModel O ViewModel que fornece lógica para manipulação de posts.
 * @param navController O controller de navegação usado para navegar para outros screens.
 */
@Composable
fun CommentsScreen(post: Post, viewModel: FirestorePostViewModel, navController: NavController) {
    val context = LocalContext.current

    var commentText by remember { mutableStateOf(TextFieldValue()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Post: ${post.title}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = Color.Black,
        )
        Text(
            text = "Description: ${post.description}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = Color.Black,
        )
        Text(
            text = "Comments:",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = Color.Black,
        )
        post.comments.forEach { comment ->
            CommentItem(comment = comment)
        }
        Spacer(modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            label = { Text("Enter your comment") }
        )


        Button(
            onClick = {
                if (!commentText.text.equals("")) {
                    viewModel.addComent(post, commentText.text, viewModel)
                } else if (commentText.text.equals("")) {
                    Toast.makeText(context, "Escreva um comentário!", Toast.LENGTH_SHORT)
                        .show()
                }
                post?.Id?.let { postId ->
                    navController.navigate("CommentsScreen/$postId")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Add Comment")

        }
        Button(
            onClick = {
                    navController.navigate("HomeScreen")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Voltar")

        }
    }
}

/**
 * Componente Composable que representa um item de comentário.
 *
 * @param comment O texto correspondente ao email do utilizador que comentou, seguido do comentário
 * a ser exibido.
 */
@Composable
fun CommentItem(comment: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "${comment}",
            color = Color.Black,
        )
    }
}

/**
 * Componente Composable que representa um grupo de botões de rádio para seleção de classificação.
 *
 * @param selectedRating A classificação selecionada atualmente.
 * @param onSelectedRatingChange Função de callback que é chamada quando a classificação
 * selecionada é alterada.
 */
@Composable
fun RadioGroup(
    selectedRating: Int,
    onSelectedRatingChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        (1..5).forEach { rating ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = rating == selectedRating,
                    onClick = { onSelectedRatingChange(rating) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    text = rating.toString(),
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clickable { onSelectedRatingChange(rating) }
                )
            }
        }
    }
}

/**
 * Calcula a distância em quilômetros entre duas coordenadas geográficas (latitude e longitude)
 * usando a fórmula de haversine.
 *
 * @param lat1 A latitude da primeira coordenada em graus decimais.
 * @param lon1 A longitude da primeira coordenada em graus decimais.
 * @param lat2 A latitude da segunda coordenada em graus decimais.
 * @param lon2 A longitude da segunda coordenada em graus decimais.
 * @return A distância em quilômetros entre as duas coordenadas.
 */
fun distanceBetweenCoordinates(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val R = 6371
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val latRad1 = Math.toRadians(lat1)
    val latRad2 = Math.toRadians(lat2)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            sin(dLon / 2) * sin(dLon / 2) * cos(latRad1) * cos(latRad2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}






