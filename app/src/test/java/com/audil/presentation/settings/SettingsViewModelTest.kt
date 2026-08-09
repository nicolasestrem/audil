package com.audil.presentation.settings

import com.audil.data.remote.ApiSettings
import com.audil.data.remote.ApiSettingsProvider
import com.audil.data.repository.SettingsRepository
import io.mockk.every
import io.mockk.coVerify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: SettingsRepository
    private lateinit var settingsProvider: ApiSettingsProvider
    private lateinit var context: android.content.Context
    private lateinit var viewModel: SettingsViewModel

    private val themeFlow = MutableStateFlow(SettingsRepository.THEME_SYSTEM)
    private val languageFlow = MutableStateFlow("en")
    private val modelTypeFlow = MutableStateFlow(SettingsRepository.MODEL_LOCAL_STANDARD)
    private val useRemoteFlow = MutableStateFlow(false)
    private val remoteApiUrlFlow = MutableStateFlow("https://api.openai.com/v1")
    private val remoteApiKeyFlow = MutableStateFlow("")
    private val remoteModelNameFlow = MutableStateFlow("gpt-3.5-turbo")
    private val transcriptionModelFlow = MutableStateFlow("whisper-1")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = io.mockk.mockk(relaxed = true)
        context = io.mockk.mockk(relaxed = true)

        every { repository.theme } returns themeFlow
        every { repository.language } returns languageFlow
        every { repository.modelType } returns modelTypeFlow
        every { repository.useRemoteGeneration } returns useRemoteFlow
        every { repository.remoteApiUrl } returns remoteApiUrlFlow
        every { repository.remoteApiKey } returns remoteApiKeyFlow
        every { repository.remoteModelName } returns remoteModelNameFlow
        every { repository.transcriptionModel } returns transcriptionModelFlow

        settingsProvider = object : ApiSettingsProvider {
            override fun getSettings() = ApiSettings.DEFAULT
        }
        viewModel = SettingsViewModel(repository, settingsProvider, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial theme is system`() = runTest {
        assertEquals(SettingsRepository.THEME_SYSTEM, viewModel.theme.first())
    }

    @Test
    fun `initial language is en`() = runTest {
        assertEquals("en", viewModel.language.first())
    }

    @Test
    fun `initial useRemoteGeneration is false`() = runTest {
        assertFalse(viewModel.useRemoteGeneration.first())
    }

    @Test
    fun `can set theme`() = runTest {
        viewModel.setTheme(SettingsRepository.THEME_DARK)
        advanceUntilIdle()
        coVerify { repository.setTheme(SettingsRepository.THEME_DARK) }
    }

    @Test
    fun `can set language`() = runTest {
        viewModel.setLanguage("fr")
        advanceUntilIdle()
        coVerify { repository.setLanguage("fr") }
    }

    @Test
    fun `can toggle useRemoteGeneration`() = runTest {
        viewModel.setUseRemoteGeneration(true)
        advanceUntilIdle()
        coVerify { repository.setUseRemoteGeneration(true) }
    }

    @Test
    fun `can set remoteApiUrl`() = runTest {
        viewModel.setRemoteApiUrl("https://api.example.com/v1")
        advanceUntilIdle()
        coVerify { repository.setRemoteApiUrl("https://api.example.com/v1") }
    }

    @Test
    fun `can set remoteApiKey`() = runTest {
        viewModel.setRemoteApiKey("sk-secret")
        advanceUntilIdle()
        coVerify { repository.setRemoteApiKey("sk-secret") }
    }

    @Test
    fun `can set remoteModelName`() = runTest {
        viewModel.setRemoteModelName("gpt-4o-mini")
        advanceUntilIdle()
        coVerify { repository.setRemoteModelName("gpt-4o-mini") }
    }

    @Test
    fun `can set transcriptionModel`() = runTest {
        viewModel.setTranscriptionModel("whisper-2")
        advanceUntilIdle()
        coVerify { repository.setTranscriptionModel("whisper-2") }
    }

    @Test
    fun `testConnection state starts false`() = runTest {
        assertFalse(viewModel.isTestingConnection.first())
    }

    @Test
    fun `connectionErrorMessage starts null`() = runTest {
        assertNull(viewModel.connectionErrorMessage.first())
    }
}
