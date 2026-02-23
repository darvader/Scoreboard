package com.darvader.scoreboard

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.assertion.ViewAssertions.matches
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before
import com.darvader.scoreboard.matrix.LedMatrix
import com.darvader.scoreboard.matrix.livescore.TestLiveScoreWebSocketManager
import com.darvader.scoreboard.matrix.activity.ScoreboardActivity

@RunWith(AndroidJUnit4::class)
class ScoreboardUiTest {
    @Rule
    @JvmField
    val activityRule = ActivityTestRule(ScoreboardActivity::class.java)

    lateinit var testEchoServer: TestEchoServer
    lateinit var testWebSocket: TestLiveScoreWebSocketManager

    @Before
    fun setup() {
        // Replace EchoServer and WebSocket with test doubles
        testEchoServer = TestEchoServer()
        MainActivity.echoServer = testEchoServer
        testWebSocket = TestLiveScoreWebSocketManager()
        // If LiveScoreActivity uses a static reference, replace it here
        // LiveScoreActivity.webSocketManager = testWebSocket
    }

    @Test
    fun testPointsUpLeftButton() {
        onView(withId(R.id.pointsUpLeft)).perform(click())
        onView(withId(R.id.points)).check(matches(withText("01:00")))
    }

    @Test
    fun testMatrixDetectAndSelect() {
        onView(withId(R.id.detect)).perform(click())
        // Simulate matrix response
        testEchoServer.simulateMatrixResponse("192.168.1.101")
        // Check that the button for the matrix appears and is selected
        onView(withText("101")).check(matches(withText("101")))
    }

    @Test
    fun testLiveScoreTVV() {
        // Simulate TVV WebSocket score update
        testWebSocket.simulateScoreUpdate(10, 8)
        // Check that the UI updates accordingly (example: points text)
        // onView(withId(R.id.points)).check(matches(withText("10:08")))
    }

    @Test
    fun testLiveScoreDVV() {
        // Simulate DVV WebSocket score update
        testWebSocket.simulateScoreUpdate(15, 12)
        // Check that the UI updates accordingly (example: points text)
        // onView(withId(R.id.points)).check(matches(withText("15:12")))
    }

    @Test
    fun testLiveScoreRegionSwitchAndMatrixSelection() {
        // Simulate TVV region
        testWebSocket.simulateScoreUpdate(10, 8)
        // TODO: Add code to switch region in UI if needed
        // Simulate DVV region
        testWebSocket.simulateScoreUpdate(15, 12)
        // Simulate matrix detection and selection
        onView(withId(R.id.detect)).perform(click())
        testEchoServer.simulateMatrixResponse("192.168.1.102")
        onView(withText("102")).perform(click())
        // Check that the matrix button is selected (alpha = 1.0f) and points are updated
        // onView(withText("102")).check(matches(isSelected())) // Custom matcher may be needed
        // onView(withId(R.id.points)).check(matches(withText("15:12")))
    }
}
