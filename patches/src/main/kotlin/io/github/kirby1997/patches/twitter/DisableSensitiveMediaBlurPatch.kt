package io.github.kirby1997.patches.twitter

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import io.github.kirby1997.patches.shared.Constants

// The "content warning" blur on timeline media is the
// SensitiveMediaBlurPreviewInterstitialView. Whether it is inflated and made VISIBLE
// is decided by a single static predicate com.twitter.tweetview.core.k.a(t, i, x) : Z,
// which returns true only when the tweetview's sensitive-media state resolves to e$a.
// Two call sites gate on it (the ViewStub inflate decision and the delegate binder
// that sets the blur view VISIBLE/GONE). Forcing false makes both treat media as
// non-sensitive: the stub never inflates and the binder sets the overlay GONE.
object ShouldShowSensitiveMediaInterstitialFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(
        "Lcom/twitter/tweetview/core/t;",
        "Lcom/twitter/ui/renderable/i;",
        "Lcom/twitter/account/model/x;",
    ),
    definingClass = "Lcom/twitter/tweetview/core/k;",
    name = "a",
)

@Suppress("unused")
val disableSensitiveMediaBlurPatch = bytecodePatch(
    name = "Disable sensitive-media content-warning blur",
    description = "Forces the timeline sensitive-media interstitial decision to false so downloaded media " +
        "renders without the \"content warning\" blur overlay. RE artifact for X 12.4.1, which is native-" +
        "PairIP-wrapped and not sideload-patchable unrooted — default-off.",
    default = false,
) {
    compatibleWith(Constants.TWITTER)

    execute {
        ShouldShowSensitiveMediaInterstitialFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
