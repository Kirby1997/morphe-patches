package io.github.kirby1997.patches.feeld

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import io.github.kirby1997.patches.shared.Constants

// Feeld is a React Native + Hermes app. Profile avatars (including the paywalled
// "who likes you" grid) render through expo-image. When a profile's `isBlurred`
// flag is set, the JS Avatar component still passes the real photo URI to the
// native ExpoImage view and asks it to blur — the blur is a client-side Glide
// transform applied to the already-downloaded bitmap, not a server-side one.
//
// ExpoImageViewWrapper's Glide-request builder only attaches the blur transform
// when its `blurRadius` Integer field is non-null (`if-nez ... return sharp
// request` otherwise). Force the setter to store null and every expo-image blur
// becomes a no-op, so the real photo renders sharp.
//
// NOTE: this only unblurs photos whose real pixels actually reach the device.
// Fully hidden photos (the isPhotoHidden / placeholder-avatar path) and the
// "hidden bio" (a server-withheld field revealed only after a mutual like/Ping)
// carry no on-device data and cannot be revealed by any client patch.

object SetBlurRadiusFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Ljava/lang/Integer;"),
    definingClass = "Lexpo/modules/image/ExpoImageViewWrapper;",
    name = "setBlurRadius\$expo_image_release",
)

@Suppress("unused")
val unblurPhotosPatch = bytecodePatch(
    name = "Unblur profile photos",
    description = "Neutralises the expo-image blur Feeld applies to gated profile photos so the underlying image renders sharp. Only affects photos whose pixels reach the device; server-hidden photos and the hidden bio are unaffected.",
) {
    compatibleWith(Constants.FEELD)

    execute {
        // Force blurRadius to null. The Glide-request builder skips the blur
        // transform entirely when the field is null, leaving the image sharp.
        SetBlurRadiusFingerprint.method.addInstructions(
            0,
            """
                const/4 p1, 0x0
            """,
        )
    }
}
