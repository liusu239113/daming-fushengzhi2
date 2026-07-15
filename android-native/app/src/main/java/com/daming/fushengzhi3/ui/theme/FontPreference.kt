package com.daming.fushengzhi3.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class FontPreference(context: Context) {
    private val prefs = context.getSharedPreferences("ui_font_pref", Context.MODE_PRIVATE)

    var style by mutableStateOf(FontStyleKey.fromId(prefs.getString("style", FontStyleKey.Hei.id)))
        private set

    fun updateStyle(next: FontStyleKey) {
        style = next
        prefs.edit().putString("style", next.id).apply()
    }
}

enum class FontStyleKey(val id: String, val label: String, val desc: String) {
    Pixel("pixel", "像素宋意", "像素中文，粗硬边，偏游戏化"),
    Hei("hei", "厚重黑体", "粗体高对比，适合标题和按钮"),
    Serif("serif", "明式宋体", "衬线字形，偏古籍感"),
    Mono("mono", "匠作等宽", "数字清楚，适合账本和数值");

    companion object {
        fun fromId(id: String?): FontStyleKey = entries.firstOrNull { it.id == id } ?: Hei
    }
}
