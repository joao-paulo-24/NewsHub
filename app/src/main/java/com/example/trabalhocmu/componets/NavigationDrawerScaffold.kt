package com.example.trabalhocmu.componets

//import com.example.trabalhocmu.generateDummyPosts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.trabalhocmu.CommentsScreen
import com.example.trabalhocmu.FirestorePostViewModel
import com.example.trabalhocmu.FirestoreScreen
import com.example.trabalhocmu.LoginScreen
import com.example.trabalhocmu.LoginViewModel
import com.example.trabalhocmu.PostListScreen
import com.example.trabalhocmu.RegisterScreen
import com.example.trabalhocmu.database.Post
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyNavigatonDrawer() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navController = rememberNavController()
    val drawerItemList = prepareNavigationDrawerItems()
    var selectedItem by remember { mutableStateOf(drawerItemList[0]) }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                drawerItemList.forEach { item ->
                    MyDrawerItem(
                        item,
                        selectedItem,
                        { selectedItem = it },
                        navController,
                        drawerState
                    )
                }
            }
        },
        content = { MyScaffold(drawerState = drawerState, navController = navController) }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDrawerItem(
    item: NavigationDrawerData,
    selectedItem: NavigationDrawerData,
    updateSelected: (i: NavigationDrawerData) -> Unit,
    navController: NavHostController,
    drawerState: DrawerState
) {
    val coroutineScope = rememberCoroutineScope()
    NavigationDrawerItem(
        icon = { Icon(imageVector = item.icon, contentDescription = null) },
        label = { Text(text = item.label) },
        selected = (item == selectedItem),
        onClick = {
            coroutineScope.launch {
                navController.navigate(item.label)
                drawerState.close()
            }
            updateSelected(item)
        },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScaffold(drawerState: DrawerState, navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            MyTopAppBar {
                coroutineScope.launch {
                    drawerState.open()
                }
            }
        },
        content = { padding ->
            Column(modifier = Modifier.padding(padding)) {
                MyScaffoldContent(navController)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(onNavIconClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = "NewsHub") },
        navigationIcon = {
            IconButton(
                onClick = {
                    onNavIconClick()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Items"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    )
}

@Composable
fun MyScaffoldContent(navController: NavHostController) {
    val viewModel: FirestorePostViewModel = viewModel()
    val viewModelReg: LoginViewModel = viewModel()
    NavHost(navController = navController, startDestination = "HomeScreen") {
        composable("HomeScreen") {
            PostListScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable("Profile") { LoginScreen(navController) }
        composable("Create Post") { FirestoreScreen(navController) }
        composable("Register") { RegisterScreen(navController = navController, viewModel = viewModelReg) }
        composable(
            route = "CommentsScreen/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            var post by remember { mutableStateOf<Post?>(null) }
            LaunchedEffect(postId) {
                postId?.let {
                    val fetchedPost = viewModel.getOnePost(it)
                    post = fetchedPost
                }
            }
            post?.let { CommentsScreen(post = it, viewModel = viewModel, navController = navController) }
        }
    }
}


private fun prepareNavigationDrawerItems(): List<NavigationDrawerData> {
    val drawerItemsList = arrayListOf<NavigationDrawerData>()
    drawerItemsList.add(NavigationDrawerData(label = "HomeScreen", icon = Icons.Filled.Home))
    drawerItemsList.add(NavigationDrawerData(label = "Create Post", icon = Icons.Filled.Add))
    drawerItemsList.add(NavigationDrawerData(label = "Profile", icon = Icons.Filled.Person))
    return drawerItemsList
}

data class NavigationDrawerData(val label: String, val icon: ImageVector)
