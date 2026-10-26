package com.example.model

import java.security.SecureRandom

/**
 * Mathematically verified BingoBoard data model.
 * Represents an N x N matrix with unique numbers 1 to N^2.
 * Tracks marked indices with an optimized O(1)/O(N) boolean lookup.
 */
class BingoBoard(
    val mode: GameMode,
    initialNumbers: List<Int>? = null
) {
    val size: Int = mode.size
    val totalCells: Int = mode.totalNumbers

    // Grid numbers arranged in row-major order: index = row * size + col
    val numbers: List<Int> = if (initialNumbers != null && initialNumbers.size == totalCells) {
        initialNumbers
    } else {
        generateShuffledPermutation(totalCells)
    }

    // Fast O(1) index lookup by number
    private val numberToIndexMap: Map<Int, Int> = numbers.mapIndexed { index, num -> num to index }.toMap()

    // Marked state tracker for each grid cell
    private val marked: BooleanArray = BooleanArray(totalCells)

    // Chronologically ordered completed lines tracker
    private val completedLinesOrdered = mutableListOf<CompletedLine>()

    companion object {
        private val secureRandom = SecureRandom()

        fun generateShuffledPermutation(total: Int): List<Int> {
            val list = (1..total).toMutableList()
            // Fisher-Yates shuffle with SecureRandom
            for (i in list.size - 1 downTo 1) {
                val j = secureRandom.nextInt(i + 1)
                val temp = list[i]
                list[i] = list[j]
                list[j] = temp
            }
            return list
        }
    }

    /**
     * Mark a number on the board.
     * Returns true if number was present and marked, false otherwise.
     */
    fun markNumber(number: Int): Boolean {
        val index = numberToIndexMap[number] ?: return false
        if (!marked[index]) {
            marked[index] = true
            return true
        }
        return false
    }

    fun isMarkedAtIndex(index: Int): Boolean {
        if (index in 0 until totalCells) {
            return marked[index]
        }
        return false
    }

    fun isMarkedNumber(number: Int): Boolean {
        val index = numberToIndexMap[number] ?: return false
        return marked[index]
    }

    fun getMarkedSnapshot(): List<Boolean> = marked.toList()

    /**
     * Evaluates all rows, columns, and the two main diagonals.
     * Returns a list of all currently completed lines.
     */
    fun getCompletedLines(): List<CompletedLine> {
        val currentlyCompleted = mutableListOf<CompletedLine>()

        // 1. Check Horizontal Lines (Rows)
        for (r in 0 until size) {
            var rowComplete = true
            val cellIndices = ArrayList<Int>(size)
            for (c in 0 until size) {
                val idx = r * size + c
                cellIndices.add(idx)
                if (!marked[idx]) {
                    rowComplete = false
                }
            }
            if (rowComplete) {
                currentlyCompleted.add(CompletedLine(LineType.HORIZONTAL, r, cellIndices))
            }
        }

        // 2. Check Vertical Lines (Columns)
        for (c in 0 until size) {
            var colComplete = true
            val cellIndices = ArrayList<Int>(size)
            for (r in 0 until size) {
                val idx = r * size + c
                cellIndices.add(idx)
                if (!marked[idx]) {
                    colComplete = false
                }
            }
            if (colComplete) {
                currentlyCompleted.add(CompletedLine(LineType.VERTICAL, c, cellIndices))
            }
        }

        // 3. Check Main Diagonal (Top-Left to Bottom-Right)
        var mainDiagComplete = true
        val mainDiagIndices = ArrayList<Int>(size)
        for (i in 0 until size) {
            val idx = i * size + i
            mainDiagIndices.add(idx)
            if (!marked[idx]) {
                mainDiagComplete = false
            }
        }
        if (mainDiagComplete) {
            currentlyCompleted.add(CompletedLine(LineType.DIAGONAL_MAIN, 0, mainDiagIndices))
        }

        // 4. Check Anti-Diagonal (Top-Right to Bottom-Left)
        var antiDiagComplete = true
        val antiDiagIndices = ArrayList<Int>(size)
        for (i in 0 until size) {
            val idx = i * size + (size - 1 - i)
            antiDiagIndices.add(idx)
            if (!marked[idx]) {
                antiDiagComplete = false
            }
        }
        if (antiDiagComplete) {
            currentlyCompleted.add(CompletedLine(LineType.DIAGONAL_ANTI, 1, antiDiagIndices))
        }

        // Retain only lines that are still complete
        completedLinesOrdered.retainAll { line ->
            currentlyCompleted.any { it.lineType == line.lineType && it.index == line.index }
        }

        // Append newly completed lines in chronological order
        for (line in currentlyCompleted) {
            if (completedLinesOrdered.none { it.lineType == line.lineType && it.index == line.index }) {
                completedLinesOrdered.add(line)
            }
        }

        return completedLinesOrdered.toList()
    }

    /**
     * Check if winning condition is met.
     */
    fun hasWon(): Boolean {
        return getCompletedLines().size >= mode.linesToWin
    }

    /**
     * Returns string of illuminated letters based on number of completed lines.
     * e.g., for 5x5 with 3 lines completed: "B I N"
     */
    fun getIlluminatedLetters(): String {
        val linesCount = getCompletedLines().size
        val target = mode.targetWord
        val count = linesCount.coerceAtMost(target.length)
        return target.substring(0, count)
    }

    /**
     * Returns a set of cell indices that belong to at least one completed line.
     */
    fun getWinningCellIndices(): Set<Int> {
        val set = mutableSetOf<Int>()
        for (line in getCompletedLines()) {
            set.addAll(line.cellIndices)
        }
        return set
    }
}
