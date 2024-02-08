package app.anidro.renderers

import app.anidro.models.TimedSegment
import java.util.*

/**
 * This time normalizer removes all pauses and sets all segments durations to null.
 * It practically makes sure that the [FixedFrameRateRenderer] renders a single
 * frame with the whole drawing in it.
 */
class SingleFrameTimeNormalizer : DrawingTimeNormalizer {
    private var normalizedSegments = mutableListOf<TimedSegment>()

    override val normalizedDuration: Long
        get() = 0

    override val normalizedDrawing: List<TimedSegment>
        get() = normalizedSegments

    override fun normalizeDrawing(segments: List<TimedSegment>) {
        normalizedSegments.clear()
        for (segment in segments) {
            segment.adjustStartTimestamp(0)
            segment.adjustDuration(0)
            normalizedSegments.add(segment)
        }
    }
}