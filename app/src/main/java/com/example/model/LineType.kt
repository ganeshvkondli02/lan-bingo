package com.example.model

enum class LineType {
    HORIZONTAL,
    VERTICAL,
    DIAGONAL_MAIN,
    DIAGONAL_ANTI
}

data class CompletedLine(
    val lineType: LineType,
    val index: Int, // row index (0..N-1), column index (0..N-1), or 0 for diagonal
    val cellIndices: List<Int>
)
