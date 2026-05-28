# capacitor-admob-nextgen

[![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://www.android.com)
[![iOS](https://img.shields.io/badge/Platform-iOS-lightgrey?logo=apple)](https://www.apple.com/ios)
[![AdMob Next Gen](https://img.shields.io/badge/SDK-Google%20Mobile%20Ads%20Next--Gen-blue)](https://ads-developers.googleblog.com/2026/01/announcing-google-mobile-ads-next-gen.html)
[![NPM version](https://img.shields.io/npm/v/capacitor-admob-nextgen.svg)](https://www.npmjs.com/package/capacitor-admob-nextgen)
[![Downloads](https://img.shields.io/npm/dm/capacitor-admob-nextgen.svg)](https://www.npmjs.com/package/capacitor-admob-nextgen)
[![License](https://img.shields.io/npm/l/capacitor-admob-nextgen.svg)](https://github.com/swaplab-engine/capacitor-admob-nextgen/blob/main/LICENSE)

**The Ultimate AdMob Monetization Solution for Capacitor.**

This plugin integrates the newly announced Google Mobile Ads (GMA) Next-Gen SDK into Capacitor. It is designed with a high-performance, modular architecture and zero mandatory native configuration.



---

<table>
  <tr>
    <td align="center"><strong>Consent (UMP)</strong></td>
    <td align="center"><strong>App Open</strong></td>
    <td align="center"><strong>Interstitial</strong></td>
    <td align="center"><strong>Rewarded</strong></td>
  </tr>
  <tr>
    <td align="center"><img width="190" src="https://github.com/user-attachments/assets/85544f57-2abd-4507-9bc9-6a6b032d4314" alt="Consent UMP Popup" /></td>
    <td align="center"><img width="190" src="https://github.com/user-attachments/assets/ccbb9244-2a80-4b87-9eab-41ce088c2a1f" alt="App Open Preload" /></td>
    <td align="center"><img width="190" src="https://github.com/user-attachments/assets/013fbdae-0db6-47af-bc62-faedb52bfeb7" alt="Interstitial Preload" /></td>
    <td align="center"><img width="190" src="https://github.com/user-attachments/assets/f2b98490-bb00-4949-a842-7f309d920c1c" alt="Rewarded Preload" /></td>
  </tr>
  <tr>
    <td align="center"><strong>Banner (Adaptive)</strong></td>
    <td align="center"><strong>Banner (Large Adaptive)</strong></td>
    <td align="center"><strong>Banner (Collapsible Adaptive)</strong></td>
    <td align="center"><strong>IOS UMP ATT</strong></td>
  </tr>
  <tr>
    <td align="center"><img width="190" src="https://github.com/user-attachments/assets/d3d8dd18-9c99-448a-b407-61c819a556bf" alt="Adaptive Banner" /></td>
    <td align="center"><img width="190" src="https://github.com/user-attachments/assets/54044007-0468-4a68-be35-0502491ceb52" alt="Large Anchored Banner" /></td>
    <td align="center"><img width="190" src="https://github.com/user-attachments/assets/1c947811-188f-48d2-85d2-0658914377b5" alt="Collapsible Banner" /></td>
    <td align="center"><img width="190" src="https://github.com/user-attachments/assets/044154ea-4f4d-4b5d-a447-74154c71a858" alt="IOS UMP ATT" /></td>
  </tr>
</table>

---


### 📦 Current SDK Versions (Maintained & Up-to-Date)
This plugin is regularly updated to support the latest standards.

| Component | Platform | Version | Release Notes |
| :--- | :--- | :--- | :--- |
| **GMA Next-Gen SDK** | Android | **1.1.0** | [View Notes](https://developers.google.com/admob/android/next-gen/rel-notes) |
| **UMP SDK** | Android | **4.0.0** | [View Notes](https://developers.google.com/admob/android/privacy/release-notes) |
| **Mobile Ads SDK** | iOS | **13.3.0** | [View Notes](https://developers.google.com/admob/ios/rel-notes) |
| **UMP SDK** | iOS | **3.1.0** | [View Notes](https://developers.google.com/ad-manager/mobile-ads-sdk/ios/privacy/download) |


---


## ✨ Key Features

### 🚀 Core & Performance
- **Next-Gen Preloading API:** Introduces continuous background ad buffering. Pull ads instantly with zero latency.
- **Classic Single-Load API:** Fully supports traditional ad loading methods for backwards compatibility.
- **Smart Caching & Rate Limiting:** Built-in anti-spam protections to safeguard AdMob accounts from Invalid Traffic penalties.
- **Automated Native Setup:** Effortlessly inject App IDs into `AndroidManifest.xml` and `Info.plist` using Capacitor CLI Hooks.
- **Integrated UMP:** Full support for Google's User Messaging Platform for GDPR and App Tracking Transparency (ATT) compliance.

### 🧩 Banner Ads
> **IMPORTANT:** The Smart WebView Push and Adaptive Layout handling is exclusively designed, thoroughly tested, and handled specifically for **Capacitor 8+ (API 36)**.
> 
> Using this plugin on Capacitor 7+ (API 35) or below may not yield expected results, as the layout engine is strictly tailored for the Capacitor 8 architecture.

- [Example video](https://github.com/swaplab-engine/capacitor-admob-nextgen/releases/tag/v1.0.1)
- **Smart WebView Push:** Dynamically adjusts WebView margins when Banner Ads are displayed to prevent UI overlap.
- **Isolated OS Branching:** Uses distinct native logic for Android 15+ (Edge-to-Edge) and Android 14 or below to guarantee layout stability.
- **Immersive Fullscreen Support:** Automatically detects and adapts perfectly whether the app is in normal or immersive fullscreen mode.

### 📱 Full-Screen Ads (Interstitial, Rewarded, App Open)
- **Safe Area Workaround:** Includes an automated background lifecycle fix to prevent full-screen ad close buttons ("X") from being hidden under system bars on Android 15+.

---

## ⚠️ Important Notices

### 1. Built from Scratch with Battle-Tested Logic
- This is **not** a fork of any existing Capacitor plugin. It is built entirely from scratch using the official [@capacitor/plugin](https://github.com/ionic-team/create-capacitor-plugin) generator.
- While the Capacitor wrapper is new, the core engine inherits years of proven optimizations from the author's highly successful Cordova implementations ([cordova-plugin-admob-nextgen](https://github.com/swaplab-engine/cordova-plugin-admob-nextgen) and [emi-indo-cordova-plugin-admob](https://github.com/EMI-INDO/emi-indo-cordova-plugin-admob)).
- **Focus on eCPM & Account Health:** This plugin is strictly engineered to maintain a healthy balance between ad requests and actual impressions. Generating thousands of unshown ad requests severely damages Click-Through Rates (CTR) and eCPM. This architecture ensures high-efficiency ad delivery to protect AdMob account health and maximize monetization metrics.

### 2. Next-Gen SDK ONLY 
This plugin specifically targets the newly announced **[GMA Next-Gen SDK](https://developers.google.com/admob/android/next-gen/quick-start)**, which is vastly different from the legacy SDK.
* Learn about the architectural differences here: [Announcing Google Mobile Ads Next-Gen](https://ads-developers.googleblog.com/2026/01/announcing-google-mobile-ads-next-gen.html).
* **CRITICAL:** Do **NOT** combine the legacy Google Mobile Ads SDK and the Next-Gen SDK in the same project. It will cause your application to crash during the build process.
* If you still need the legacy SDK, please use the standard [@capacitor-community/admob](https://github.com/capacitor-community/admob) plugin.

---

## 📦 Installation

```bash
npm install capacitor-admob-nextgen
npx cap sync
```

## ⚙️ Configuration (App ID Setup)

Google's UMP SDK strictly requires your AdMob App ID to be present in your native `AndroidManifest.xml` (Android) and `Info.plist` (iOS) before initialization. This plugin offers two ways to handle this:

### Option A: Automatic Setup (Highly Recommended)
Avoid manual XML editing. This plugin provides a Capacitor CLI Hook to automatically inject your App IDs every time you run `npx cap sync`.

1. Add your AdMob configuration to your app's root `package.json`:
```json
{
  "name": "your-app-name",
  "admob": {
    "androidAppId": "ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy",
    "iosAppId": "ca-app-pub-xxxxxxxxxxxxxxxx~zzzzzzzzzz",
    "userTrackingDescription": "This identifier will be used to deliver personalized ads to you." 
  }
}
```

2. Register the hook in the `scripts` section of your `package.json`:
```json
{
  "scripts": {
    "capacitor:sync:after": "node node_modules/capacitor-admob-nextgen/scripts/admob-manifest.js"
  }
}
```
*Transparency Note: This plugin does NOT secretly manipulate your project. The injection script is strictly OPT-IN and only runs if you explicitly declare the hook above.*

### Option B: Manual Setup
If you prefer total manual control, simply do not add the script to your `package.json`. 

**For Android:** Open `android/app/src/main/AndroidManifest.xml` and add the following inside the `<application>` tag:
```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy" />
```

**For iOS:** Open `ios/App/App/Info.plist` and add: ( IOS Next release)
```xml
<key>GADApplicationIdentifier</key>
<string>ca-app-pub-xxxxxxxxxxxxxxxx~zzzzzzzzzz</string>
<key>NSUserTrackingUsageDescription</key>
<string>This identifier will be used to deliver personalized ads to you.</string>
```
---

## Note: IOS
Capacitor 8 Recommendation: Swift Package Manager (SPM).
When a dependency is missing, open the project in Xcode 26+
- click File at the top, and select Package > Reset Package Caches.
This will download all dependencies in Package.swift.

- Because CocoaPods remains available as an alternative.
`cd ios`
```
pod install --repo-update
```


---

## 🚀 Example Usage

Check out the comprehensive, interactive dashboard example to see how to implement UMP, classic loading, and the new Preloading API:
👉 **[capacitor-welcome.js Example Project](https://github.com/swaplab-engine/capacitor-admob-nextgen/blob/main/example-app/src/js/capacitor-welcome.js)**

---

## Install

To use npm:

```bash
npm install capacitor-admob-nextgen
```

To use yarn:

```bash
yarn add capacitor-admob-nextgen
```

Sync native files:

```bash
npx cap sync
```

## API

<docgen-index>

* [`requestTrackingAuthorization()`](#requesttrackingauthorization)
* [`requestConsentInfo(...)`](#requestconsentinfo)
* [`showPrivacyOptionsForm()`](#showprivacyoptionsform)
* [`getTCData()`](#gettcdata)
* [`initialize(...)`](#initialize)
* [`loadAppOpen(...)`](#loadappopen)
* [`showAppOpen()`](#showappopen)
* [`createBanner(...)`](#createbanner)
* [`showBanner()`](#showbanner)
* [`hideBanner()`](#hidebanner)
* [`destroyBanner()`](#destroybanner)
* [`loadInterstitial(...)`](#loadinterstitial)
* [`showInterstitial()`](#showinterstitial)
* [`loadRewarded(...)`](#loadrewarded)
* [`showRewarded()`](#showrewarded)
* [`loadRewardedInterstitial(...)`](#loadrewardedinterstitial)
* [`showRewardedInterstitial()`](#showrewardedinterstitial)
* [`startPreloadAppOpen(...)`](#startpreloadappopen)
* [`pollAndShowAppOpen()`](#pollandshowappopen)
* [`isAppOpenPreloadAvailable(...)`](#isappopenpreloadavailable)
* [`startPreloadBanner(...)`](#startpreloadbanner)
* [`pollAndShowBanner()`](#pollandshowbanner)
* [`hidePreloadedBanner()`](#hidepreloadedbanner)
* [`destroyPreloadedBanner()`](#destroypreloadedbanner)
* [`isBannerPreloadAvailable(...)`](#isbannerpreloadavailable)
* [`startPreloadInterstitial(...)`](#startpreloadinterstitial)
* [`pollAndShowInterstitial()`](#pollandshowinterstitial)
* [`isInterstitialPreloadAvailable(...)`](#isinterstitialpreloadavailable)
* [`startPreloadRewarded(...)`](#startpreloadrewarded)
* [`pollAndShowRewarded()`](#pollandshowrewarded)
* [`isRewardedPreloadAvailable(...)`](#isrewardedpreloadavailable)
* [`startPreloadRewardedInterstitial(...)`](#startpreloadrewardedinterstitial)
* [`pollAndShowRewardedInterstitial()`](#pollandshowrewardedinterstitial)
* [`isRewardedInterstitialPreloadAvailable(...)`](#isrewardedinterstitialpreloadavailable)
* [`addListener(string, ...)`](#addlistenerstring-)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

# AdMob Next-Gen Plugin
* ### CRITICAL: Proper Execution Order
To comply with Google's policies (GDPR, COPPA, etc.), you MUST follow this exact execution order when your app starts:
1. **Request Consent (UMP)**: `await AdMobNextGen.requestConsentInfo(...)`
*Always do this first. It checks if the user needs to see a privacy form.*
2. **Initialize SDK**: `await AdMobNextGen.initialize(...)`
*Do this REGARDLESS of whether the consent form was shown or not. The SDK needs to boot up using the consent status gathered in step 1.*
3. **Load Ads**: e.g., `await AdMobNextGen.loadInterstitial(...)`
4. **Show Ads**: e.g., `await AdMobNextGen.showInterstitial()`
* ---

### requestTrackingAuthorization()

```typescript
requestTrackingAuthorization() => Promise<TrackingAuthorizationResult>
```

Requests App Tracking Transparency (ATT) authorization from the user.
This is explicitly required for iOS 14.5+ to display personalized ads.
On Android and Web, it automatically returns { status: 'authorized' }.
* @returns A promise resolving to the authorization status.

**Returns:** <code>Promise&lt;<a href="#trackingauthorizationresult">TrackingAuthorizationResult</a>&gt;</code>

--------------------


### requestConsentInfo(...)

```typescript
requestConsentInfo(options?: ConsentOptions | undefined) => Promise<ConsentStatusResult>
```

Requests consent information update and shows the consent form if required (UMP SDK).
MUST be called before `initialize()`.

| Param         | Type                                                      |
| ------------- | --------------------------------------------------------- |
| **`options`** | <code><a href="#consentoptions">ConsentOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#consentstatusresult">ConsentStatusResult</a>&gt;</code>

--------------------


### showPrivacyOptionsForm()

```typescript
showPrivacyOptionsForm() => Promise<ConsentStatusResult>
```

Shows the privacy options form if the user wants to change their consent later.
Usually triggered by a button in your app's Settings menu.

**Returns:** <code>Promise&lt;<a href="#consentstatusresult">ConsentStatusResult</a>&gt;</code>

--------------------


### getTCData()

```typescript
getTCData() => Promise<TCDataResult>
```

Reads the IAB TCF v2.2 strings directly from SharedPreferences.
Useful if you need to pass consent strings to a custom analytics server.

**Returns:** <code>Promise&lt;<a href="#tcdataresult">TCDataResult</a>&gt;</code>

--------------------


### initialize(...)

```typescript
initialize(options: InitializeOptions) => Promise<InitializeResult>
```

Initializes the GMA Next-Gen SDK.
MUST be called after `requestConsentInfo()`.

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#initializeoptions">InitializeOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#initializeresult">InitializeResult</a>&gt;</code>

--------------------


### loadAppOpen(...)

```typescript
loadAppOpen(options: AppOpenOptions) => Promise<void>
```

Loads an App Open Ad.

| Param         | Type                                                      |
| ------------- | --------------------------------------------------------- |
| **`options`** | <code><a href="#appopenoptions">AppOpenOptions</a></code> |

--------------------


### showAppOpen()

```typescript
showAppOpen() => Promise<void>
```

Shows the loaded App Open Ad.

--------------------


### createBanner(...)

```typescript
createBanner(options: BannerOptions) => Promise<void>
```

Creates and loads a Banner Ad. It can automatically show upon loading if isAutoShow is true.

| Param         | Type                                                    |
| ------------- | ------------------------------------------------------- |
| **`options`** | <code><a href="#banneroptions">BannerOptions</a></code> |

--------------------


### showBanner()

```typescript
showBanner() => Promise<void>
```

Shows a previously created (and potentially hidden) banner ad.

--------------------


### hideBanner()

```typescript
hideBanner() => Promise<void>
```

Temporarily hides the banner ad without destroying it.

--------------------


### destroyBanner()

```typescript
destroyBanner() => Promise<void>
```

Destroys the banner ad and cleans up resources from the view hierarchy.

--------------------


### loadInterstitial(...)

```typescript
loadInterstitial(options: InterstitialOptions) => Promise<void>
```

Loads an Interstitial Ad.

| Param         | Type                                                                |
| ------------- | ------------------------------------------------------------------- |
| **`options`** | <code><a href="#interstitialoptions">InterstitialOptions</a></code> |

--------------------


### showInterstitial()

```typescript
showInterstitial() => Promise<void>
```

Shows the loaded Interstitial Ad.

--------------------


### loadRewarded(...)

```typescript
loadRewarded(options: RewardedOptions) => Promise<void>
```

Loads a Rewarded Ad.

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#rewardedoptions">RewardedOptions</a></code> |

--------------------


### showRewarded()

```typescript
showRewarded() => Promise<void>
```

Shows the loaded Rewarded Ad.

--------------------


### loadRewardedInterstitial(...)

```typescript
loadRewardedInterstitial(options: RewardedInterstitialOptions) => Promise<void>
```

Loads a Rewarded Interstitial Ad.

| Param         | Type                                                                                |
| ------------- | ----------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#rewardedinterstitialoptions">RewardedInterstitialOptions</a></code> |

--------------------


### showRewardedInterstitial()

```typescript
showRewardedInterstitial() => Promise<void>
```

Shows the loaded Rewarded Interstitial Ad.

--------------------


### startPreloadAppOpen(...)

```typescript
startPreloadAppOpen(options: AppOpenOptions) => Promise<void>
```

--------------------
⚠️ CRITICAL NOTE: 
Do NOT mix the Classic API with the Next-Gen API. 
If you use `startPreload...`, you must ONLY use `pollAndShow...` to display the ad.
Calling classic methods like `load()` or `show()` while a preloader is active will cause unexpected behavior.
* Starts the background preloader buffer for App Open Ads.
This tells the SDK to automatically fetch and cache ads continuously.
* ⚠️ Do not use `loadAppOpen()` if you are using this preloader method.
--------------------
- **🍎 iOS LIMITATION:** The Next-Gen Preloading API is currently an Android-exclusive feature within the GMA SDK. When building or running on iOS devices, you MUST use the Classic single-load API (e.g., `loadAppOpen()` and `showAppOpen()`). Calling Preload methods on iOS will not trigger the native preloader.
--------------------

| Param         | Type                                                      |
| ------------- | --------------------------------------------------------- |
| **`options`** | <code><a href="#appopenoptions">AppOpenOptions</a></code> |

--------------------


### pollAndShowAppOpen()

```typescript
pollAndShowAppOpen() => Promise<void>
```

Pulls the next available ad from the preload buffer and shows it instantly.
If the buffer is empty (exhausted), this will throw an error.

--------------------


### isAppOpenPreloadAvailable(...)

```typescript
isAppOpenPreloadAvailable(options?: { adUnitId?: string | undefined; } | undefined) => Promise<{ isAvailable: boolean; }>
```

Synchronously checks if an ad is currently ready in the preload buffer.

| Param         | Type                                |
| ------------- | ----------------------------------- |
| **`options`** | <code>{ adUnitId?: string; }</code> |

**Returns:** <code>Promise&lt;{ isAvailable: boolean; }&gt;</code>

--------------------


### startPreloadBanner(...)

```typescript
startPreloadBanner(options: BannerOptions & { bufferSize?: number; }) => Promise<void>
```

Starts the background preloader buffer for Banner Ads.

| Param         | Type                                                                               |
| ------------- | ---------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#banneroptions">BannerOptions</a> & { bufferSize?: number; }</code> |

--------------------


### pollAndShowBanner()

```typescript
pollAndShowBanner() => Promise<void>
```

Pulls the next available Banner ad from the preload buffer and renders it immediately.

--------------------


### hidePreloadedBanner()

```typescript
hidePreloadedBanner() => Promise<void>
```

Hides the preloaded banner without destroying it.

--------------------


### destroyPreloadedBanner()

```typescript
destroyPreloadedBanner() => Promise<void>
```

Destroys the current preloaded banner view.

--------------------


### isBannerPreloadAvailable(...)

```typescript
isBannerPreloadAvailable(options?: { adUnitId?: string | undefined; } | undefined) => Promise<{ isAvailable: boolean; }>
```

Synchronously checks if a Banner ad is ready in the preload buffer.

| Param         | Type                                |
| ------------- | ----------------------------------- |
| **`options`** | <code>{ adUnitId?: string; }</code> |

**Returns:** <code>Promise&lt;{ isAvailable: boolean; }&gt;</code>

--------------------


### startPreloadInterstitial(...)

```typescript
startPreloadInterstitial(options: InterstitialOptions & { bufferSize?: number; }) => Promise<void>
```

Starts the background preloader buffer for Interstitial Ads.

| Param         | Type                                                                                           |
| ------------- | ---------------------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#interstitialoptions">InterstitialOptions</a> & { bufferSize?: number; }</code> |

--------------------


### pollAndShowInterstitial()

```typescript
pollAndShowInterstitial() => Promise<void>
```

Pulls the next available Interstitial ad from the preload buffer and shows it instantly.

--------------------


### isInterstitialPreloadAvailable(...)

```typescript
isInterstitialPreloadAvailable(options?: { adUnitId?: string | undefined; } | undefined) => Promise<{ isAvailable: boolean; }>
```

Synchronously checks if an Interstitial ad is ready in the preload buffer.

| Param         | Type                                |
| ------------- | ----------------------------------- |
| **`options`** | <code>{ adUnitId?: string; }</code> |

**Returns:** <code>Promise&lt;{ isAvailable: boolean; }&gt;</code>

--------------------


### startPreloadRewarded(...)

```typescript
startPreloadRewarded(options: RewardedOptions & { bufferSize?: number; }) => Promise<void>
```

Starts the background preloader buffer for Rewarded Ads.

| Param         | Type                                                                                   |
| ------------- | -------------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#rewardedoptions">RewardedOptions</a> & { bufferSize?: number; }</code> |

--------------------


### pollAndShowRewarded()

```typescript
pollAndShowRewarded() => Promise<void>
```

Pulls the next available Rewarded ad from the preload buffer and shows it instantly.

--------------------


### isRewardedPreloadAvailable(...)

```typescript
isRewardedPreloadAvailable(options?: { adUnitId?: string | undefined; } | undefined) => Promise<{ isAvailable: boolean; }>
```

Synchronously checks if a Rewarded ad is ready in the preload buffer.

| Param         | Type                                |
| ------------- | ----------------------------------- |
| **`options`** | <code>{ adUnitId?: string; }</code> |

**Returns:** <code>Promise&lt;{ isAvailable: boolean; }&gt;</code>

--------------------


### startPreloadRewardedInterstitial(...)

```typescript
startPreloadRewardedInterstitial(options: RewardedInterstitialOptions & { bufferSize?: number; }) => Promise<void>
```

Starts the background preloader buffer for Rewarded Interstitial Ads.

| Param         | Type                                                                                                           |
| ------------- | -------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#rewardedinterstitialoptions">RewardedInterstitialOptions</a> & { bufferSize?: number; }</code> |

--------------------


### pollAndShowRewardedInterstitial()

```typescript
pollAndShowRewardedInterstitial() => Promise<void>
```

Pulls the next available Rewarded Interstitial ad from the preload buffer and shows it instantly.

--------------------


### isRewardedInterstitialPreloadAvailable(...)

```typescript
isRewardedInterstitialPreloadAvailable(options?: { adUnitId?: string | undefined; } | undefined) => Promise<{ isAvailable: boolean; }>
```

Synchronously checks if a Rewarded Interstitial ad is ready in the preload buffer.

| Param         | Type                                |
| ------------- | ----------------------------------- |
| **`options`** | <code>{ adUnitId?: string; }</code> |

**Returns:** <code>Promise&lt;{ isAvailable: boolean; }&gt;</code>

--------------------


### addListener(string, ...)

```typescript
addListener(eventName: string, listenerFunc: (info: any) => void) => Promise<PluginListenerHandle>
```

Listens for Ad events triggered by the native SDK.
* 💡 **NOTE ON PRELOADING:** If you are using the Next-Gen Preloading API, standard ad events 
(like Loaded, FailedToLoad, Impression, Paid, etc.) will include an additional property: 
`{ source: "preloader" }`. This allows you to easily distinguish them from Classic API events using a single listener.
* ### Interstitial Events
- `onInterstitialAdLoaded`: Fired when an ad is loaded. Returns { adUnitId: string, source?: string }.
- `onInterstitialAdFailedToLoad`: Fired when an ad fails to load. Returns { error: string, source?: string }.
- `onInterstitialAdShowed`: Fired when an ad is displayed.
- `onInterstitialAdDismissed`: Fired when an ad is closed by the user.
- `onInterstitialAdFailedToShow`: Fired when an ad fails to display. Returns { error: string, source?: string }.
- `onInterstitialAdImpression`: Fired when an ad impression is recorded.
- `onInterstitialAdClicked`: Fired when an ad is clicked.
- `onInterstitialAdPaid`: Fired when revenue is recorded. Returns AdPaidEvent.
* ### Rewarded Events
- `onRewardedAdLoaded`: Fired when an ad is loaded. Returns { adUnitId: string, source?: string }.
- `onRewardedAdFailedToLoad`: Fired when an ad fails to load. Returns { error: string, source?: string }.
- `onRewardedAdShowed`: Fired when an ad is displayed.
- `onRewardedAdDismissed`: Fired when an ad is closed.
- `onRewardedAdFailedToShow`: Fired when an ad fails to display. Returns { error: string, source?: string }.
- `onRewardedAdImpression`: Fired when an ad impression is recorded.
- `onRewardedAdClicked`: Fired when an ad is clicked.
- `onRewardedAdReward`: Fired when the user earns a reward. Returns { amount: number, type: string, source?: string }.
- `onRewardedAdPaid`: Fired when revenue is recorded. Returns AdPaidEvent.
* ### Rewarded Interstitial Events
- `onRewardedInterstitialAdLoaded`: Fired when an ad is loaded. Returns { adUnitId: string, source?: string }.
- `onRewardedInterstitialAdFailedToLoad`: Fired when an ad fails to load. Returns { error: string, source?: string }.
- `onRewardedInterstitialAdShowed`: Fired when an ad is displayed.
- `onRewardedInterstitialAdDismissed`: Fired when an ad is closed.
- `onRewardedInterstitialAdFailedToShow`: Fired when an ad fails to display. Returns { error: string, source?: string }.
- `onRewardedInterstitialAdImpression`: Fired when an ad impression is recorded.
- `onRewardedInterstitialAdClicked`: Fired when an ad is clicked.
- `onRewardedInterstitialAdReward`: Fired when the user earns a reward. Returns { amount: number, type: string, source?: string }.
- `onRewardedInterstitialAdPaid`: Fired when revenue is recorded. Returns AdPaidEvent.
* ### App Open Events
- `onAppOpenAdLoaded`: Fired when an ad is loaded. Returns { adUnitId: string, source?: string }.
- `onAppOpenAdFailedToLoad`: Fired when an ad fails to load. Returns { error: string, source?: string }.
- `onAppOpenAdShowed`: Fired when an ad is displayed.
- `onAppOpenAdDismissed`: Fired when an ad is closed.
- `onAppOpenAdFailedToShow`: Fired when an ad fails to display. Returns { error: string, source?: string }.
- `onAppOpenAdImpression`: Fired when an ad impression is recorded.
- `onAppOpenAdClicked`: Fired when an ad is clicked.
- `onAppOpenAdPaid`: Fired when revenue is recorded. Returns AdPaidEvent.
* ### Banner Events
- `onBannerAdLoaded`: Fired when a banner is loaded. Returns { adUnitId: string, width: number, height: number, widthPixels: number, heightPixels: number, isCollapsible: boolean, source?: string }.
- `onBannerAdFailedToLoad`: Fired when a banner fails to load. Returns { error: string, source?: string }.
- `onBannerAdImpression`: Fired when a banner impression is recorded.
- `onBannerAdClicked`: Fired when a banner is clicked.
- `onBannerAdShowedFullScreen`: Fired when a banner opens an overlay (e.g., user clicked it).
- `onBannerAdDismissedFullScreen`: Fired when the banner overlay is closed.
- `onBannerAdFailedToShowFullScreen`: Fired when the banner overlay fails to open. Returns { error: string, source?: string }.
- `onBannerAdRefreshed`: Fired when the banner auto-refreshes.
- `onBannerAdFailedToRefresh`: Fired when the banner auto-refresh fails. Returns { error: string, source?: string }.
- `onBannerAdPaid`: Fired when revenue is recorded. Returns AdPaidEvent.
* ### Consent (UMP) Events
- `onConsentInfoUpdated`: Fired when consent info is successfully updated.
- `onConsentFormDismissed`: Fired when the consent form overlay is closed.
- `onConsentStatusChange`: Fired when consent status changes. Returns <a href="#consentstatusresult">ConsentStatusResult</a>.
- `onConsentError`: Fired when a UMP error occurs. Returns { code: number, message: string }.
* ### Next-Gen Buffer Events (Exhausted)
These events are uniquely fired by the Preloading API when the background buffer runs out of cached ads.
- `onAppOpenAdPreloadExhausted`: Fired when the App Open buffer is empty. Returns { adUnitId: string, source: "preloader" }.
- `onInterstitialAdPreloadExhausted`: Fired when the Interstitial buffer is empty. Returns { adUnitId: string, source: "preloader" }.
- `onRewardedAdPreloadExhausted`: Fired when the Rewarded buffer is empty. Returns { adUnitId: string, source: "preloader" }.
- `onRewardedInterstitialAdPreloadExhausted`: Fired when the Rewarded Interstitial buffer is empty. Returns { adUnitId: string, source: "preloader" }.
- `onBannerAdPreloadExhausted`: Fired when the Banner buffer is empty. Returns { adUnitId: string, source: "preloader" }.
* @param eventName The name of the event to listen for.

| Param              | Type                                | Description                                    |
| ------------------ | ----------------------------------- | ---------------------------------------------- |
| **`eventName`**    | <code>string</code>                 |                                                |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code> | The callback function handling the event data. |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### Interfaces


#### TrackingAuthorizationResult

| Prop         | Type                                                                                  | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| ------------ | ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **`status`** | <code>'authorized' \| 'denied' \| 'notDetermined' \| 'restricted' \| 'unknown'</code> | The current status of the App Tracking Transparency (ATT) authorization. - `authorized`: The user authorized access to app-related data for tracking. - `denied`: The user denied authorization to access app-related data for tracking. - `notDetermined`: The user has not yet received an authorization request. - `restricted`: The authorization to access app-related data is restricted. - `unknown`: Platform does not support ATT (e.g., Android, Web, or older iOS). |


#### ConsentStatusResult

| Prop                                  | Type                 | Description                                                                                                                                                                                                                                                                                                    |
| ------------------------------------- | -------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`canRequestAds`**                   | <code>boolean</code> | Indicates whether you have obtained sufficient consent to safely request ads. Do not load ads if this is false.                                                                                                                                                                                                |
| **`privacyOptionsRequirementStatus`** | <code>string</code>  | The exact string representation of the privacy options requirement. Possible values: "REQUIRED", "NOT_REQUIRED", "UNKNOWN".                                                                                                                                                                                    |
| **`isPrivacyOptionsRequired`**        | <code>boolean</code> | A simplified boolean check for `privacyOptionsRequirementStatus === 'REQUIRED'`. If true, you MUST provide a button in your app's settings allowing the user to modify their privacy choices, which triggers `showPrivacyOptionsForm()`.                                                                       |
| **`consentStatus`**                   | <code>number</code>  | The current consent status integer indicator: - 0 = UNKNOWN: Consent status is unknown. - 1 = REQUIRED: User consent is required but has not been obtained yet. - 2 = NOT_REQUIRED: User consent is not required (e.g., user is outside the EEA). - 3 = OBTAINED: User consent has been successfully obtained. |


#### ConsentOptions

| Prop                          | Type                 | Description                                                                                                                                                            |
| ----------------------------- | -------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`debug`**                   | <code>boolean</code> | Enables debug mode to simulate user location in the EEA (European Economic Area) for testing UMP. VERY IMPORTANT: Set to false in production. Default: false           |
| **`reset`**                   | <code>boolean</code> | Clears all previously saved consent data. Useful for simulating a first-time app launch. Default: false                                                                |
| **`tagForUnderAgeOfConsent`** | <code>boolean</code> | Sets whether the user is under the age of consent for GDPR purposes. Default: false                                                                                    |
| **`testDeviceId`**            | <code>string</code>  | A specific hashed device ID for debugging. If `debug` is true and this is left empty, the plugin will automatically attempt to detect and register the current device. |


#### TCDataResult

| Prop                        | Type                 | Description                                                                                                                                                                                                          |
| --------------------------- | -------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`tcString`**              | <code>string</code>  | The raw Transparency and Consent String (TC String) encoded in Base64.                                                                                                                                               |
| **`purposeConsents`**       | <code>string</code>  | A binary string representing user consent for specific IAB purposes (e.g., "10011..."). Index 0 represents Purpose 1, index 1 represents Purpose 2, etc.                                                             |
| **`vendorConsents`**        | <code>string</code>  | A binary string representing user consent for specific vendors.                                                                                                                                                      |
| **`gdprApplies`**           | <code>number</code>  | Indicates whether GDPR applies to this user. - 1 = GDPR applies. - 0 = GDPR does not apply.                                                                                                                          |
| **`isPersonalizedAllowed`** | <code>boolean</code> | A smart helper boolean parsed natively by the plugin. Returns true if IAB Purpose 1 is granted OR if GDPR does not apply. Useful for quickly deciding whether to load personalized or non-personalized ads manually. |
| **`statusMessage`**         | <code>string</code>  | A human-readable message explaining the parsing result of the TCData.                                                                                                                                                |


#### InitializeResult

| Prop         | Type                |
| ------------ | ------------------- |
| **`status`** | <code>string</code> |


#### InitializeOptions

| Prop                               | Type                                          | Description                                                                                                                                                                        |
| ---------------------------------- | --------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`isTesting`**                    | <code>boolean</code>                          | Automatically detect and register the current device as a test device. VERY IMPORTANT: Set this to false or remove it before publishing your app to the Play Store. Default: false |
| **`maxAdContentRating`**           | <code>'' \| 'G' \| 'PG' \| 'T' \| 'MA'</code> | Filter ads by content rating. 'G': General audiences 'PG': Parental guidance 'T': Teens 'MA': Mature audiences Default: "" (No filter)                                             |
| **`tagForChildDirectedTreatment`** | <code>boolean</code>                          | For purposes of the Children's Online Privacy Protection Act (COPPA). true: Child-directed false: Not child-directed undefined: Unspecified                                        |
| **`tagForUnderAgeOfConsent`**      | <code>boolean</code>                          | For users under the age of consent (GDPR). true: Under age of consent false: Not under age of consent undefined: Unspecified                                                       |


#### AppOpenOptions

| Prop                | Type                | Description                                                                                                                             |
| ------------------- | ------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| **`adUnitId`**      | <code>string</code> |                                                                                                                                         |
| **`retryInterval`** | <code>number</code> | Minimum time (in milliseconds) before the next ad request is allowed. Prevents spam requests and invalid traffic bans. Default is 5000. |


#### BannerOptions

| Prop                | Type                                                                                                                                                                                                                                                                                           | Description                                                                                                                             |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| **`adUnitId`**      | <code>string</code>                                                                                                                                                                                                                                                                            |                                                                                                                                         |
| **`isAutoShow`**    | <code>boolean</code>                                                                                                                                                                                                                                                                           | Automatically show the banner after it successfully loads. Default is true.                                                             |
| **`position`**      | <code>'TOP' \| 'BOTTOM'</code>                                                                                                                                                                                                                                                                 | "TOP" or "BOTTOM". Default is "BOTTOM".                                                                                                 |
| **`adSize`**        | <code>'BANNER' \| 'LARGE_BANNER' \| 'MEDIUM_RECTANGLE' \| 'FULL_BANNER' \| 'LEADERBOARD' \| 'LARGE_LANDSCAPE_ANCHORED_ADAPTIVE' \| 'LARGE_PORTRAIT_ANCHORED_ADAPTIVE' \| 'CURRENT_ORIENTATION_INLINE_ADAPTIVE' \| 'LARGE_ANCHORED_ADAPTIVE' \| 'PORTRAIT_INLINE_ADAPTIVE' \| 'ADAPTIVE'</code> | Specifies the banner size or adaptive behavior. Default is "ADAPTIVE" (Current Orientation Anchored Adaptive).                          |
| **`isOverlap`**     | <code>boolean</code>                                                                                                                                                                                                                                                                           | If true, the banner overlaps the webview. If false, it pushes the webview up/down. Default is true.                                     |
| **`isCollapsible`** | <code>boolean</code>                                                                                                                                                                                                                                                                           | Enables Google's collapsible banner feature. Default is false.                                                                          |
| **`retryInterval`** | <code>number</code>                                                                                                                                                                                                                                                                            | Minimum time (in milliseconds) before the next ad request is allowed. Prevents spam requests and invalid traffic bans. Default is 5000. |


#### InterstitialOptions

| Prop                | Type                | Description                                                                                                                             |
| ------------------- | ------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| **`adUnitId`**      | <code>string</code> |                                                                                                                                         |
| **`retryInterval`** | <code>number</code> | Minimum time (in milliseconds) before the next ad request is allowed. Prevents spam requests and invalid traffic bans. Default is 5000. |


#### RewardedOptions

| Prop                | Type                | Description                                                                                                                             |
| ------------------- | ------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| **`adUnitId`**      | <code>string</code> |                                                                                                                                         |
| **`retryInterval`** | <code>number</code> | Minimum time (in milliseconds) before the next ad request is allowed. Prevents spam requests and invalid traffic bans. Default is 5000. |


#### RewardedInterstitialOptions

| Prop                | Type                | Description                                                                                                                             |
| ------------------- | ------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| **`adUnitId`**      | <code>string</code> |                                                                                                                                         |
| **`retryInterval`** | <code>number</code> | Minimum time (in milliseconds) before the next ad request is allowed. Prevents spam requests and invalid traffic bans. Default is 5000. |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

</docgen-api>