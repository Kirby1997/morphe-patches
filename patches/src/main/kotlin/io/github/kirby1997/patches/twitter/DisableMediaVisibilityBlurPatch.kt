package io.github.kirby1997.patches.twitter

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import io.github.kirby1997.patches.shared.Constants

// X 12.7.1 gates every media interstitial - the ordinary "sensitive media" cover
// AND the age-restricted "verify your age" cover - behind one static predicate:
//
//   com.twitter.model.mediavisibility.d.a(g) : Z
//
// It returns true when the tweet's MediaVisibilityResults (field e.B, type g)
// either carries a MediaInterstitial whose payload is a BlurredImageInterstitial
// (g.a.a instanceof b) or carries a BlurredImageInterstitial directly (g.b != null).
//
// Three call sites depend on it and nothing else decides blurring:
//   - com.twitter.model.core.e.J0()               - pure delegate: return d.a(this.B)
//   - com.twitter.tweetview.core.l.a(a0, i, a0)   - the tweetview sensitive-media state
//   - SensitiveMediaBlurPreviewInterstitialViewDelegateBinder.d(k, a0, a0, Z)
//
// The delegate binder is where the two variants diverge: it reads
// BlurredImageInterstitial.e (com.twitter.model.mediavisibility.c) and, when it equals
// c.AgeVerificationPrompt, labels the button for age verification instead of "show".
// Both variants share this predicate, which is why Piko's "Show sensitive media"
// (a different, account-settings-level surface) leaves the age-restricted cover in place.
//
// Forcing false makes all three treat the media as unrestricted: the interstitial is
// never inflated or made visible and the underlying media renders directly.
object MediaVisibilityHasBlurInterstitialFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Lcom/twitter/model/mediavisibility/g;"),
    definingClass = "Lcom/twitter/model/mediavisibility/d;",
    name = "a",
)

@Suppress("unused")
val disableMediaVisibilityBlurPatch = bytecodePatch(
    name = "Show age-restricted and sensitive media",
    description = "Removes the blurred cover over age-restricted media (the \"verify your age\" " +
        "interstitial) as well as the ordinary sensitive-media cover, by forcing X's single " +
        "media-visibility predicate to report that a post carries no blur interstitial. " +
        "Complements Piko's \"Show sensitive media\", which does not cover the age-verification variant.",
) {
    compatibleWith(Constants.TWITTER_12_7_1)

    execute {
        // .registers 3 with one parameter, so v0 and v1 are locals - safe to write.
        MediaVisibilityHasBlurInterstitialFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
