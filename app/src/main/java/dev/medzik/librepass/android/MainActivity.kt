package dev.medzik.librepass.android

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import dagger.hilt.android.AndroidEntryPoint
import dev.medzik.android.components.navigate
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.ui.LibrePassNavigation
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.theme.LibrePassTheme
import dev.medzik.librepass.android.utils.SecretStore
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.StoreKey
import dev.medzik.librepass.android.utils.ThemeValues
import dev.medzik.librepass.android.utils.UserSecrets
import dev.medzik.librepass.android.utils.Vault
import dev.medzik.librepass.android.utils.VaultTimeoutValues
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var vault: Vault

    lateinit var userSecrets: UserSecrets

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // handle uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Log.e("LibrePass", "Uncaught exception", e)
            finish()
        }

        // merge application data when application updated
        Migrations.update(this, repository)

        // init vault
        userSecrets = SecretStore.initialize(this)

        vault.cipherRepository = repository.cipher

        // get app theme settings
        val dynamicColor = this.readKey(StoreKey.DynamicColor)
        val theme = this.readKey(StoreKey.Theme)
        val autoTheme = theme == ThemeValues.SYSTEM.ordinal
        val darkTheme = theme == ThemeValues.DARK.ordinal
        val blackTheme = theme == ThemeValues.BLACK.ordinal

        setContent {
            LibrePassTheme(
                darkTheme = blackTheme || darkTheme || (autoTheme && isSystemInDarkTheme()),
                blackTheme = blackTheme,
                dynamicColor = dynamicColor
            ) {
                LibrePassNavigation()
            }
        }
    }

    override fun onPause() {
        super.onPause()

        // check if user is logged
        if (repository.credentials.get() == null) return

        val vaultTimeout = this.readKey(StoreKey.VaultTimeout)
        if (vaultTimeout == VaultTimeoutValues.INSTANT.seconds) {
            SecretStore.delete(this)
        } else {
            SecretStore.save(this, userSecrets)
        }
    }

    /** Called from [LibrePassNavigation] */
    fun onResume(navController: NavController) {
        // check if user is logged

        if (repository.credentials.get() == null) return

        val vaultTimeout = this.readKey(StoreKey.VaultTimeout)
        val expiresTime = this.readKey(StoreKey.VaultExpiresAt)
        val currentTime = System.currentTimeMillis()

        // check if the vault has expired
        if (vaultTimeout == VaultTimeoutValues.INSTANT.seconds ||
            (vaultTimeout != VaultTimeoutValues.NEVER.seconds && currentTime > expiresTime)
        ) {
            SecretStore.delete(this)

            navController.navigate(
                screen = Screen.Unlock,
                disableBack = true
            )
        }
    }
}
