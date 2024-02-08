package app.anidro.models

import android.graphics.Paint
import android.graphics.PointF
import app.anidro.common.DrawingPaintBuilder
import kotlin.math.floor

/**
 * This class represents a collection of points forming a single drawing stroke
 * on a 2D canvas. The exact timestamp of each point is also saved
 */
class TimedSegment {
    private val points: Array<PointF>
    private val timestamps: LongArray
    private var canvasWidth: Int
    private var canvasHeight: Int

    val paint: Paint

    val color: Int
        get() = paint.color

    /**
     * Calculate the total drawing duration of the segment, i.e. the time elapsed
     * between the first and the last point
     */
    val duration: Long
        get() = if (timestamps.size > 1) {
            timestamps.last() - timestamps.first()
        } else 0

    val endTime: Long
        get() = timestamps.last()

    fun getPoint(index: Int) = points[index]

    val pointsCount: Int
        get() = points.size

    val isEmpty: Boolean
        get() = points.isEmpty()

    constructor(points: List<TimedPoint>, canvasWidth: Int, canvasHeight: Int, paint: Paint) {
        timestamps = LongArray(points.size)
        this.points = points.mapIndexed { index, timedPoint ->
            timestamps[index] = timedPoint.timestamp
            PointF(timedPoint.x, timedPoint.y)
        }.toTypedArray()
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
        this.paint = paint
    }

    constructor(points: Array<PointF>, timestamps: LongArray, color: Int, strokeWidth: Float,
                canvasWidth: Int, canvasHeight: Int) {
        this.points = points
        this.timestamps = timestamps
        this.paint = DrawingPaintBuilder.getPaint(color, strokeWidth)
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
    }

    /**
     * Sets the timestamp of the first point to the input timestamp and recalculates the
     * timestamps of the remaining points accordingly
     */
    fun adjustStartTimestamp(timestamp: Long) {
        if (timestamps.isNotEmpty() && timestamp >= 0) {
            val diff = timestamp - timestamps[0]
            for (i in timestamps.indices) {
                timestamps[i] += diff
            }
        }
    }

    /**
     * Recalculates the timestamps of all the points so that the total
     * duration equals the input duration
     */
    fun adjustDuration(newDuration: Long) {
        if (timestamps.size > 1) {
            val firstTimestamp = timestamps[0]
            if (newDuration > 0) {
                val adjustCoef = newDuration.toDouble() / duration.toDouble()
                var tempTimestamp: Long
                for (i in 1 until timestamps.size) {
                    tempTimestamp = timestamps[i]
                    tempTimestamp = floor((tempTimestamp - firstTimestamp).toDouble() * adjustCoef).toLong()
                    timestamps[i] = tempTimestamp + firstTimestamp
                }
            } else {
                for (i in 1 until timestamps.size) {
                    timestamps[i] = firstTimestamp
                }
            }
        }
    }

    /**
     * Updates the canvas width and height of this segment and recalculates
     * the x and y coordinates of all points accordingly
     */
    fun adjustCanvasSize(newCanvasWidth: Int, newCanvasHeight: Int) {
        if (newCanvasWidth == canvasWidth && newCanvasHeight == canvasHeight) {
            return
        }
        val widthCoef = newCanvasWidth.toFloat() / canvasWidth.toFloat()
        val heightCoef = newCanvasHeight.toFloat() / canvasHeight.toFloat()
        var currentPoint: PointF
        for (i in points.indices) {
            currentPoint = points[i]
            points[i] = PointF(currentPoint.x * widthCoef, currentPoint.y * heightCoef)
        }
        canvasWidth = newCanvasWidth
        canvasHeight = newCanvasHeight
        paint.strokeWidth = paint.strokeWidth * widthCoef
    }

    /**
     * Checks if there are any points with timestamp inside in the input time interval
     */
    fun hasPointsInInterval(startTime: Long, endTime: Long): Boolean {
        if (timestamps.isEmpty()) return false
        if (timestamps.first() > endTime || timestamps.last() < startTime) return false
        return true
    }

    /**
     * Returns a new [TimedSegment] subsegment defined by
     * the input start timestamp and duration. It contains all points
     * of the current [TimedSegment] inside this interval as well as
     * any missing boundary points
     */
    fun getSubSegment(start: Long, duration: Long): TimedSegment {
        if (isEmpty) {
            return this
        }

        val end = start + duration

        if (timestamps[0] > end) {
            return TimedSegment(arrayOf(), LongArray(0), paint.color, paint.strokeWidth, canvasWidth, canvasHeight)
        }

        if (timestamps[timestamps.size - 1] < start) {
            return TimedSegment(arrayOf(), LongArray(0), paint.color, paint.strokeWidth, canvasWidth, canvasHeight)
        }

        if (timestamps[0] >= start) {
            if (timestamps[timestamps.size - 1] <= end) {
                // Whole segment is in the interval
                return this
            } else {
                // Only end is outside the interval
                val firstAfterNewEndIndex = getFirstPointAfter(end)
                val newEnd = calculatePointBetween(firstAfterNewEndIndex - 1, firstAfterNewEndIndex, end)
                return buildSubSegment(0, firstAfterNewEndIndex, null, newEnd)
            }
        } else {
            if (timestamps[timestamps.size - 1] <= end) {
                // Only start is outside the interval
                val lastBeforeNewStartIndex = getLastPointBefore(start)
                val newStart = calculatePointBetween(lastBeforeNewStartIndex, lastBeforeNewStartIndex + 1, start)
                return buildSubSegment(lastBeforeNewStartIndex, timestamps.size - 1, newStart, null)
            } else {
                // Both start and end are outside the interval
                val lastBeforeNewStartIndex = getLastPointBefore(start)
                val newStart = calculatePointBetween(lastBeforeNewStartIndex, lastBeforeNewStartIndex + 1, start)

                val firstAfterNewEndIndex = getFirstPointAfter(end)
                val newEnd = calculatePointBetween(firstAfterNewEndIndex - 1, firstAfterNewEndIndex, end)

                return buildSubSegment(lastBeforeNewStartIndex, firstAfterNewEndIndex, newStart, newEnd)
            }
        }
    }

    private fun getLastPointBefore(timestamp: Long): Int {
        for (i in 1 until timestamps.size) {
            if (timestamps[i] >= timestamp) {
                return i - 1
            }
        }
        return timestamps.size - 1
    }

    private fun getFirstPointAfter(timestamp: Long): Int {
        for (i in timestamps.size - 2 downTo 0) {
            if (timestamps[i] <= timestamp) {
                return i + 1
            }
        }
        return 0
    }

    private fun buildSubSegment(startIndex: Int, endIndex: Int, newStart: TimedPoint?, newEnd: TimedPoint?): TimedSegment {
        val subSegmentPoints = points.copyOfRange(startIndex, endIndex + 1)
        val subSegmentTimestamps = timestamps.copyOfRange(startIndex, endIndex + 1)
        if (newStart != null) {
            subSegmentPoints[0] = PointF(newStart.x, newStart.y)
            subSegmentTimestamps[0] = newStart.timestamp
        }
        if (newEnd != null) {
            subSegmentPoints[subSegmentPoints.size - 1] = PointF(newEnd.x, newEnd.y)
            subSegmentTimestamps[subSegmentTimestamps.size - 1] = newEnd.timestamp
        }
        return TimedSegment(subSegmentPoints, subSegmentTimestamps, paint.color, paint.strokeWidth, canvasWidth, canvasHeight)
    }

    private fun calculatePointBetween(firstPointIdx: Int, secondPointIdx: Int, timestampBetween: Long): TimedPoint? {
        if (timestamps[firstPointIdx] == timestampBetween) {
            return getAsTimedPoint(firstPointIdx)
        }
        if (timestamps[secondPointIdx] == timestampBetween) {
            return getAsTimedPoint(secondPointIdx)
        }
        val coef = (timestampBetween - timestamps[firstPointIdx]).toFloat() / (timestamps[secondPointIdx] - timestamps[firstPointIdx]).toFloat()
        val x = (1 - coef) * points[firstPointIdx].x + coef * points[secondPointIdx].x
        val y = (1 - coef) * points[firstPointIdx].y + coef * points[secondPointIdx].y
        return TimedPoint(x, y, timestampBetween)
    }

    private fun getAsTimedPoint(index: Int): TimedPoint {
        return TimedPoint(points[index].x, points[index].y, timestamps[index])
    }
}