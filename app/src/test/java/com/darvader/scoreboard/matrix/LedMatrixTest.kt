package com.darvader.scoreboard.matrix

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import com.darvader.scoreboard.IUdpClient

class LedMatrixTest {
    private lateinit var ledMatrix: LedMatrix
    private lateinit var mockUdpClient: IUdpClient

    @Before
    fun setUp() {
        mockUdpClient = Mockito.mock(IUdpClient::class.java)
        ledMatrix = LedMatrix(mockUdpClient)
    }

    @Test
    fun testPointsLeftUp() {
        ledMatrix.pointsLeft = 0
        ledMatrix.pointsLeftUp()
        assertEquals(1, ledMatrix.pointsLeft.toInt())
        assertEquals(1, ledMatrix.leftTeamServes.toInt())
    }

    @Test
    fun testPointsRightUp() {
        ledMatrix.pointsRight = 0
        ledMatrix.pointsRightUp()
        assertEquals(1, ledMatrix.pointsRight.toInt())
        assertEquals(0, ledMatrix.leftTeamServes.toInt())
    }

    @Test
    fun testSetsLeftUp() {
        ledMatrix.setsLeft = 0
        ledMatrix.setsLeftUp()
        assertEquals(1, ledMatrix.setsLeft.toInt())
    }

    @Test
    fun testMatrixDetectionAndSelection() {
        val address1 = "192.168.1.101"
        val address2 = "192.168.1.102"
        ledMatrix.onMessage(address1, "LedMatrix")
        ledMatrix.onMessage(address2, "LedMatrix")
        assertEquals(address1, ledMatrix.matrixAddress)
        ledMatrix.onMessage(address2, "LedMatrix")
        assertEquals(address1, ledMatrix.matrixAddress)
    }

    @Test
    fun testPointsMaxLimit() {
        ledMatrix.pointsLeft = 99
        ledMatrix.pointsLeftUp()
        assertEquals(99, ledMatrix.pointsLeft.toInt())
    }

    @Test
    fun testSetsMaxLimit() {
        ledMatrix.setsLeft = 9
        ledMatrix.setsLeftUp()
        assertEquals(9, ledMatrix.setsLeft.toInt())
    }

    @Test
    fun testSwitchSides() {
        ledMatrix.pointsLeft = 10
        ledMatrix.pointsRight = 20
        ledMatrix.setsLeft = 1
        ledMatrix.setsRight = 2
        ledMatrix.leftTeamServes = 1
        ledMatrix.switchSides()
        assertEquals(20, ledMatrix.pointsLeft.toInt())
        assertEquals(10, ledMatrix.pointsRight.toInt())
        assertEquals(2, ledMatrix.setsLeft.toInt())
        assertEquals(1, ledMatrix.setsRight.toInt())
        assertEquals(0, ledMatrix.leftTeamServes.toInt())
        assertTrue(ledMatrix.isTeamsSwitched)
    }

    @Test
    fun testReset() {
        ledMatrix.pointsLeft = 10
        ledMatrix.pointsRight = 20
        ledMatrix.setsLeft = 1
        ledMatrix.setsRight = 2
        ledMatrix.reset()
        assertEquals(0, ledMatrix.pointsLeft.toInt())
        assertEquals(0, ledMatrix.pointsRight.toInt())
        assertEquals(0, ledMatrix.setsLeft.toInt())
        assertEquals(0, ledMatrix.setsRight.toInt())
    }

    @Test
    fun testTimeout() {
        ledMatrix.timeout()
        Mockito.verify(mockUdpClient).send("timeout", "")
    }

    @Test
    fun testClearPoints() {
        ledMatrix.pointsLeft = 15
        ledMatrix.pointsRight = 22
        ledMatrix.clearPoints()
        assertEquals(0, ledMatrix.pointsLeft.toInt())
        assertEquals(0, ledMatrix.pointsRight.toInt())
        assertEquals(1, ledMatrix.leftTeamServes.toInt())
    }
}
