package app.anidro.modules.main

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import app.anidro.R
import app.anidro.common.DrawingColorsPalette
import app.anidro.common.DrawingPaintBuilder
import app.anidro.common.viewBinding
import app.anidro.databinding.FragmentBrushSetupBinding
import app.anidro.modules.main.views.ColorPickerView.OnColorSelectedCallbackListener

/**
 * A dialog for selecting the drawing brush color and size. It contains a custom color picker,
 * a slider for the brush size and a preview of the resulting brush
 */
class BrushSetupDialogFragment : DialogFragment() {
    private val viewBinding by viewBinding(FragmentBrushSetupBinding::bind)

    private val drawingColorsPalette by lazy { DrawingColorsPalette(resources) }

    // Local state
    private var currentSize = 0
    private var currentColorIndex = 0
    private var currentColorLightnessIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_brush_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.run {
            okButton.setOnClickListener { onOkClicked() }
            if (savedInstanceState != null) {
                currentColorIndex = savedInstanceState.getInt(STATE_EXTRA_CURRENT_COLOR_INDEX, DrawingColorsPalette.DEFAULT_BRUSH_COLOR_INDEX)
                currentColorLightnessIndex = savedInstanceState.getInt(STATE_EXTRA_CURRENT_COLOR_LIGHTNESS_INDEX,
                        DrawingColorsPalette.DEFAULT_BRUSH_COLOR_LIGHTNESS_INDEX)
                currentSize = savedInstanceState.getInt(STATE_EXTRA_CURRENT_SIZE, DrawingPaintBuilder.DEFAULT_STROKE_WIDTH_DP)
            } else {
                currentColorIndex = arguments!!.getInt(ARG_EXTRA_INITIAL_COLOR_INDEX, DrawingColorsPalette.DEFAULT_BRUSH_COLOR_INDEX)
                currentColorLightnessIndex = arguments!!.getInt(ARG_EXTRA_INITIAL_COLOR_LIGHTNESS_INDEX,
                        DrawingColorsPalette.DEFAULT_BRUSH_COLOR_LIGHTNESS_INDEX)
                currentSize = arguments!!.getInt(ARG_EXTRA_INITIAL_SIZE, DrawingPaintBuilder.DEFAULT_STROKE_WIDTH_DP)
            }
            colorPickerGrid.setOnColorSelectedCallbackListener(baseColorSelectedCallback)
            colorPickerLightness.setOnColorSelectedCallbackListener(colorLightnessSelectedCallback)
            brushSize.setOnSeekBarChangeListener(sizeChangedListener)
            val allBaseColors = drawingColorsPalette.getColorsForLightness(DrawingColorsPalette.BASE_COLOR_LIGHTNESS_INDEX)
            colorPickerGrid.setColors(allBaseColors, 0, allBaseColors.size, 2)
            colorPickerLightness.setColors(drawingColorsPalette.getLightnessesForColor(currentColorIndex))
            brushSize.progress = currentSize
            updatePreview()
        }
    }

    override fun onStart() {
        super.onStart()
        val width = resources.getDimensionPixelSize(R.dimen.brush_setup_dialog_width)
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_EXTRA_CURRENT_SIZE, currentSize)
        outState.putInt(STATE_EXTRA_CURRENT_COLOR_INDEX, currentColorIndex)
        outState.putInt(STATE_EXTRA_CURRENT_COLOR_LIGHTNESS_INDEX, currentColorLightnessIndex)
    }

    // On OK click
    private fun onOkClicked() {
        val parentActivity: Activity? = activity
        if (parentActivity != null) {
            if (parentActivity is BrushSetupCallback) {
                (parentActivity as BrushSetupCallback).onSetupBrush(currentSize, currentColorIndex, currentColorLightnessIndex)
            }
        }
        dismiss()
    }

    // Callbacks for views
    private val baseColorSelectedCallback = OnColorSelectedCallbackListener { _, index ->
        currentColorIndex = index
        currentColorLightnessIndex = DrawingColorsPalette.BASE_COLOR_LIGHTNESS_INDEX
        viewBinding.colorPickerLightness.setColors(drawingColorsPalette.getLightnessesForColor(currentColorIndex))
        updatePreview()
    }
    private val colorLightnessSelectedCallback = OnColorSelectedCallbackListener { _, index ->
        currentColorLightnessIndex = index
        updatePreview()
    }
    private val sizeChangedListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                if (progress > 0) {
                    currentSize = progress
                    updatePreview()
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }

    // Helper methods
    private fun updatePreview() {
        viewBinding.brushPreview.setColorAndSize(
                drawingColorsPalette.getColorWithLightness(currentColorIndex, currentColorLightnessIndex),
                currentSize)
    }

    /**
     * A callback interface that should be implemented by the calling activity
     */
    interface BrushSetupCallback {
        fun onSetupBrush(size: Int, colorIndex: Int, lightnessIndex: Int)
    }

    companion object {
        const val TAG = "SETUP_BRUSH_DIALOG"

        @JvmStatic
        fun isAlreadyShown(manager: FragmentManager): Boolean {
            return manager.findFragmentByTag(TAG) != null
        }

        private const val STATE_EXTRA_CURRENT_SIZE = "current_size"
        private const val STATE_EXTRA_CURRENT_COLOR_INDEX = "current_color_index"
        private const val STATE_EXTRA_CURRENT_COLOR_LIGHTNESS_INDEX = "current_color_lightness_index"
        private const val ARG_EXTRA_INITIAL_SIZE = "initial_size"
        private const val ARG_EXTRA_INITIAL_COLOR_INDEX = "initial_color_index"
        private const val ARG_EXTRA_INITIAL_COLOR_LIGHTNESS_INDEX = "initial_color_lightness_index"

        @JvmStatic
        fun getInstance(initialSize: Int, initialColorIndex: Int,
                        initialColorLightnessIndex: Int): BrushSetupDialogFragment {
            val fragment = BrushSetupDialogFragment()
            val arguments = Bundle()
            arguments.putInt(ARG_EXTRA_INITIAL_SIZE, initialSize)
            arguments.putInt(ARG_EXTRA_INITIAL_COLOR_INDEX, initialColorIndex)
            arguments.putInt(ARG_EXTRA_INITIAL_COLOR_LIGHTNESS_INDEX, initialColorLightnessIndex)
            fragment.arguments = arguments
            return fragment
        }
    }
}