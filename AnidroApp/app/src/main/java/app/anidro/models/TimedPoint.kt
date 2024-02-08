package app.anidro.models

data class TimedPoint(val x: Float, val y: Float, val timestamp: Long) {

    /**
     * Calculates the time in milliseconds between this point and another [TimedPoint]
     */
    fun timeTo(point: TimedPoint): Long {
        return timeTo(point.timestamp)
    }

    /**
     * Calculates the time in milliseconds between this point and a particular timestamp
     */
    fun timeTo(timestamp: Long): Long {
        return this.timestamp - timestamp
    }
}