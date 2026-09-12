package com.example.ui.components

import android.content.Context
import androidx.annotation.DrawableRes
import com.example.R

object DrawableResolver {
    @DrawableRes
    fun getDrawableId(context: Context, name: String): Int {
        return when (name) {
            "img_apt_vake" -> R.drawable.img_apt_vake
            "img_apt_vera" -> R.drawable.img_apt_vera
            "img_apt_saburtalo" -> R.drawable.img_apt_saburtalo
            "img_apt_mtatsminda" -> R.drawable.img_apt_mtatsminda
            "ic_rentch_logo" -> R.drawable.ic_rentch_logo
            else -> {
                val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
                if (resId != 0) resId else R.drawable.img_apt_vake
            }
        }
    }
}
