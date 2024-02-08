package app.anidro.renderers

import app.anidro.models.TimedSegment

/**
 * A drawing time normalizer's main purpose is to make any changes if needed
 * to the original timing of a drawing. Since the user may take as much time as
 * he or she wants to draw the drawing making long pauses and performing many operations,
 * certain adjustments are always required. An implementation of this interface is used by
 * the [FixedFrameRateRenderer].
 */
interface DrawingTimeNormalizer {
    val normalizedDuration: Long
    val normalizedDrawing: List<TimedSegment>
    fun normalizeDrawing(segments: List<TimedSegment>)
}