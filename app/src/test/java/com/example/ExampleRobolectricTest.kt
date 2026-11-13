package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.BrowserRepository
import com.example.data.SearchSuggestionService
import com.example.data.local.BrowserDatabase
import com.example.model.SearchEngine
import com.example.ui.BrowserViewModel
import com.example.ui.OrbitBrowserApp
import com.example.ui.theme.OrbitTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("Orbit", context.getString(R.string.app_name))
    }

    @Test
    fun `start page tabs private mode and theme settings work smoothly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BrowserDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = BrowserRepository(dao = db.browserDao(), context = context)
        val viewModel = BrowserViewModel(repository)

        composeRule.setContent {
            OrbitTheme {
                OrbitBrowserApp(viewModel = viewModel)
            }
        }

        // Top omnibox, daisyUI Join segmented control, and Quick Access tiles are visible
        composeRule.onNodeWithTag("omnibox_input").assertIsDisplayed()
        composeRule.onNodeWithTag("start_tab_quick_access").assertIsDisplayed()
        composeRule.onNodeWithTag("speed_dial_wikipedia").performScrollTo().assertIsDisplayed()

        // Switch Start Page segmented tab to Bookmarks and back
        composeRule.onNodeWithTag("start_tab_bookmarks").performClick()
        composeRule.onNodeWithTag("start_tab_quick_access").performClick()

        // Open Tabs switcher and create a Private Tab
        composeRule.onNodeWithTag("tabs_switcher_button").performClick()
        composeRule.onNodeWithTag("tabs_switcher_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("tabs_new_private_button").performClick()
        composeRule.onNodeWithTag("private_mode_banner").assertIsDisplayed()

        // Open Browser Menu and navigate to Settings
        composeRule.onNodeWithTag("more_menu_button").performClick()
        composeRule.onNodeWithTag("browser_menu_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("menu_settings").performScrollTo().performClick()
        composeRule.onNodeWithTag("settings_sheet").assertIsDisplayed()

        // Switch Theme Palette and Corner Style, verify Search Engine chip
        composeRule.onNodeWithTag("theme_preset_daisy_dracula").performScrollTo().performClick()
        composeRule.onNodeWithTag("radius_preset_pill").performScrollTo().performClick()
        composeRule.onNodeWithTag("engine_chip_duckduckgo").performScrollTo().assertIsDisplayed()

        db.close()
    }

    @Test
    fun `typing non-domain query shows search engine suggestions and submitting navigates`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BrowserDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = BrowserRepository(dao = db.browserDao(), context = context)
        val fakeSuggestionService = object : SearchSuggestionService() {
            override suspend fun fetchSuggestions(query: String, engine: SearchEngine): List<String> {
                return listOf("$query material3", "$query navigation")
            }
        }
        val viewModel = BrowserViewModel(repository, fakeSuggestionService)

        composeRule.setContent {
            OrbitTheme {
                OrbitBrowserApp(viewModel = viewModel)
            }
        }

        // Focus omnibox and type a search query (not a domain)
        composeRule.onNodeWithTag("omnibox_input").performClick()
        composeRule.onNodeWithTag("omnibox_input").performTextInput("compose")
        org.robolectric.shadows.ShadowLooper.idleMainLooper(300, java.util.concurrent.TimeUnit.MILLISECONDS)
        composeRule.waitForIdle()

        // Search engine suggestions appear
        composeRule.onNodeWithTag("omnibox_suggestions").assertIsDisplayed()
        composeRule.onNodeWithText("compose material3").assertIsDisplayed()

        // Submitting via IME action resolves to active search engine URL (not wiped by focus loss)
        composeRule.onNodeWithTag("omnibox_input").performImeAction()
        composeRule.waitForIdle()

        assertEquals("https://duckduckgo.com/?q=compose", viewModel.uiState.value.activeTab.url)

        db.close()
    }
}
