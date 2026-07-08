package io.github.kirby1997.patches.meetup

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import io.github.kirby1997.patches.shared.Constants

// Meetup embeds the OneTrust cookie-consent SDK. IntroFragment gates the login
// screen behind OTPublishersHeadlessSDK.shouldShowBanner(). We intercept it to
// programmatically save a proper "Banner - Reject All" decision through OneTrust's
// own API, then return false so the banner doesn't render. OneTrust writes real
// per-category consent to SharedPreferences and broadcasts the usual consent-change
// intents, so downstream lookups return natural values (0 rejected, 1 Strictly
// Necessary) without breaking functional-category features (e.g. Maps tiles).

private const val OT_SDK = "Lcom/onetrust/otpublishers/headless/Public/OTPublishersHeadlessSDK;"

object OtShouldShowBannerFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = listOf(),
    definingClass = OT_SDK,
    name = "shouldShowBanner",
)

@Suppress("unused")
val disableCookieBannerPatch = bytecodePatch(
    name = "Auto-reject cookie banner",
    description = "Programmatically persists a 'Banner - Reject All' decision through OneTrust and suppresses the consent banner, so the user is never prompted and non-essential tracking is rejected on every launch.",
) {
    compatibleWith(Constants.MEETUP)

    execute {
        OtShouldShowBannerFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "Banner - Reject All"
                invoke-virtual {p0, v0}, $OT_SDK->saveConsent(Ljava/lang/String;)V
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
