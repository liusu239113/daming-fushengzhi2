package com.daming.fushengzhi3.data

object GameImages {
    const val V3DossierBg = "image/v3_dossier_bg.png"
    const val V3CountyMap = "image/v3_county_map.png"
    const val V3Icon = "image/v3_icon.png"
}

enum class BgmKey { V3County }

enum class SfxKey {
    UiClick,
    UiSelect,
    UiTabSwitch,
    MonthAdvance,
    V3Edict,
    V3Build,
    V3Dispute,
    V3Finale
}

object GameAudioAssets {
    val bgm = mapOf(
        BgmKey.V3County to "audio/v3_county_dossier_theme.ogg"
    )

    val sfx = mapOf(
        SfxKey.UiClick to "audio/sfx/ui_click.ogg",
        SfxKey.UiSelect to "audio/sfx/ui_select.ogg",
        SfxKey.UiTabSwitch to "audio/sfx/ui_tab_switch.ogg",
        SfxKey.MonthAdvance to "audio/sfx/month_advance.ogg",
        SfxKey.V3Edict to "audio/sfx/v3_official_edict_stamp.ogg",
        SfxKey.V3Build to "audio/sfx/v3_county_build.ogg",
        SfxKey.V3Dispute to "audio/sfx/v3_branch_dispute.ogg",
        SfxKey.V3Finale to "audio/sfx/v3_final_chronicle.ogg"
    )
}
