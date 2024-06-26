package dev.medzik.librepass.android.ui.screens.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.medzik.android.components.PickerDialog
import dev.medzik.android.components.PropertyPreference
import dev.medzik.android.components.SwitcherPreference
import dev.medzik.android.components.rememberDialogState
import dev.medzik.android.crypto.KeyStore
import dev.medzik.librepass.android.MainActivity
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.LibrePassViewModel
import dev.medzik.librepass.android.utils.KeyAlias
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.SecretStore.writeKey
import dev.medzik.librepass.android.utils.StoreKey
import dev.medzik.librepass.android.utils.VaultTimeoutValues
import dev.medzik.librepass.android.utils.checkIfBiometricAvailable
import dev.medzik.librepass.android.utils.showBiometricPromptForSetup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsSecurityScreen(viewModel: LibrePassViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val credentials = viewModel.credentialRepository.get() ?: return

    val scope = rememberCoroutineScope()
    var biometricEnabled by remember { mutableStateOf(credentials.biometricAesKey != null) }
    val timerDialogState = rememberDialogState()
    var vaultTimeout by remember { mutableIntStateOf(context.readKey(StoreKey.VaultTimeout)) }

    // Biometric checked event handler (enable/disable biometric authentication)
    fun biometricHandler() {
        if (biometricEnabled) {
            biometricEnabled = false

            scope.launch(Dispatchers.IO) {
                viewModel.credentialRepository.update(
                    credentials.copy(
                        biometricReSetup = false,
                        biometricAesKey = null,
                        biometricAesKeyIV = null
                    )
                )
            }

            return
        }

        showBiometricPromptForSetup(
            context as MainActivity,
            cipher = KeyStore.initForEncryption(
                KeyAlias.BiometricAesKey,
                deviceAuthentication = false
            ),
            onAuthenticationSucceeded = { cipher ->
                val encryptedData = KeyStore.encrypt(
                    cipher = cipher,
                    clearBytes = viewModel.vault.aesKey
                )

                biometricEnabled = true

                scope.launch {
                    viewModel.credentialRepository.update(
                        credentials.copy(
                            biometricAesKey = encryptedData.cipherText,
                            biometricAesKeyIV = encryptedData.initializationVector
                        )
                    )
                }
            },
            onAuthenticationFailed = {}
        )
    }

    @Composable
    fun getVaultTimeoutTranslation(value: VaultTimeoutValues): String {
        return when (value) {
            VaultTimeoutValues.INSTANT -> stringResource(R.string.Timeout_Instant)

            VaultTimeoutValues.ONE_MINUTE -> pluralStringResource(R.plurals.minutes, 1, 1)

            VaultTimeoutValues.FIVE_MINUTES -> pluralStringResource(R.plurals.minutes, 5, 5)

            VaultTimeoutValues.FIFTEEN_MINUTES -> pluralStringResource(R.plurals.minutes, 15, 15)

            VaultTimeoutValues.THIRTY_MINUTES -> pluralStringResource(R.plurals.minutes, 30, 30)

            VaultTimeoutValues.ONE_HOUR -> pluralStringResource(R.plurals.hours, 1, 1)

            VaultTimeoutValues.NEVER -> stringResource(R.string.Timeout_Never)
        }
    }

    if (checkIfBiometricAvailable(context)) {
        SwitcherPreference(
            title = stringResource(R.string.UnlockWithBiometrics),
            icon = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
            checked = biometricEnabled,
            onCheckedChange = { biometricHandler() }
        )
    }

    PropertyPreference(
        title = stringResource(R.string.VaultTimeout),
        icon = { Icon(Icons.Default.Timer, contentDescription = null) },
        currentValue = getVaultTimeoutTranslation(
            VaultTimeoutValues.fromSeconds(
                vaultTimeout
            )
        ),
        onClick = { timerDialogState.show() },
    )

    PickerDialog(
        state = timerDialogState,
        title = stringResource(R.string.VaultTimeout),
        items = VaultTimeoutValues.entries,
        onSelected = {
            vaultTimeout = it.seconds
            context.writeKey(StoreKey.VaultTimeout, it.seconds)
        }
    ) {
        Text(
            text = getVaultTimeoutTranslation(it),
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth()
        )
    }
}
