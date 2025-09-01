package com.radarqr.dating.android.utility.cardstackview

enum class Direction {
    Left, Right, Top, Bottom;

    companion object {
        val HORIZONTAL = listOf(Left, Right)
        val VERTICAL = listOf(Top, Bottom)
        val FREEDOM = listOf(values())
    }
}