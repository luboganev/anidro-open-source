package app.anidro.modules.main

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import app.anidro.R
import app.anidro.common.DrawingColorsPalette
import app.anidro.common.viewBinding
import app.anidro.databinding.FragmentBackgroundColorBinding
import app.anidro.modules.main.views.ColorPickerView.OnColorSelectedCallbackListener

/**
 * A dialog for selecting the background color of the drawing. It contains a custom color picker.
 */
class BackgroundColorDialogFragment : DialogFragment() {
    private val viewBinding by viewBinding(FragmentBackgroundColorBinding::bind)

    private val drawingColorsPalette by lazy { DrawingColorsPalette(resources) }

    // Local state
    private var currentColorIndex = 0
    private var currentColorLightnessIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_background_color, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.run {
            okButton.setOnClickListener { onOkClicked() }
            if (savedInstanceState != null) {
                currentColorIndex = savedInstanceState.getInt(STATE_EXTRA_CURRENT_COLOR_INDEX, DrawingColorsPalette.DEFAULT_BACKGROUND_COLOR_INDEX)
                currentColorLightnessIndex = savedInstanceState.getInt(STATE_EXTRA_CURRENT_COLOR_LIGHTNESS_INDEX,
                        DrawingColorsPalette.DEFAULT_BACKGROUND_COLOR_LIGHTNESS_INDEX)
            } else {
                currentColorIndex = arguments!!.getInt(ARG_EXTRA_INITIAL_COLOR_INDEX, DrawingColorsPalette.DEFAULT_BACKGROUND_COLOR_INDEX)
                currentColorLightnessIndex = arguments!!.getInt(ARG_EXTRA_INITIAL_COLOR_LIGHTNESS_INDEX,
                        DrawingColorsPalette.DEFAULT_BACKGROUND_COLOR_LIGHTNESS_INDEX)
            }
            colorPickerGrid.setOnColorSelectedCallbackListener(baseColorSelectedCallback)
            colorPickerLightness.setOnColorSelectedCallbackListener(colorLightnessSelectedCallback)
            val allBaseColors = drawingColorsPalette.getColorsForLightness(DrawingColorsPalette.BASE_COLOR_LIGHTNESS_INDEX)
            colorPickerGrid.setColors(allBaseColors, 0, allBaseColors.size, 2)
            colorPickerLightness.setColors(drawingColorsPalette.getLightnessesForColor(currentColorIndex))
            updatePreview()
        }
    }

    override fun onStart() {
        super.onStart()
        val width = resources.getDimensionPixelSize(R.dimen.background_color_dialog_width)
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_EXTRA_CURRENT_COLOR_INDEX, currentColorIndex)
        outState.putInt(STATE_EXTRA_CURRENT_COLOR_LIGHTNESS_INDEX, currentColorLightnessIndex)
    }

    // On OK click
    private fun onOkClicked() {
        val parentActivity: Activity? = activity
        if (parentActivity != null) {
            if (parentActivity is BackgroundColorCallback) {
                (parentActivity as BackgroundColorCallback).onSelectBackgroundColor(currentColorIndex, currentColorLightnessIndex)
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

    // Helper methods
    private fun updatePreview() {
        viewBinding.backgroundColorPreview.setBackgroundColor(drawingColorsPalette.getColorWithLightness(currentColorIndex, currentColorLightnessIndex))
    }

    /**
     * A callback interface that should be implemented by the calling activity
     */
    interface BackgroundColorCallback {
        fun onSelectBackgroundColor(colorIndex: Int, lightnessIndex: Int)
    }

    companion object {
        const val TAG = "BACKGROUND_COLOR_DIALOG"

        @JvmStatic
        fun isAlreadyShown(manager: FragmentManager): Boolean {
            return manager.findFragmentByTag(TAG) != null
        }

        private const val STATE_EXTRA_CURRENT_COLOR_INDEX = "current_color_index"
        private const val STATE_EXTRA_CURRENT_COLOR_LIGHTNESS_INDEX = "current_color_lightness_index"
        private const val ARG_EXTRA_INITIAL_COLOR_INDEX = "initial_color_index"
        private const val ARG_EXTRA_INITIAL_COLOR_LIGHTNESS_INDEX = "initial_color_lightness_index"

        // Fragment methods
        @JvmStatic
        fun getInstance(initialColorIndex: Int,
                        initialColorLightnessIndex: Int): BackgroundColorDialogFragment {
            val fragment = BackgroundColorDialogFragment()
            val arguments = Bundle()
            arguments.putInt(ARG_EXTRA_INITIAL_COLOR_INDEX, initialColorIndex)
            arguments.putInt(ARG_EXTRA_INITIAL_COLOR_LIGHTNESS_INDEX, initialColorLightnessIndex)
            fragment.arguments = arguments
            return fragment
        }
    }
}