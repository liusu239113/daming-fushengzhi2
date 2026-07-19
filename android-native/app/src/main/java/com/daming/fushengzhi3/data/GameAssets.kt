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
    const val V3InkLandscapeBg = "image/v3_ink_landscape_bg.png"
    const val V3UiInkPanel = "image/v3_ui_ink_panel.png"
    const val V3UiPrimaryButton = "image/v3_ui_primary_button.png"
    const val V3UiSecondaryButton = "image/v3_ui_secondary_button.png"
    const val V3UiResourcePlaque = "image/v3_ui_resource_plaque.png"
    const val V3UiStylePreview = "image/v3_ui_style_preview.png"
    const val MingyunHomeBg = "image/mingyun_review/审核_V3_家业简洁背景.png"
    const val MingyunClanBg = "image/mingyun_review/审核_V3_宗族简洁背景.png"
    const val MingyunPeopleBg = "image/mingyun_review/审核_V3_族人简洁背景.png"
    const val MingyunStrategyBg = "image/mingyun_review/审核_V3_大势简洁背景.png"
    const val MingyunPrimaryButton = "image/mingyun_review/审核_V3_主按钮_普通.png"
    const val MingyunPrimaryButtonDisabled = "image/mingyun_review/审核_V3_主按钮_禁用.png"
    const val MingyunSmallButton = "image/mingyun_review/审核_V3_小按钮_普通.png"
    const val MingyunSmallButtonSelected = "image/mingyun_review/审核_V3_小按钮_选中.png"
    const val MingyunSmallButtonDisabled = "image/mingyun_review/审核_V3_小按钮_禁用.png"
    const val MingyunPanel = "image/mingyun_review/审核_V3_通用内容面板.png"
    const val MingyunResourcePlaque = "image/mingyun_review/审核_V3_资源牌.png"
    const val MingyunDialog = "image/mingyun_review/审核_V3_通用弹窗.png"

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
        "neighbor_county" to "image/v3_world_home_county.png",
        "river_prefecture" to "image/v3_world_river_prefecture.png",
        "mountain_prefecture" to "image/v3_world_mountain_prefecture.png",
        "lake_province" to "image/v3_world_river_prefecture.png",
        "coast_province" to "image/v3_world_south_province.png",
        "south_province" to "image/v3_world_south_province.png",
        "shandong_corridor" to "image/v3_world_mountain_prefecture.png",
        "liaodong_front" to "image/v3_world_north_capital.png",
        "north_capital" to "image/v3_world_north_capital.png",
        "jiangsea_gate" to "image/v3_world_south_province.png",
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

    val v3AvatarVariants = mapOf(
        "baby" to listOf(
            v3AvatarPortraits.getValue("baby"),
            "image/avatars/pd_baby_1_20260511205339.png",
            "image/avatars/pd_baby_2_20260511205608.png",
            "image/avatars/av_baby_a_20260511210946.png",
            "image/avatars/av_baby_b_20260511210956.png",
            "image/avatars/av_baby_c_20260511210945.png",
            "image/avatars/av_baby_d_20260511211009.png"
        ),
        "male_child" to listOf(
            "image/avatars/pd_boy_1_20260511205352.png",
            "image/avatars/av_boy_a_20260511211044.png",
            "image/avatars/av_boy_b_20260511211028.png",
            "image/avatars/av_boy_c_20260511211027.png"
        ),
        "female_child" to listOf(
            "image/avatars/pd_girl_1_20260511205347.png",
            "image/avatars/av_girl_a_20260511211029.png",
            "image/avatars/av_girl_b_20260511211030.png",
            "image/avatars/av_girl_c_20260511211029.png"
        ),
        "male_youth" to listOf(
            v3AvatarPortraits.getValue("male_youth"),
            "image/avatars/paperdoll_base_v2_20260511205100.png",
            "image/avatars/pd_male_youth_2_20260511205513.png",
            "image/avatars/pd_male_youth_3_20260511205510.png",
            "image/avatars/av_m_youth_a_20260511210138.png",
            "image/avatars/av_m_youth_b_20260511210150.png",
            "image/avatars/av_m_youth_c_20260511210141.png",
            "image/avatars/av_m_youth_d_20260511210142.png",
            "image/avatars/av_m_youth_e_20260511210149.png"
        ),
        "female_youth" to listOf(
            v3AvatarPortraits.getValue("female_youth"),
            "image/avatars/pd_female_youth_1_20260511205344.png",
            "image/avatars/pd_female_youth_2_20260511205502.png",
            "image/avatars/pd_female_youth_3_20260511205503.png",
            "image/avatars/av_f_youth_a_20260511210215.png",
            "image/avatars/av_f_youth_b_20260511210208.png",
            "image/avatars/av_f_youth_c_20260511210207.png",
            "image/avatars/av_f_youth_d_20260511210212.png",
            "image/avatars/av_f_youth_e_20260511210213.png"
        ),
        "male_adult" to listOf(
            "image/avatars/av_m_adult_a_20260512082153.png",
            "image/avatars/av_m_adult_b_20260512081743.png",
            "image/avatars/av_m_adult_c_20260512082336.png",
            "image/avatars/av_m_adult_d_20260512081652.png",
            "image/avatars/av_m_adult_e_20260512082337.png",
            "image/avatars/av_m_adult_f_20260512082413.png",
            "image/avatars/av_m_adult_g_20260512141103.png",
            "image/avatars/av_m_adult_h_20260512141024.png",
            "image/avatars/av_m_adult_i_20260512141023.png",
            "image/avatars/av_m_adult_j_20260512141026.png",
            "image/avatars/av_m_adult_k_20260512141046.png",
            "image/avatars/av_m_adult_l_20260512141023.png"
        ),
        "female_adult" to listOf(
            "image/avatars/av_f_adult_a_20260512081928.png",
            "image/avatars/av_f_adult_b_20260512081939.png",
            "image/avatars/av_f_adult_d_20260512081643.png",
            "image/avatars/av_f_adult_e_20260512081750.png",
            "image/avatars/av_f_adult_g_20260512140907.png",
            "image/avatars/av_f_adult_h_20260512140928.png",
            "image/avatars/av_f_adult_i_20260512140617.png",
            "image/avatars/av_f_adult_i_20260512140909.png",
            "image/avatars/av_f_adult_j_20260512140918.png",
            "image/avatars/av_f_adult_k_20260512140915.png",
            "image/avatars/av_f_adult_l_20260512140905.png",
            "image/avatars/av_f_adult_m_20260512141240.png",
            "image/avatars/av_f_adult_n_20260512141227.png",
            "image/avatars/av_f_adult_o_20260512141249.png",
            "image/avatars/av_f_adult_p_20260512141234.png",
            "image/avatars/av_f_adult_q_20260512141242.png"
        ),
        "male_middle" to listOf(
            v3AvatarPortraits.getValue("male_middle"),
            "image/avatars/pd_male_mid_1_20260511205341.png",
            "image/avatars/pd_male_mid_2_20260511205501.png",
            "image/avatars/av_m_mid_a_20260511210341.png",
            "image/avatars/av_m_mid_b_20260511210335.png",
            "image/avatars/av_m_mid_c_20260511210326.png",
            "image/avatars/av_m_mid_d_20260511210339.png",
            "image/avatars/av_m_mid_e_20260511210333.png"
        ),
        "female_middle" to listOf(
            v3AvatarPortraits.getValue("female_middle"),
            "image/avatars/pd_female_mid_1_20260511205341.png",
            "image/avatars/pd_female_mid_2_20260511205504.png",
            "image/avatars/av_f_mid_a_20260511210407.png",
            "image/avatars/av_f_mid_b_20260511210401.png",
            "image/avatars/av_f_mid_c_20260511210413.png",
            "image/avatars/av_f_mid_d_20260511210435.png",
            "image/avatars/av_f_mid_e_20260511210400.png"
        ),
        "male_elder" to listOf(
            v3AvatarPortraits.getValue("male_elder"),
            "image/avatars/pd_male_elder_1_20260511205340.png",
            "image/avatars/av_m_elder_a_20260511210559.png",
            "image/avatars/av_m_elder_b_20260511210551.png",
            "image/avatars/av_m_elder_c_20260511210536.png",
            "image/avatars/av_m_elder_d_20260511210556.png"
        ),
        "female_elder" to listOf(
            v3AvatarPortraits.getValue("female_elder"),
            "image/avatars/pd_female_elder_1_20260511205338.png",
            "image/avatars/av_f_elder_a_20260511210642.png",
            "image/avatars/av_f_elder_b_20260511210618.png",
            "image/avatars/av_f_elder_c_20260511210617.png",
            "image/avatars/av_f_elder_d_20260511210616.png"
        )
    )

    val v3SpousePortraits = mapOf(
        "farmer" to "image/avatars/spouses/wang_chunniang.png",
        "merchant" to "image/avatars/spouses/shen_yuniang.png",
        "scholar" to "image/avatars/spouses/chen_wanyi.png",
        "martial" to "image/avatars/spouses/zhao_yueying.png",
        "healer" to "image/avatars/spouses/gu_suwen.png",
        "gentry" to "image/avatars/spouses/zhou_minghui.png",
        "sea" to "image/avatars/spouses/lin_haitang.png",
        "chieftain" to "image/avatars/spouses/qin_zhaoxue.png",
        "scholar_male" to "image/avatars/spouses/shen_yanqiu.png",
        "merchant_male" to "image/avatars/spouses/gu_xingzhou.png",
        "martial_male" to "image/avatars/spouses/zhao_changge.png",
        "gentry_male" to "image/avatars/spouses/zhou_huaijin.png"
    )

    val v3EnemyPortraits = listOf(
        "image/avatars/enemies/v3_enemy_ref_01.png",
        "image/avatars/enemies/v3_enemy_ref_02.png",
        "image/avatars/enemies/v3_enemy_ref_03.png",
        "image/avatars/enemies/v3_enemy_ref_04.png",
        "image/avatars/enemies/v3_enemy_ref_05.png",
        "image/avatars/enemies/v3_enemy_ref_06.png",
        "image/avatars/enemies/v3_enemy_ref_07.png",
        "image/avatars/enemies/v3_enemy_ref_08.png",
        "image/avatars/enemies/v3_enemy_ref_09.png",
        "image/avatars/enemies/v3_enemy_ref_10.png",
        "image/avatars/enemies/v3_enemy_ref_11.png",
        "image/avatars/enemies/v3_enemy_ref_12.png",
        "image/avatars/enemies/v3_enemy_ref_13.png",
        "image/avatars/enemies/v3_enemy_ref_14.png",
        "image/avatars/enemies/v3_enemy_ref_15.png",
        "image/avatars/enemies/v3_enemy_ref_16.png",
        "image/avatars/enemies/v3_enemy_ref_17.png",
        "image/avatars/enemies/v3_enemy_ref_18.png",
        "image/avatars/enemies/v3_enemy_ref_19.png",
        "image/avatars/enemies/v3_enemy_ref_20.png",
        "image/avatars/enemies/v3_enemy_ref_21.png",
        "image/avatars/enemies/v3_enemy_ref_22.png",
        "image/avatars/enemies/v3_enemy_ref_23.png",
        "image/avatars/enemies/v3_enemy_ref_24.png",
        "image/avatars/enemies/v3_enemy_ref_25.png",
        "image/avatars/enemies/v3_enemy_ref_26.png",
        "image/avatars/enemies/v3_enemy_ref_27.png",
        "image/avatars/enemies/v3_enemy_ref_28.png",
        "image/avatars/enemies/v3_enemy_ref_29.png",
        "image/avatars/enemies/v3_enemy_ref_30.png",
        "image/avatars/enemies/v3_enemy_ref_31.png",
        "image/avatars/enemies/v3_enemy_ref_32.png",
        "image/avatars/enemies/v3_enemy_ref_33.png",
        "image/avatars/enemies/v3_enemy_ref_34.png",
        "image/avatars/enemies/v3_enemy_ref_35.png",
        "image/avatars/enemies/v3_enemy_ref_36.png",
        "image/avatars/enemies/v3_enemy_ref_37.png",
        "image/avatars/enemies/v3_enemy_ref_38.png",
        "image/avatars/enemies/v3_enemy_ref_39.png",
        "image/avatars/enemies/v3_enemy_ref_40.png",
        "image/avatars/enemies/v3_enemy_ref_41.png",
        "image/avatars/enemies/v3_enemy_ref_42.png",
        "image/avatars/enemies/v3_enemy_ref_43.png",
        "image/avatars/enemies/v3_enemy_ref_44.png",
        "image/avatars/enemies/v3_enemy_ref_45.png",
        "image/avatars/enemies/v3_enemy_ref_46.png",
        "image/avatars/enemies/v3_enemy_ref_47.png",
        "image/avatars/enemies/v3_enemy_ref_48.png",
        "image/avatars/enemies/v3_enemy_ref_49.png",
        "image/avatars/enemies/v3_enemy_ref_50.png"
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
