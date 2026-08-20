package io.github.kirby1997.patches.twitter

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import io.github.kirby1997.patches.shared.Constants

// X 12.7.1 covers restricted media with SensitiveMediaBlurPreviewInterstitialView, used
// for both the ordinary "sensitive media" cover and the age-restricted "verify your age"
// cover. Which one is drawn is decided in the binder by reading
// BlurredImageInterstitial.e (com.twitter.model.mediavisibility.c): when it equals
// c.AgeVerificationPrompt the button is labelled for age verification. Piko's
// "Show sensitive media" works on a different, account-settings-level surface and
// leaves the age-verification cover in place.
//
// Whether the cover is shown at all comes from this tweetview state predicate:
//
//   com.twitter.tweetview.core.l.a(a0, i, a0) : Z          (12.4.1's k.a(t, i, x))
//
// It resolves the tweet's sensitive-media state and returns true only for f$a.
// SensitiveMediaBlurPreviewInterstitialViewDelegateBinder.d(...) calls it first: false
// takes the early branch that binds the media entity and sets the interstitial View
// GONE, which leaves the media underneath rendering normally.
//
// Deliberately NOT patched: com.twitter.model.mediavisibility.d.a(g), the lower helper
// this predicate calls. It looks like the cleaner chokepoint but it also backs
// com.twitter.model.core.e.J0(), which a dozen unrelated call sites consult (timeline
// binding, QuoteView, tweet actions). Forcing it false makes the media flash
// uncensored and then disappear entirely - the surrounding pipeline stops treating the
// post as carrying media at all. Patch the narrow tweetview predicate instead: three
// call sites, all of them cover-related.
object SensitiveMediaInterstitialStateFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(
        "Lcom/twitter/tweetview/core/a0;",
        "Lcom/twitter/ui/renderable/i;",
        "Lcom/twitter/account/model/a0;",
    ),
    definingClass = "Lcom/twitter/tweetview/core/l;",
    name = "a",
)

@Suppress("unused")
val disableMediaVisibilityBlurPatch = bytecodePatch(
    name = "Show age-restricted and sensitive media",
    description = "Removes the blurred cover over age-restricted media (the \"verify your age\" " +
        "interstitial) as well as the ordinary sensitive-media cover, so the media underneath renders " +
        "directly. Complements Piko's \"Show sensitive media\", which does not cover the age-verification variant.",
) {
    compatibleWith(Constants.TWITTER_12_7_1)

    execute {
        // .registers 4 with three parameters, so v0 is a local - safe to write.
        SensitiveMediaInterstitialStateFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
