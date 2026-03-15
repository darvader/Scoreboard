package com.darvader.scoreboard

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
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
import androidx.test.espresso.Espresso

@RunWith(AndroidJUnit4::class)
class ScoreboardUiTest {
    @Rule
    @JvmField
    val activityRule = ActivityTestRule(ScoreboardActivity::class.java, true, false)

    lateinit var testUdpDiscoveryServer: TestUdpDiscoveryServer
    lateinit var testWebSocket: TestLiveScoreWebSocketManager

    @Before
    fun setup() {
        ScoreboardActivity.disableInformerForTests = true
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as ScoreboardApp
        app.ledMatrix = LedMatrix()
        testUdpDiscoveryServer = TestUdpDiscoveryServer()
        app.udpDiscoveryServer = testUdpDiscoveryServer
        testWebSocket = TestLiveScoreWebSocketManager()
        activityRule.launchActivity(null)
    }

    @Test
    fun testPointsUpLeftButton() {
        onView(withId(R.id.pointsUpLeft)).perform(click())
        onView(withId(R.id.points)).check(matches(withText("01:00")))
    }

    @Test
    fun testMatrixDetectAndSelect() {
        Espresso.onIdle()
        onView(withId(R.id.detect)).perform(scrollTo(), click())
        testUdpDiscoveryServer.simulateMatrixResponse("192.168.1.101")
        Thread.sleep(500)
        onView(withText("101")).check(matches(withText("101")))
    }

    @Test
    fun testLiveScoreTVV() {
        testWebSocket.simulateScoreUpdate(10, 8)
    }

    @Test
    fun testLiveScoreDVV() {
        testWebSocket.simulateScoreUpdate(15, 12)
    }

    @Test
    fun testLiveScoreRegionSwitchAndMatrixSelection() {
        testWebSocket.simulateScoreUpdate(10, 8)
        testWebSocket.simulateScoreUpdate(15, 12)
        Espresso.onIdle()
        onView(withId(R.id.detect)).perform(scrollTo(), click())
        testUdpDiscoveryServer.simulateMatrixResponse("192.168.1.102")
        Thread.sleep(500)
        onView(withText("102")).perform(scrollTo(), click())
    }
}
