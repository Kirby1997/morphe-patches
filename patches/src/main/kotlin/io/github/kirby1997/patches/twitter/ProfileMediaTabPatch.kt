package io.github.kirby1997.patches.twitter

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import io.github.kirby1997.patches.shared.Constants

// The profile Media tab (ProfileMediaTimelineFragment, legacy timeline type 15) is loaded by
// com.twitter.api.legacy.request.urt.timelines.o, whose n0() names the GraphQL operation
// "media_timeline_v2" (UserProfileMediaTimelineQuery, safety_level UserScopedTimeline).
//
// There is no client-side NSFW gate on that tab: the fragment only configures the
// "@%s hasn't posted media" empty state, the request carries nothing but rest_id, and neither
// the legacy URT pipeline nor the Compose grid (com.x.profile.timeline.v) filters on the
// user's sensitive/interstitial flags. For an account X flags as sensitive the server simply
// returns an empty media timeline, while the same posts still come back on the Posts and
// Replies tabs. So the tab is fed from "user_with_profile_tweets_and_replies_query_v2" instead
// - the request com.twitter.profiles.requests.c builds for the Replies tab, which has exactly
// the same variables (rest_id, includeTweetVisibilityNudge) and response path
// (user_result.result.timeline_response.timeline) - and trimmed back to the owner's own posts
// with media before it is stored.
//
// Every legacy URT response passes through com.twitter.api.legacy.request.urt.t.a(z3, v2) on
// its way into the timeline database, so the trimming is done at the top of that method, by
// the extension, on the parsed response. The extension checks the timeline type itself and
// leaves every other timeline alone.
private const val MEDIA_TIMELINE_OPERATION = "media_timeline_v2"
private const val POSTS_AND_REPLIES_OPERATION = "user_with_profile_tweets_and_replies_query_v2"

private const val EXTENSION_CLASS =
    "Lio/github/kirby1997/extension/twitter/ProfileMediaTimelineFilter;"

object ProfileMediaTimelineRequestFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Lcom/twitter/api/legacy/request/urt/graphql/a;",
    parameters = listOf(),
    definingClass = "Lcom/twitter/api/legacy/request/urt/timelines/o;",
    name = "n0",
)

object TimelineResponseWriterFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Lcom/twitter/model/timeline/urt/a1;",
    parameters = listOf(
        "Lcom/twitter/model/timeline/urt/z3;",
        "Lcom/twitter/model/timeline/v2;",
    ),
    definingClass = "Lcom/twitter/api/legacy/request/urt/t;",
    name = "a",
)

// .registers 41 with two parameters, so p1 (the response) is v39 - out of reach of the
// non-range invoke forms.
private fun filterCall(method: String) =
    "invoke-static/range {p1 .. p1}, $EXTENSION_CLASS->$method(Ljava/lang/Object;)V"

@Suppress("unused")
val showMediaTabForSensitiveProfilesPatch = bytecodePatch(
    name = "Show media tab for sensitive profiles",
    description = "Rebuilds the profile Media tab from the account's posts and replies, keeping " +
        "only their own posts with media. X returns an empty media timeline for accounts it flags " +
        "as sensitive (\"@user hasn't posted media\"), even though the same posts load on the " +
        "Posts and Replies tabs. Applies to every profile's Media tab, not only flagged ones.",
) {
    compatibleWith(Constants.TWITTER_12_7_1)
    extendWith("extensions/extension.mpe")

    execute {
        val request = ProfileMediaTimelineRequestFingerprint.method
        val (index, instruction) = request.instructions.withIndex().firstOrNull { (_, instruction) ->
            (instruction.opcode == Opcode.CONST_STRING || instruction.opcode == Opcode.CONST_STRING_JUMBO) &&
                ((instruction as ReferenceInstruction).reference as StringReference).string ==
                MEDIA_TIMELINE_OPERATION
        } ?: throw PatchException("\"$MEDIA_TIMELINE_OPERATION\" not found in the media timeline request")

        val register = (instruction as OneRegisterInstruction).registerA
        request.replaceInstruction(index, "const-string v$register, \"$POSTS_AND_REPLIES_OPERATION\"")

        TimelineResponseWriterFingerprint.method.addInstructions(0, filterCall("keepOwnMedia"))
    }
}

@Suppress("unused")
val hideMediaTabGifRepliesPatch = bytecodePatch(
    name = "Hide GIF replies from media tab",
    description = "Removes replies whose attached media is an animated GIF from the profile " +
        "Media tab, so reaction-GIF replies don't crowd out the account's own photos and videos.",
) {
    compatibleWith(Constants.TWITTER_12_7_1)
    extendWith("extensions/extension.mpe")

    execute {
        TimelineResponseWriterFingerprint.method.addInstructions(0, filterCall("hideGifReplies"))
    }
}
