package dev.medzik.librepass.android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.drawablepainter.DrawablePainter
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composables.common.TopBarTwoColor

@Composable
fun WelcomeScreen(navController: NavController) {
    // get composable context
    val context = LocalContext.current

    // get app icon
    val icon = context.packageManager.getApplicationIcon(context.packageName)

    Scaffold(
        topBar = { TopBarTwoColor("Libre", "Pass") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // show app icon
            Image(
                painter = DrawablePainter(icon),
                contentDescription = null,
                modifier = Modifier.size(128.dp)
            )

            Text(
                text = stringResource(id = R.string.welcome),
                modifier = Modifier.padding(top = 20.dp)
            )

            Button(
                onClick = { navController.navigate(Screen.Register.get) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 90.dp)
                    .padding(top = 20.dp)
            ) {
                Text(text = stringResource(id = R.string.register))
            }

            OutlinedButton(
                onClick = { navController.navigate(Screen.Login.get) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 90.dp)
                    .padding(top = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.login))
            }
        }
    }
}
