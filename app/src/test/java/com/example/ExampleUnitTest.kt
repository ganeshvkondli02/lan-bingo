package com.example

import com.example.model.BingoBoard
import com.example.model.GameMode
import com.example.model.LineType
import com.example.model.NetworkPacket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    @Test
    fun testBingoBoard5x5Initialization() {
        val board = BingoBoard(GameMode.CLASSIC_5X5)
        assertEquals(25, board.numbers.size)
        assertEquals(25, board.numbers.toSet().size) // all unique numbers
        assertTrue(board.numbers.all { it in 1..25 })
        assertEquals(0, board.getCompletedLines().size)
        assertFalse(board.hasWon())
    }

    @Test
    fun testBingoBoard10x10Initialization() {
        val board = BingoBoard(GameMode.EXTRA_10X10)
        assertEquals(100, board.numbers.size)
        assertEquals(100, board.numbers.toSet().size)
        assertTrue(board.numbers.all { it in 1..100 })
        assertEquals(0, board.getCompletedLines().size)
    }

    @Test
    fun testHorizontalLineDetection() {
        // Sequential numbers 1..25
        val numbers = (1..25).toList()
        val board = BingoBoard(GameMode.CLASSIC_5X5, numbers)

        // Mark first row (1, 2, 3, 4, 5)
        for (i in 1..5) {
            board.markNumber(i)
        }

        val lines = board.getCompletedLines()
        assertEquals(1, lines.size)
        assertEquals(LineType.HORIZONTAL, lines[0].lineType)
        assertEquals(0, lines[0].index)
        assertEquals("B", board.getIlluminatedLetters())
    }

    @Test
    fun testDiagonalLineDetection() {
        val numbers = (1..25).toList()
        val board = BingoBoard(GameMode.CLASSIC_5X5, numbers)

        // Main diagonal: (0,0)=1, (1,1)=7, (2,2)=13, (3,3)=19, (4,4)=25
        val mainDiag = listOf(1, 7, 13, 19, 25)
        mainDiag.forEach { board.markNumber(it) }

        val lines = board.getCompletedLines()
        assertEquals(1, lines.size)
        assertEquals(LineType.DIAGONAL_MAIN, lines[0].lineType)
        assertEquals(0, lines[0].index)
    }

    @Test
    fun testWinningConditionFiveLines() {
        val numbers = (1..25).toList()
        val board = BingoBoard(GameMode.CLASSIC_5X5, numbers)

        // Mark 5 horizontal lines (all 25 numbers)
        for (i in 1..25) {
            board.markNumber(i)
        }

        // 5 rows + 5 cols + 2 diags = 12 total completed lines
        assertTrue(board.getCompletedLines().size >= 5)
        assertTrue(board.hasWon())
        assertEquals("BINGO", board.getIlluminatedLetters())
    }

    @Test
    fun testNetworkPacketSerialization() {
        val handshake = NetworkPacket.Handshake("p1", "Alice", 3, true)
        val json = handshake.toJsonString()
        val deserialized = NetworkPacket.fromJsonString(json)

        assertNotNull(deserialized)
        assertTrue(deserialized is NetworkPacket.Handshake)
        val h = deserialized as NetworkPacket.Handshake
        assertEquals("p1", h.playerId)
        assertEquals("Alice", h.playerName)
        assertEquals(3, h.avatarIndex)
        assertTrue(h.isHost)
    }
}
