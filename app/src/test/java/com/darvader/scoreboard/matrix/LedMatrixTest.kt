package com.darvader.scoreboard.matrix

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import com.darvader.scoreboard.IEchoClient

class LedMatrixTest {
    private lateinit var ledMatrix: LedMatrix
    private lateinit var mockEchoClient: IEchoClient

    @Before
    fun setUp() {
        mockEchoClient = Mockito.mock(IEchoClient::class.java)
        ledMatrix = LedMatrix(mockEchoClient)
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
        assertEquals(address1, LedMatrix.matrixAddress)
        ledMatrix.onMessage(address2, "LedMatrix")
        assertEquals(address1, LedMatrix.matrixAddress)
    }

    // Timeout and scrollText tests skipped (require Android UI/threading)
}
