package app.anidro.modules.main.views

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import app.anidro.R
import app.anidro.models.FileType

/**
 * An adapter for the popup presenting the user with different drawing export options
 */
class SharePopupAdapter : BaseAdapter() {

    private val data = FileType.availableFileTypes

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return data[position].ordinal.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = if (convertView == null) {
            val textView = AppCompatTextView(parent.context)
            val padding = parent.resources.getDimensionPixelSize(R.dimen.share_popup_item_padding)
            val drawablePadding = parent.resources.getDimensionPixelSize(R.dimen.share_popup_item_drawable_padding)
            textView.setPadding(padding, padding, padding, padding)
            textView.compoundDrawablePadding = drawablePadding
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0f)
            textView.layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            textView
        } else {
            convertView as AppCompatTextView
        }
        val item = data[position]
        val vdc = VectorDrawableCompat.create(parent.resources, getIcon(item), parent.context.theme)
        view.setCompoundDrawablesWithIntrinsicBounds(vdc, null, null, null)
        view.setText(getText(item))
        view.tag = item
        return view
    }

    @DrawableRes
    private fun getIcon(position: FileType): Int {
        return when (position) {
            FileType.IMAGE -> R.drawable.ic_image_grey600_24dp
            FileType.GIF -> R.drawable.ic_gif_grey600_24dp
            FileType.VIDEO -> R.drawable.ic_movie_grey600_24dp
            else -> R.drawable.ic_image_grey600_24dp
        }
    }

    @StringRes
    private fun getText(position: FileType): Int {
        return when (position) {
            FileType.IMAGE -> R.string.fab_share_item_image
            FileType.GIF -> R.string.fab_share_item_gif
            FileType.VIDEO -> R.string.fab_share_item_video
            else -> R.string.fab_share_item_image
        }
    }
}