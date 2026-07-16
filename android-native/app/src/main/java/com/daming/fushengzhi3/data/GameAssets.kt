package com.daming.fushengzhi3.data

object GameImages {
    const val V3MainMenuBg = "image/v3_main_menu_bg.png"
    const val V3MainLogo = "image/v3_main_logo.png"
    const val V3DossierBg = "image/v3_dossier_bg.png"
    const val V3CountyMap = "image/v3_county_map.png"
    const val V3WorldMap = "image/v3_world_map_scroll.png"
    const val V3GenealogyBg = "image/v3_genealogy_light_bg.png"
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

    val v3AvatarPortraits = mapOf(
        "baby" to "image/avatars/v3_avatar_baby.png",
        "male_youth" to "image/avatars/v3_avatar_male_youth.png",
        "male_middle" to "image/avatars/v3_avatar_male_middle.png",
        "male_elder" to "image/avatars/v3_avatar_male_elder.png",
        "female_youth" to "image/avatars/v3_avatar_female_youth.png",
        "female_middle" to "image/avatars/v3_avatar_female_middle.png",
        "female_elder" to "image/avatars/v3_avatar_female_elder.png"
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
    V3PageTurn,
    V3SpecialAction,
    V3Success,
    V3Failure,
    V3ScrollOpen,
    V3YearSummary,
    V3Warning,
    V3ResourceSettle
}

object GameAudioAssets {
    val bgm = mapOf(
        BgmKey.V3County to "audio/v3_county_dossier_theme.ogg"
    )

    val sfx = mapOf(
        SfxKey.UiClick to "audio/sfx/v3_woodblock_click.ogg",
        SfxKey.UiSelect to "audio/sfx/v3_brush_select.ogg",
        SfxKey.UiTabSwitch to "audio/sfx/v3_scroll_open.ogg",
        SfxKey.MonthAdvance to "audio/sfx/v3_coin_grain_settle.ogg",
        SfxKey.V3Edict to "audio/sfx/v3_official_edict_stamp.ogg",
        SfxKey.V3Build to "audio/sfx/v3_county_build.ogg",
        SfxKey.V3Dispute to "audio/sfx/v3_branch_dispute.ogg",
        SfxKey.V3Finale to "audio/sfx/v3_final_chronicle.ogg",
        SfxKey.V3PageTurn to "audio/sfx/v3_page_turn_genealogy.ogg",
        SfxKey.V3SpecialAction to "audio/sfx/v3_scroll_open.ogg",
        SfxKey.V3Success to "audio/sfx/v3_year_summary_chime.ogg",
        SfxKey.V3Failure to "audio/sfx/v3_warning_drum.ogg",
        SfxKey.V3ScrollOpen to "audio/sfx/v3_scroll_open.ogg",
        SfxKey.V3YearSummary to "audio/sfx/v3_year_summary_chime.ogg",
        SfxKey.V3Warning to "audio/sfx/v3_warning_drum.ogg",
        SfxKey.V3ResourceSettle to "audio/sfx/v3_coin_grain_settle.ogg"
    )
}
