package com.daming.fushengzhi3.data

object GameImages {
    const val V3MainMenuBg = "image/v3_main_menu_bg.png"
    const val V3MainLogo = "image/v3_main_logo.png"
    const val V3DossierBg = "image/v3_dossier_bg.png"
    const val V3CountyMap = "image/v3_county_map.png"
    const val V3Icon = "image/v3_icon.png"
    const val V3MapBgPlain = "image/v3_map_bg_plain.png"
    const val V3IconSilver = "image/v3_icon_silver.png"
    const val V3IconGrain = "image/v3_icon_grain.png"
    const val V3IconPopulation = "image/v3_icon_population.png"
    const val V3IconIndustry = "image/v3_icon_industry.png"
    const val V3UiGenealogyBook = "image/v3_ui_genealogy_book.png"
    const val V3UiPageLeft = "image/v3_ui_page_left.png"
    const val V3UiPageRight = "image/v3_ui_page_right.png"
    const val V3UiEventPanel = "image/v3_ui_event_panel.png"
    const val V3UiExamPaper = "image/v3_ui_exam_paper.png"
    const val V3UiBattleReport = "image/v3_ui_battle_report.png"
    const val V3UiSettingsScroll = "image/v3_ui_settings_scroll.png"

    val v3SiteIcons = mapOf(
        "shrine" to "image/v3_site_shrine.png",
        "farmland" to "image/v3_site_farmland.png",
        "market" to "image/v3_site_market.png",
        "yamen" to "image/v3_site_yamen.png",
        "fort" to "image/v3_site_fort.png",
        "dock" to "image/v3_site_dock.png",
        "academy" to "image/v3_site_academy.png",
        "clinic" to "image/v3_site_clinic.png",
        "mountain_pass" to "image/v3_site_mountain_pass.png"
    )
    val v3WorldRegionIcons = mapOf(
        "home_county" to "image/v3_world_home_county.png",
        "river_prefecture" to "image/v3_world_river_prefecture.png",
        "mountain_prefecture" to "image/v3_world_mountain_prefecture.png",
        "south_province" to "image/v3_world_south_province.png",
        "north_capital" to "image/v3_world_north_capital.png",
        "all_realm" to "image/v3_world_all_realm.png"
    )
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
    V3Finale,
    V3PageTurn
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
        SfxKey.V3Finale to "audio/sfx/v3_final_chronicle.ogg",
        SfxKey.V3PageTurn to "audio/sfx/v3_page_turn_genealogy.ogg"
    )
}
