# 👋🧩 Morphe Patches template

Template repository for Morphe Patches.

## ❓ About

Patches for apps I like.

TODO: Update this about section with a brief introduction/summary about this repo and what it offers.

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.2.0](https://github.com/Kirby1997/morphe-patches/releases/tag/v1.2.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;43 patches total
<details open>
<summary>📦 Feeld&nbsp;&nbsp;•&nbsp;&nbsp;4 patches</summary>
<br>

**🎯 Supported versions:**

| 9.7.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Block tracking hosts (okhttp)](#block-tracking-hosts-okhttp) | Drops any okhttp request to known analytics/tracking hosts (Amplitude, AppsFlyer, Braze, Facebook app-events) with an IOException, before a connection is opened. Covers JS-fetch trackers that bypass the native-SDK kills. |  |
| [Disable AppsFlyer tracking](#disable-appsflyer-tracking) | Stops the AppsFlyer SDK from starting, so it never uploads launches, installs, or attribution events to *.appsflyer.com / appsflyersdk.com. |  |
| [Disable Braze tracking](#disable-braze-tracking) | Short-circuits the Braze network executor so no analytics/session data is ever POSTed to *.braze.eu; Braze treats it as a transient network failure. |  |
| [Unblur profile photos](#unblur-profile-photos) | Neutralises the expo-image blur Feeld applies to gated profile photos so the underlying image renders sharp. Only affects photos whose pixels reach the device; server-hidden photos and the hidden bio are unaffected. |  |

</details>

<details open>
<summary>📦 X&nbsp;&nbsp;•&nbsp;&nbsp;3 patches</summary>
<br>

**🎯 Supported versions:**

| 🧪&nbsp;12.4.1-release.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Bypass PairIP integrity](#bypass-pairip-integrity) | Severs PairIP's startup path (attachBaseContext short-circuits to super; verifyIntegrity and StartupLauncher.launch no-op) so a re-signed APK does not hit the libpairipcore SIGSEGV. Does not defeat server-side Play Integrity login attestation. |  |
| [Disable sensitive-media content-warning blur](#disable-sensitive-media-content-warning-blur) | Forces the timeline sensitive-media interstitial decision to false so downloaded media renders without the "content warning" blur overlay. RE artifact for X 12.4.1, which is native-PairIP-wrapped and not sideload-patchable unrooted — default-off. |  |
| [Remove promoted timeline items](#remove-promoted-timeline-items) | Drops promoted (ad / "Sponsored") posts, event summaries, and trends from every timeline before they are rendered. RE artifact for X 12.4.1, which is native-PairIP-wrapped and not sideload-patchable unrooted — default-off. |  |

</details>

<details open>
<summary>📦 FolderSync&nbsp;&nbsp;•&nbsp;&nbsp;2 patches</summary>
<br>

**🎯 Supported versions:**

| 4.8.5 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Disable AdMob banners](#disable-admob-banners) | Turns the AppAdmobBannerLoader composable into a no-op so the lite-version banner ad never renders. |  |
| [Disable AdMob interstitials](#disable-admob-interstitials) | Neutralises the synthetic Runnable that loads and shows AdMob interstitial ads on navigation events. |  |

</details>

<details open>
<summary>📦 Tinder&nbsp;&nbsp;•&nbsp;&nbsp;15 patches</summary>
<br>

**🎯 Supported versions:**

| 17.15.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Disable Boost upsell](#disable-boost-upsell) | Suppresses the standard Boost upsell popup ("Get Tinder Plus / Gold / Platinum" prompt that surfaces when out of Boosts). |  |
| [Disable Likes You Gold upsell modal](#disable-likes-you-gold-upsell-modal) | Stops the "pick one of the people who liked you" Gold Home teaser from appearing, which also removes the crash that happened when selecting a face launched the Gold paywall. |  |
| [Disable MyLikes upsell](#disable-mylikes-upsell) | Suppresses the "You've liked amazing people" Tinder Platinum popup on the Likes Sent tab. |  |
| [Disable Platinum Likes upsell](#disable-platinum-likes-upsell) | Suppresses the "Be Seen Faster / Upgrade Likes" Tinder Platinum popup. |  |
| [Disable Primetime Boost upsell](#disable-primetime-boost-upsell) | Suppresses the Primetime Boost upsell popup. |  |
| [Disable Secret Admirer upsell](#disable-secret-admirer-upsell) | Suppresses the Secret Admirer (Gold) upsell popup. |  |
| [Disable ads-bouncer rewarded-video paywall](#disable-ads-bouncer-rewarded-video-paywall) | Suppresses the "Watch an ad to keep swiping" rewarded-video bottom sheet shown when out of likes. |  |
| [Disable all Tinder paywalls & ads](#disable-all-tinder-paywalls-ads) | Single toggle that applies every safe Tinder ad/upsell patch at once (Boost, MyLikes, Platinum Likes, Primetime, Secret Admirer, headless purchase, Likes You Gold, rewarded video, ads-bouncer paywall, and both swipe-stack ad injectors). Default-off: enable this on its own instead of the individual patches, not alongside them. Excludes the purchase-breaking generic paywall patches. |  |
| [Disable consent prompt](#disable-consent-prompt) | Suppresses Tinder's in-house TCF consent popup ("Privacy preference centre") by taking CmpLifecycleObserver's existing no-prompt branch unconditionally. No consent is granted, so non-essential tracking stays unpermitted and the popup never appears. |  |
| [Disable curated swipe-stack ads](#disable-curated-swipe-stack-ads) | Stops Tinder inserting sponsored ad cards into curated card stacks (AdCuratedCardStackInjector.shouldInsertAdRec -> false). |  |
| [Disable dynamic paywall sheet](#disable-dynamic-paywall-sheet) | Suppresses the generic server-driven paywall sheet (PaywallDialogFragment) that LaunchPaywallFlow renders for most upgrade prompts. |  |
| [Disable headless purchase upsell](#disable-headless-purchase-upsell) | Suppresses the headless-purchase confirmation upsell popup. |  |
| [Disable main swipe-stack ads](#disable-main-swipe-stack-ads) | Stops Tinder inserting sponsored ad cards into the main swipe rec-stack (AdMainCardStackInjector.shouldInsertAdRec -> false). |  |
| [Disable paywall flow](#disable-paywall-flow) | Short-circuits the central LaunchPaywallFlow entry. Suppresses every paywall routed through paywallflow but also disables legitimate purchase flows. |  |
| [Disable rewarded-video modal](#disable-rewarded-video-modal) | Suppresses the standalone rewarded-video bottom sheet (e.g. "watch an ad to get a Rewind"). |  |

</details>

<details open>
<summary>📦 Meetup&nbsp;&nbsp;•&nbsp;&nbsp;11 patches</summary>
<br>

**🎯 Supported versions:**

| 2026.04.10.2881 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Disable Meetup+ trial panels](#disable-meetup-trial-panels) | Removes the 'Try Meetup+ free for 7 days' banner composable from every screen that embeds it. |  |
| [Disable MemberSub paywalls](#disable-membersub-paywalls) | Closes the Compose-era Meetup+ paywall activities (MemberSubActivity and MemberSubWebViewActivity) before they render, blocking the popups that profile views, message composition, and other upsells now route through. |  |
| [Disable Rokt partner offers](#disable-rokt-partner-offers) | Removes the 'Powered by Rokt' third-party offer popups (e.g. the post-signup Disney+ interstitial) by no-oping the mParticle RoktKit.execute funnel so no Rokt placement renders. |  |
| [Disable all Meetup paywalls](#disable-all-meetup-paywalls) | Single toggle that applies every Meetup+ paywall patch at once (intro, step-up, MemberSub, profile, trial panels, unprompted, and attendees paywall panels). Default-off: enable this on its own instead of the individual patches, not alongside them. |  |
| [Disable intro paywall](#disable-intro-paywall) | Suppresses the Meetup+ intro paywall that pops up on fresh login. |  |
| [Disable profile paywall](#disable-profile-paywall) | Stops the Meetup+ subscription popup from appearing when tapping a member's name or 'See full profile'. |  |
| [Disable step-up paywalls](#disable-step-up-paywalls) | Closes the Meetup+ step-up paywall Activity before it renders, blocking every popup that routes through it (RSVP, messaging, attendees, waitlist, group members, profile). |  |
| [Disable unprompted paywalls](#disable-unprompted-paywalls) | Forces AppSettings.getShouldShowUnpromptedPaywall / getShouldShowEventUnpromptedPaywall to false so Meetup+ paywalls do not pop up on their own. |  |
| [Hide attendees paywall panels](#hide-attendees-paywall-panels) | Hides the 'Learn more about attendees / Unlock full details' teaser on event pages and the 'Learn more about who will be there. Try for free.' banner on the Attendees list. |  |
| [Inject Google Maps API key](#inject-google-maps-api-key) | Replaces the manifest's com.google.android.maps.v2.API_KEY with a user-supplied key. REQUIRED for Maps to render on sideloaded builds — Meetup's production key is cert-fingerprint-locked and rejects requests from any re-signed APK. | • mapsKey |
| [Unblur profile content](#unblur-profile-content) | Disables the Compose blur overlay Meetup applies to gated profile fields, group lists, and member rows so the underlying data is visible. |  |

</details>

<details open>
<summary>📦 Hidrate Spark&nbsp;&nbsp;•&nbsp;&nbsp;2 patches</summary>
<br>

**🎯 Supported versions:**

| 4.6.9 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Disable license check](#disable-license-check) | Bypasses the Google Play license verification that blocks sideloaded installs. |  |
| [Disable payment check](#disable-payment-check) | Bypasses the premium subscription check so all features are unlocked. |  |

</details>

<details open>
<summary>📦 Met Office&nbsp;&nbsp;•&nbsp;&nbsp;2 patches</summary>
<br>

**🎯 Supported versions:**

| 3.40.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Hide ad containers](#hide-ad-containers) | Collapses every AdMob BannerAd slot to zero pixels. Pair with 'Remove ads' to also strip the empty Met Office-branded strip the homepage banner slot leaves behind. |  |
| [Remove ads](#remove-ads) | Stops banner, interstitial, rewarded, and app-open ads from loading by no-opping the react-native-google-mobile-ads native bridge entry points. |  |

</details>

<details open>
<summary>🌐 Universal&nbsp;&nbsp;•&nbsp;&nbsp;4 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Allow user certificates (MITM)](#allow-user-certificates-mitm) | Makes the app trust user-installed CA certificates so an intercepting proxy (Burp/mitmproxy) can read its HTTPS traffic on an unrooted device. Overwrites any existing network security config to trust system + user CAs and permit cleartext. For inspecting your own app/account traffic; does not defeat certificate pinning (see the pinning-bypass patch). | • makeDebuggable |
| [Auto-reject OneTrust consent banner](#auto-reject-onetrust-consent-banner) | Universally suppresses the OneTrust cookie/TCF consent banner in any app that bundles OneTrust. Persists a real 'Banner - Reject All' decision so non-essential tracking is rejected, and no-ops the setupUI render path so the banner never appears on any launch. |  |
| [Bypass certificate pinning](#bypass-certificate-pinning) | No-ops OkHttp's CertificatePinner so pinned hosts accept an intercepting proxy's certificate. Covers the common Java/OkHttp case (class Lokhttp3/CertificatePinner;). Does not defeat native/NDK pinning (Flutter, BoringSSL) or OkHttp shaded to a non-okhttp3 package. |  |
| [Disable PairIP license check](#disable-pairip-license-check) | Universally bypasses Google Play PairIP's license verification (the Play-ownership gate that blocks sideloaded installs). Applies to any PairIP-licensed app; does not defeat PairIP's native VM/integrity layer. |  |

</details>

<!-- PATCHES_END -->

#### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=Kirby1997/morphe-patches

Or manually add this repository url as a patch source in Morphe: https://github.com/Kirby1997/morphe-patches

### 🛠️ Building

To build UserXYZ Patches,
you can follow the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation).

## 📜 License

UserXYZ Patches are licensed under the [GNU General Public License v3.0](LICENSE)
