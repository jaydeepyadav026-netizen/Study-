package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.ai.AiParsedResult
import com.example.ui.screens.ai.LifeAiEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("LIFE OS", appName)
  }

  @Test
  fun `verify LifeAiEngine parses expense correctly`() {
    val parsed = LifeAiEngine.parseUserCommand("Add ₹200 transport expense today")
    assertTrue(parsed is AiParsedResult.TransactionAction)
    val action = parsed as AiParsedResult.TransactionAction
    assertEquals(200.0, action.amount, 0.01)
    assertEquals("Transport", action.category)
    assertEquals("Expense", action.type)
  }
}
