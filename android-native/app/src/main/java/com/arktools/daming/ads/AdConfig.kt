package com.arktools.daming.ads

object AdConfig {
    var appId: Long = 0L
        private set
    var rewardedPlacementId: String = ""
        private set
    var privacyPolicyUrl: String = ""
        private set
    var debug: Boolean = false
        private set

    fun configure(
        appId: Long,
        rewardedPlacementId: String,
        privacyPolicyUrl: String,
        debug: Boolean
    ) {
        this.appId = appId
        this.rewardedPlacementId = rewardedPlacementId
        this.privacyPolicyUrl = privacyPolicyUrl
        this.debug = debug
    }
}
