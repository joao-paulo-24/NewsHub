package com.example.trabalhocmu

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/**
 * Screen de registo que permite aos utilizadores criarem uma nova conta.
 *
 * @param navController O controller de navegação para navegar para outras screens.
 * @param viewModel O ViewModel responsável pela lógica de registo de utilizador.
 */
@Composable
fun RegisterScreen(navController: NavController, viewModel: LoginViewModel) {
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                viewModel.register(emailState.value, passwordState.value)
                navController.navigate("Profile")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
    }
}

/**
 * Tela de login que permite aos utilizadores entrar nas suas contas.
 *
 * @param navController O controller de navegação para navegar para outras screens.
 */
@Composable
fun LoginScreen(navController: NavController){
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val viewModel:LoginViewModel = viewModel()
    val authStatus = viewModel.authState.observeAsState()
    val user = Firebase.auth.currentUser

    if (authStatus.value == LoginViewModel.AuthStatus.NOLOGGIN && user == null) {
        LoginFields(email = email.value, password = password.value,
            onEmailChange = { email.value = it },
            onPasswordChange = { password.value = it },
            onLoginClick = { viewModel.login(email.value, password.value) },
            navController = navController
        )
        return
    }
    if (authStatus.value == LoginViewModel.AuthStatus.LOGGED || user != null){
        LogoutFields (
            onLogoutClick = {
                viewModel.logout()
                navController.navigate("Profile")
            },
            navController = navController
        )
    }
}

/**
 * Componente que exibe campos de entrada para e-mail e password e um botão de login, ou um botão de
 * logout caso o utilizador esteja autenticado.
 *
 * @param email O e-mail do utilizador.
 * @param password A password do utilizador.
 * @param onEmailChange Função callback chamada quando o e-mail é alterado.
 * @param onPasswordChange Função callback de chamada chamada quando a password é alterada.
 * @param onLoginClick Função callback chamada quando o botão de login é clicado.
 * @param navController O controller de navegação para navegar para outras screens.
 */
@Composable
fun LoginFields(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: (String) -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Please login")
        OutlinedTextField(
            value = email,
            placeholder = { Text(text = "user@email.com") },
            label = { Text(text = "email") },
            onValueChange = onEmailChange,
        )

        OutlinedTextField(
            value = password,
            placeholder = { Text(text = "password") },
            label = { Text(text = "password") },
            onValueChange = onPasswordChange,
            visualTransformation = PasswordVisualTransformation()
        )

        Button(onClick = {
            if (email.isBlank() == false && password.isBlank() == false) {
                onLoginClick(email)
            } else {
                Toast.makeText(
                    context,
                    "Please enter an email and password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }) {
            Text("Login")
        }

        Button(onClick = { navController.navigate("Register") }) {
            Text("Register")
        }
    }
}


/**
 * Componente que exibe um botão para logout do utilizador.
 *
 * @param onLogoutClick Função callback chamada quando o botão de logout é clicado.
 * @param navController O controller de navegação para navegar para outras screens.
 */
@Composable
fun LogoutFields(
    onLogoutClick: () -> Unit,
    navController: NavController
){
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        verticalArrangement = Arrangement.spacedBy(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onLogoutClick) {
            Text("Logout!")
        }
    }
}
