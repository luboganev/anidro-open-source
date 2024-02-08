package app.anidro.renderers

import app.anidro.models.TimedSegment
import java.util.*

/**
 * This time normalizer adjusts the starting time
 * of all segments of the drawing in a way that only a single segment of
 * the drawing is being drawn at a specific time.
 * It also reduces the pauses between segments to be short and equal between all
 * drawing segments.
 */
class SequentialTimeNormalizer : DrawingTimeNormalizer {

    private var normalizedSegments = mutableListOf<TimedSegment>()

    override var normalizedDuration: Long = 0
        private set

    override val normalizedDrawing: List<TimedSegment>
        get() = normalizedSegments

    override fun normalizeDrawing(segments: List<TimedSegment>) {
        normalizedSegments.clear()
        normalizedDuration = 0
        if (segments.isNotEmpty()) {
            var currentTime: Long = 0
            for (segment in segments) {
                segment.adjustStartTimestamp(currentTime)
                normalizedSegments.add(segment)
                currentTime += segment.duration + SEGMENT_PAUSE
            }
            normalizedDuration = normalizedSegments[normalizedSegments.size - 1].endTime
        }
    }

    companion object {
        private const val SEGMENT_PAUSE: Long = 100
    }
}