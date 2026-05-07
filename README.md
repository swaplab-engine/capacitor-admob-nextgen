# capacitor-admob-nextgen

[![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://www.android.com)
[![iOS](https://img.shields.io/badge/Platform-iOS-lightgrey?logo=apple)](https://www.apple.com/ios)
[![AdMob Next Gen](https://img.shields.io/badge/SDK-Google%20Mobile%20Ads%20Next--Gen-blue)](https://ads-developers.googleblog.com/2026/01/announcing-google-mobile-ads-next-gen.html)
[![NPM version](https://img.shields.io/npm/v/capacitor-admob-nextgen.svg)](https://www.npmjs.com/package/capacitor-admob-nextgen)
[![Downloads](https://img.shields.io/npm/dm/capacitor-admob-nextgen.svg)](https://www.npmjs.com/package/capacitor-admob-nextgen)
[![License](https://img.shields.io/npm/l/capacitor-admob-nextgen.svg)](https://github.com/swaplab-engine/capacitor-admob-nextgen/blob/main/LICENSE)

**The Ultimate AdMob Monetization Solution for Capacitor.**

- Google Mobile Ads Next Gen SDK for Capacitor.
- High performance, zero native configuration, and modular architecture.

https://github.com/user-attachments/assets/49171a27-541b-4f0c-b3bd-961da9cad25e

---

## ⚠️ Important Notices

### 1. Built from Scratch
- This is **not** a fork of any existing Capacitor plugin.
- It is built entirely from scratch using the official [@capacitor/plugin](https://github.com/ionic-team/create-capacitor-plugin) generator.

- (I am also the author of the Cordova plugins [cordova-plugin-admob-nextgen](https://github.com/swaplab-engine/cordova-plugin-admob-nextgen) and [emi-indo-cordova-plugin-admob](https://github.com/EMI-INDO/emi-indo-cordova-plugin-admob)).

### 2. Next-Gen SDK ONLY
This plugin uses the newly announced **[GMA Next-Gen SDK](https://developers.google.com/admob/android/next-gen/quick-start)**, which is vastly different from the legacy Google Mobile Ads SDK. 
* Learn about the differences here: [Announcing Google Mobile Ads Next-Gen](https://ads-developers.googleblog.com/2026/01/announcing-google-mobile-ads-next-gen.html).
* **CRITICAL:** Do **NOT** combine the legacy Google Mobile Ads SDK and the Next-Gen SDK in the same project.
* It will cause your application to crash during the build process.
* If you still need the legacy SDK, please use [@capacitor-community/admob](https://github.com/capacitor-community/admob).

### 3. Not for Cordova
This is explicitly designed for **Capacitor**.
- If you are using Cordova, please use [cordova-plugin-admob-nextgen](https://github.com/swaplab-engine/cordova-plugin-admob-nextgen) which fully supports the Next-Gen architecture.

---

## 🛠 Status: BETA

This plugin is currently in **BETA** and features are being rolled out gradually.

It takes significant time to build and test across platforms.
* **Currently Supported:** Interstitial Ads (Android).
* **Maintainer:** I am a solo developer working on this in my free time. 

💖 **Support Development:** If you want to see faster updates and Android/iOS support, please consider **Sponsoring this project**!

---

## Example Usage

Check out our minimal implementation to see how easy it is to use:
👉 [capacitor-welcome.js Example Project](https://github.com/swaplab-engine/capacitor-admob-nextgen/blob/main/example-app/src/js/capacitor-welcome.js)

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

* [`initialize(...)`](#initialize)
* [`loadInterstitial(...)`](#loadinterstitial)
* [`showInterstitial()`](#showinterstitial)
* [`addListener(string, ...)`](#addlistenerstring-)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initialize(...)

```typescript
initialize(options: InitializeOptions) => Promise<InitializeResult>
```

Initializes the GMA Next-Gen SDK.

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#initializeoptions">InitializeOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#initializeresult">InitializeResult</a>&gt;</code>

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


### addListener(string, ...)

```typescript
addListener(eventName: string, listenerFunc: (info: any) => void) => Promise<PluginListenerHandle>
```

Listens for Ad events (e.g., onAdShowed, onAdDismissed).

| Param              | Type                                |
| ------------------ | ----------------------------------- |
| **`eventName`**    | <code>string</code>                 |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### Interfaces


#### InitializeResult

| Prop         | Type                |
| ------------ | ------------------- |
| **`status`** | <code>string</code> |


#### InitializeOptions

| Prop        | Type                |
| ----------- | ------------------- |
| **`appId`** | <code>string</code> |


#### InterstitialOptions

| Prop           | Type                |
| -------------- | ------------------- |
| **`adUnitId`** | <code>string</code> |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

</docgen-api>