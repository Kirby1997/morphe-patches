package io.github.kirby1997.extension.twitter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Filters the profile Media tab response of X 12.7.1 before it is written to the timeline
 * database.
 * <p>
 * Called from the top of {@code com.twitter.api.legacy.request.urt.t.a(z3, v2)}, the converter
 * every legacy URT response passes through on its way to the database. The argument is the
 * parsed response ({@code com.twitter.model.timeline.urt.z3}); only responses for the profile
 * media timeline (timeline type 15) are touched.
 * <p>
 * Field names are the R8-obfuscated ones of 12.7.1 and are resolved reflectively so this class
 * needs no stubs. Any mismatch leaves the response untouched rather than crashing the timeline.
 *
 * <pre>
 * z3            a: q (global objects)      b: t1 (instructions)       c: z3$b (metadata)
 * z3$b          b: e2 (timeline key)
 * e2            a: int timeline type       c: long timeline owner id
 * q             a: Map&lt;String, core.b&gt; tweets by id
 * t1            b: List&lt;f2&gt; instructions
 * instructions  n.a: List&lt;a2&gt; (AddEntries)   o.c: List&lt;n2&gt; (AddToModule)   h.a: a2 (PinEntry)
 * a2 entries    v4.j: r4 (tweet item, r4.a = tweet id)   t2.e: List&lt;n2&gt; (module items)
 * core.b        b: l1 author (l1.a = id)   f: core.d
 * core.d        o: long in_reply_to_status_id   k: h1 text entities
 * h1.f: j1      j1.g: c0 media list    c0.a (from s): List&lt;b0&gt;    b0.p: b0$d media type enum
 * </pre>
 */
@SuppressWarnings("unused")
public final class ProfileMediaTimelineFilter {
    private static final int PROFILE_MEDIA_TIMELINE = 15;

    private static final String ADD_ENTRIES = "com.twitter.model.timeline.urt.instructions.n";
    private static final String ADD_TO_MODULE = "com.twitter.model.timeline.urt.instructions.o";
    private static final String PIN_ENTRY = "com.twitter.model.timeline.urt.instructions.h";
    private static final String TWEET_ENTRY = "com.twitter.model.timeline.urt.v4";
    private static final String MODULE_ENTRY = "com.twitter.model.timeline.urt.t2";

    private static final int KEEP_OWN_MEDIA = 1;
    private static final int HIDE_GIF_REPLIES = 2;

    private ProfileMediaTimelineFilter() {
    }

    /**
     * Keeps only the profile owner's own posts that carry media. Used when the Media tab is
     * fed from the posts-and-replies timeline instead of the server's media timeline.
     */
    public static void keepOwnMedia(Object response) {
        filter(response, KEEP_OWN_MEDIA);
    }

    /** Drops replies whose media is an animated GIF. */
    public static void hideGifReplies(Object response) {
        filter(response, HIDE_GIF_REPLIES);
    }

    private static void filter(Object response, int mode) {
        try {
            Object timelineKey = get(get(response, "c"), "b");
            if (((Number) get(timelineKey, "a")).intValue() != PROFILE_MEDIA_TIMELINE) return;

            Context context = new Context(
                    mode,
                    ((Number) get(timelineKey, "c")).longValue(),
                    (Map<?, ?>) get(get(response, "a"), "a")
            );

            Object instructionHolder = get(response, "b");
            List<?> instructions = (List<?>) get(instructionHolder, "b");
            List<Object> keptInstructions = new ArrayList<>(instructions.size());
            boolean instructionsChanged = false;

            for (Object instruction : instructions) {
                String type = instruction.getClass().getName();
                if (ADD_ENTRIES.equals(type)) {
                    List<?> entries = (List<?>) get(instruction, "a");
                    List<Object> kept = filterEntries(entries, context);
                    if (kept != null) set(instruction, "a", kept);
                } else if (ADD_TO_MODULE.equals(type)) {
                    List<?> items = (List<?>) get(instruction, "c");
                    List<Object> kept = filterItems(items, context);
                    if (kept != null) set(instruction, "c", kept);
                } else if (PIN_ENTRY.equals(type) && !context.keepEntry(get(instruction, "a"))) {
                    instructionsChanged = true;
                    continue;
                }
                keptInstructions.add(instruction);
            }

            if (instructionsChanged) {
                set(instructionHolder, "b", Collections.unmodifiableList(keptInstructions));
            }
        } catch (Throwable ignored) {
            // Unexpected shape: show the response as the server sent it.
        }
    }

    /** Returns the filtered entries, or null when nothing was removed. */
    private static List<Object> filterEntries(List<?> entries, Context context) throws Exception {
        List<Object> kept = new ArrayList<>(entries.size());
        for (Object entry : entries) {
            if (context.keepEntry(entry)) kept.add(entry);
        }
        return kept.size() == entries.size() ? null : kept;
    }

    /** Filters module items, keeping every non-post item (cursors, headers, footers). */
    private static List<Object> filterItems(List<?> items, Context context) throws Exception {
        List<Object> kept = new ArrayList<>(items.size());
        for (Object item : items) {
            if (!TWEET_ENTRY.equals(item.getClass().getName()) || context.keepPost(item)) kept.add(item);
        }
        return kept.size() == items.size() ? null : kept;
    }

    private static final class Context {
        final int mode;
        final long ownerId;
        final Map<?, ?> tweets;

        Context(int mode, long ownerId, Map<?, ?> tweets) {
            this.mode = mode;
            this.ownerId = ownerId;
            this.tweets = tweets;
        }

        boolean keepEntry(Object entry) throws Exception {
            String type = entry.getClass().getName();
            if (TWEET_ENTRY.equals(type)) return keepPost(entry);
            if (!MODULE_ENTRY.equals(type)) return true;

            List<?> items = (List<?>) get(entry, "e");
            int posts = 0;
            for (Object item : items) {
                if (TWEET_ENTRY.equals(item.getClass().getName())) posts++;
            }
            List<Object> kept = filterItems(items, this);
            if (kept == null) return true;

            int keptPosts = posts - (items.size() - kept.size());
            // A conversation or grid module with none of its posts left would render as an
            // empty shell, so drop the whole module.
            if (posts > 0 && keptPosts == 0) return false;
            set(entry, "e", kept);
            return true;
        }

        boolean keepPost(Object tweetEntry) throws Exception {
            Object tweetId = get(get(tweetEntry, "j"), "a");
            Object tweet = tweetId == null ? null : tweets.get(tweetId);
            if (tweet == null) return mode != KEEP_OWN_MEDIA;

            Object core = get(tweet, "f");
            List<?> media = media(core);

            if (mode == KEEP_OWN_MEDIA) {
                long authorId = ((Number) get(get(tweet, "b"), "a")).longValue();
                // Reposts carry the original author here, so they fall out with other people's posts.
                return authorId == ownerId && !media.isEmpty();
            }

            boolean isReply = ((Number) get(core, "o")).longValue() != 0;
            return !(isReply && hasGif(media));
        }
    }

    private static List<?> media(Object core) throws Exception {
        Object entities = get(get(core, "k"), "f");
        Object mediaList = entities == null ? null : get(entities, "g");
        Object list = mediaList == null ? null : get(mediaList, "a");
        return list instanceof List ? (List<?>) list : Collections.emptyList();
    }

    private static boolean hasGif(List<?> media) throws Exception {
        for (Object item : media) {
            Object type = get(item, "p");
            if (type instanceof Enum && "ANIMATED_GIF".equals(((Enum<?>) type).name())) return true;
        }
        return false;
    }

    private static Object get(Object target, String name) throws Exception {
        return field(target.getClass(), name).get(target);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        field(target.getClass(), name).set(target, value);
    }

    private static Field field(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(type.getName() + "." + name);
    }
}
