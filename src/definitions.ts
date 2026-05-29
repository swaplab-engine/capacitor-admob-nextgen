import type { PluginListenerHandle } from '@capacitor/core';

/**
 * # AdMob Next-Gen Plugin
 * * ### CRITICAL: Proper Execution Order
 * To comply with Google's policies (GDPR, COPPA, etc.), you MUST follow this exact execution order when your app starts:
 * 1. **Request Consent (UMP)**: `await AdMobNextGen.requestConsentInfo(...)`
 * *Always do this first. It checks if the user needs to see a privacy form.*
 * 2. **Initialize SDK**: `await AdMobNextGen.initialize(...)`
 * *Do this REGARDLESS of whether the consent form was shown or not. The SDK needs to boot up using the consent status gathered in step 1.*
 * 3. **Load Ads**: e.g., `await AdMobNextGen.loadInterstitial(...)`
 * 4. **Show Ads**: e.g., `await AdMobNextGen.showInterstitial()`
 * * ---
 */
export interface AdMobNextGenPlugin {

  // ==========================================
  // CONSENT (UMP) & PRIVACY
  // ==========================================

  /**
   * Requests App Tracking Transparency (ATT) authorization from the user.
   * This is explicitly required for iOS 14.5+ to display personalized ads.
   * On Android and Web, it automatically returns { status: 'authorized' }.
   * * @returns A promise resolving to the authorization status.
   */
  requestTrackingAuthorization(): Promise<TrackingAuthorizationResult>;

  /**
   * Requests consent information update and shows the consent form if required (UMP SDK).
   * MUST be called before `initialize()`.
   */
  requestConsentInfo(options?: ConsentOptions): Promise<ConsentStatusResult>;

  /**
   * Shows the privacy options form if the user wants to change their consent later.
   * Usually triggered by a button in your app's Settings menu.
   */
  showPrivacyOptionsForm(): Promise<ConsentStatusResult>;

  /**
   * Reads the IAB TCF v2.2 strings directly from SharedPreferences.
   * Useful if you need to pass consent strings to a custom analytics server.
   */
  getTCData(): Promise<TCDataResult>;
  
  // ==========================================
  // INITIALIZATION
  // ==========================================

  /**
   * Initializes the GMA Next-Gen SDK.
   * MUST be called after `requestConsentInfo()`.
   */
  initialize(options: InitializeOptions): Promise<InitializeResult>;

  // ==========================================
  // APP OPEN ADS
  // ==========================================

  /**
   * Loads an App Open Ad.
   */
  loadAppOpen(options: AppOpenOptions): Promise<void>;

  /**
   * Shows the loaded App Open Ad.
   */
  showAppOpen(): Promise<void>;

  // ==========================================
  // BANNER ADS
  // ==========================================

  /**
   * Creates and loads a Banner Ad. It can automatically show upon loading if isAutoShow is true.
   */
  createBanner(options: BannerOptions): Promise<void>;

  /**
   * Shows a previously created (and potentially hidden) banner ad.
   */
  showBanner(): Promise<void>;

  /**
   * Temporarily hides the banner ad without destroying it.
   */
  hideBanner(): Promise<void>;

  /**
   * Destroys the banner ad and cleans up resources from the view hierarchy.
   */
  destroyBanner(): Promise<void>;

  // ==========================================
  // INTERSTITIAL ADS
  // ==========================================

  /**
   * Loads an Interstitial Ad.
   */
  loadInterstitial(options: InterstitialOptions): Promise<void>;

  /**
   * Shows the loaded Interstitial Ad.
   */
  showInterstitial(): Promise<void>;

  // ==========================================
  // REWARDED ADS
  // ==========================================

  /**
   * Loads a Rewarded Ad.
   */
  loadRewarded(options: RewardedOptions): Promise<void>;

  /**
   * Shows the loaded Rewarded Ad.
   */
  showRewarded(): Promise<void>;

  // ==========================================
  // REWARDED INTERSTITIAL ADS
  // ==========================================

  /**
   * Loads a Rewarded Interstitial Ad.
   */
  loadRewardedInterstitial(options: RewardedInterstitialOptions): Promise<void>;

  /**
   * Shows the loaded Rewarded Interstitial Ad.
   */
  
  showRewardedInterstitial(): Promise<void>;

  // ==========================================
  // NATIVE ADS
  // ==========================================

  /**
   * Loads and displays a Native Ad at the specified screen coordinates using predefined templates.
   * * 💡 **NOTE:** To use this, you must set `"enableNativeAds": true` in your app's `package.json` 
   * under the `"admob"` configuration, and run `npx cap sync`.
   * * @param options The configuration and layout parameters for the Native Ad.
   */
  showNativeAd(options: NativeAdOptions): Promise<{ width: number; height: number }>;

  /**
   * Hides and destroys the currently displayed Native Ad.
   */
  hideNativeAd(): Promise<void>;
   
  
  /**
   * --------------------
   * ⚠️ CRITICAL NOTE: 
   * Do NOT mix the Classic API with the Next-Gen API. 
   * If you use `startPreload...`, you must ONLY use `pollAndShow...` to display the ad.
   * Calling classic methods like `load()` or `show()` while a preloader is active will cause unexpected behavior.
   * * Starts the background preloader buffer for App Open Ads.
   * This tells the SDK to automatically fetch and cache ads continuously.
   * * ⚠️ Do not use `loadAppOpen()` if you are using this preloader method.
   * --------------------
   * - **🍎 iOS LIMITATION:** The Next-Gen Preloading API is currently an Android-exclusive feature within the GMA SDK. When building or running on iOS devices, you MUST use the Classic single-load API (e.g., `loadAppOpen()` and `showAppOpen()`). Calling Preload methods on iOS will not trigger the native preloader.
   * --------------------
   */



  // ==========================================
  // NEXT-GEN PRELOADING API
  // ==========================================

  

 
  startPreloadAppOpen(options: AppOpenOptions): Promise<void>;

  /**
   * Pulls the next available ad from the preload buffer and shows it instantly.
   * If the buffer is empty (exhausted), this will throw an error.
   */
  pollAndShowAppOpen(): Promise<void>;

  /**
   * Synchronously checks if an ad is currently ready in the preload buffer.
   */
  isAppOpenPreloadAvailable(options?: { adUnitId?: string }): Promise<{ isAvailable: boolean }>;

  /**
   * Starts the background preloader buffer for Banner Ads.
   */
  startPreloadBanner(options: BannerOptions & { bufferSize?: number }): Promise<void>;

  /**
   * Pulls the next available Banner ad from the preload buffer and renders it immediately.
   */
  pollAndShowBanner(): Promise<void>;

  /**
   * Hides the preloaded banner without destroying it.
   */
  hidePreloadedBanner(): Promise<void>;

  /**
   * Destroys the current preloaded banner view.
   */
  destroyPreloadedBanner(): Promise<void>;

  /**
   * Synchronously checks if a Banner ad is ready in the preload buffer.
   */
  isBannerPreloadAvailable(options?: { adUnitId?: string }): Promise<{ isAvailable: boolean }>;

  /**
   * Starts the background preloader buffer for Interstitial Ads.
   */
  startPreloadInterstitial(options: InterstitialOptions & { bufferSize?: number }): Promise<void>;

  /**
   * Pulls the next available Interstitial ad from the preload buffer and shows it instantly.
   */
  pollAndShowInterstitial(): Promise<void>;

  /**
   * Synchronously checks if an Interstitial ad is ready in the preload buffer.
   */
  isInterstitialPreloadAvailable(options?: { adUnitId?: string }): Promise<{ isAvailable: boolean }>;

  /**
   * Starts the background preloader buffer for Rewarded Ads.
   */
  startPreloadRewarded(options: RewardedOptions & { bufferSize?: number }): Promise<void>;

  /**
   * Pulls the next available Rewarded ad from the preload buffer and shows it instantly.
   */
  pollAndShowRewarded(): Promise<void>;

  /**
   * Synchronously checks if a Rewarded ad is ready in the preload buffer.
   */
  isRewardedPreloadAvailable(options?: { adUnitId?: string }): Promise<{ isAvailable: boolean }>;

  /**
   * Starts the background preloader buffer for Rewarded Interstitial Ads.
   */
  startPreloadRewardedInterstitial(options: RewardedInterstitialOptions & { bufferSize?: number }): Promise<void>;

  /**
   * Pulls the next available Rewarded Interstitial ad from the preload buffer and shows it instantly.
   */
  pollAndShowRewardedInterstitial(): Promise<void>;

  /**
   * Synchronously checks if a Rewarded Interstitial ad is ready in the preload buffer.
   */
  isRewardedInterstitialPreloadAvailable(options?: { adUnitId?: string }): Promise<{ isAvailable: boolean }>;


  // ==========================================
  // EVENT LISTENERS
  // ==========================================

  /**
   * Listens for Ad events triggered by the native SDK.
   * * 💡 **NOTE ON PRELOADING:** If you are using the Next-Gen Preloading API, standard ad events 
   * (like Loaded, FailedToLoad, Impression, Paid, etc.) will include an additional property: 
   * `{ source: "preloader" }`. This allows you to easily distinguish them from Classic API events using a single listener.
   * * ### Interstitial Events
   * - `onInterstitialAdLoaded`: Fired when an ad is loaded. Returns { adUnitId: string, source?: string }.
   * - `onInterstitialAdFailedToLoad`: Fired when an ad fails to load. Returns { error: string, source?: string }.
   * - `onInterstitialAdShowed`: Fired when an ad is displayed.
   * - `onInterstitialAdDismissed`: Fired when an ad is closed by the user.
   * - `onInterstitialAdFailedToShow`: Fired when an ad fails to display. Returns { error: string, source?: string }.
   * - `onInterstitialAdImpression`: Fired when an ad impression is recorded.
   * - `onInterstitialAdClicked`: Fired when an ad is clicked.
   * - `onInterstitialAdPaid`: Fired when revenue is recorded. Returns AdPaidEvent.
   * * ### Rewarded Events
   * - `onRewardedAdLoaded`: Fired when an ad is loaded. Returns { adUnitId: string, source?: string }.
   * - `onRewardedAdFailedToLoad`: Fired when an ad fails to load. Returns { error: string, source?: string }.
   * - `onRewardedAdShowed`: Fired when an ad is displayed.
   * - `onRewardedAdDismissed`: Fired when an ad is closed.
   * - `onRewardedAdFailedToShow`: Fired when an ad fails to display. Returns { error: string, source?: string }.
   * - `onRewardedAdImpression`: Fired when an ad impression is recorded.
   * - `onRewardedAdClicked`: Fired when an ad is clicked.
   * - `onRewardedAdReward`: Fired when the user earns a reward. Returns { amount: number, type: string, source?: string }.
   * - `onRewardedAdPaid`: Fired when revenue is recorded. Returns AdPaidEvent.
   * * ### Rewarded Interstitial Events
   * - `onRewardedInterstitialAdLoaded`: Fired when an ad is loaded. Returns { adUnitId: string, source?: string }.
   * - `onRewardedInterstitialAdFailedToLoad`: Fired when an ad fails to load. Returns { error: string, source?: string }.
   * - `onRewardedInterstitialAdShowed`: Fired when an ad is displayed.
   * - `onRewardedInterstitialAdDismissed`: Fired when an ad is closed.
   * - `onRewardedInterstitialAdFailedToShow`: Fired when an ad fails to display. Returns { error: string, source?: string }.
   * - `onRewardedInterstitialAdImpression`: Fired when an ad impression is recorded.
   * - `onRewardedInterstitialAdClicked`: Fired when an ad is clicked.
   * - `onRewardedInterstitialAdReward`: Fired when the user earns a reward. Returns { amount: number, type: string, source?: string }.
   * - `onRewardedInterstitialAdPaid`: Fired when revenue is recorded. Returns AdPaidEvent.
   * * ### App Open Events
   * - `onAppOpenAdLoaded`: Fired when an ad is loaded. Returns { adUnitId: string, source?: string }.
   * - `onAppOpenAdFailedToLoad`: Fired when an ad fails to load. Returns { error: string, source?: string }.
   * - `onAppOpenAdShowed`: Fired when an ad is displayed.
   * - `onAppOpenAdDismissed`: Fired when an ad is closed.
   * - `onAppOpenAdFailedToShow`: Fired when an ad fails to display. Returns { error: string, source?: string }.
   * - `onAppOpenAdImpression`: Fired when an ad impression is recorded.
   * - `onAppOpenAdClicked`: Fired when an ad is clicked.
   * - `onAppOpenAdPaid`: Fired when revenue is recorded. Returns AdPaidEvent.
   * * ### Banner Events
   * - `onBannerAdLoaded`: Fired when a banner is loaded. Returns { adUnitId: string, width: number, height: number, widthPixels: number, heightPixels: number, isCollapsible: boolean, source?: string }.
   * - `onBannerAdFailedToLoad`: Fired when a banner fails to load. Returns { error: string, source?: string }.
   * - `onBannerAdImpression`: Fired when a banner impression is recorded.
   * - `onBannerAdClicked`: Fired when a banner is clicked.
   * - `onBannerAdShowedFullScreen`: Fired when a banner opens an overlay (e.g., user clicked it).
   * - `onBannerAdDismissedFullScreen`: Fired when the banner overlay is closed.
   * - `onBannerAdFailedToShowFullScreen`: Fired when the banner overlay fails to open. Returns { error: string, source?: string }.
   * - `onBannerAdRefreshed`: Fired when the banner auto-refreshes.
   * - `onBannerAdFailedToRefresh`: Fired when the banner auto-refresh fails. Returns { error: string, source?: string }.
   * - `onBannerAdPaid`: Fired when revenue is recorded. Returns AdPaidEvent.
   * * ### Native Events
   * - `onNativeAdLoaded`: Fired when a native ad is successfully loaded and rendered. Returns { width: number, height: number }.
   * - `onNativeAdFailedToLoad`: Fired when a native ad fails to load from the network. Returns { message: string }.
   * - `onNativeAdShowed`: Fired when the native ad opens an overlay (e.g., user clicked to view full content).
   * - `onNativeAdDismissed`: Fired when the native ad overlay is closed.
   * - `onNativeAdFailedToShow`: Fired when the native ad fails to show full-screen content. Returns { message: string }.
   * - `onNativeAdImpression`: Fired when a native ad impression is recorded.
   * - `onNativeAdClicked`: Fired when a native ad is clicked by the user.
   * - `onNativeAdPaid`: Fired when revenue is recorded. Returns AdPaidEvent.
   * * ### Consent (UMP) Events
   * - `onConsentInfoUpdated`: Fired when consent info is successfully updated.
   * - `onConsentFormDismissed`: Fired when the consent form overlay is closed.
   * - `onConsentStatusChange`: Fired when consent status changes. Returns ConsentStatusResult.
   * - `onConsentError`: Fired when a UMP error occurs. Returns { code: number, message: string }.
   * * ### Next-Gen Buffer Events (Exhausted)
   * These events are uniquely fired by the Preloading API when the background buffer runs out of cached ads.
   * - `onAppOpenAdPreloadExhausted`: Fired when the App Open buffer is empty. Returns { adUnitId: string, source: "preloader" }.
   * - `onInterstitialAdPreloadExhausted`: Fired when the Interstitial buffer is empty. Returns { adUnitId: string, source: "preloader" }.
   * - `onRewardedAdPreloadExhausted`: Fired when the Rewarded buffer is empty. Returns { adUnitId: string, source: "preloader" }.
   * - `onRewardedInterstitialAdPreloadExhausted`: Fired when the Rewarded Interstitial buffer is empty. Returns { adUnitId: string, source: "preloader" }.
   * - `onBannerAdPreloadExhausted`: Fired when the Banner buffer is empty. Returns { adUnitId: string, source: "preloader" }.
   * * @param eventName The name of the event to listen for.
   * @param listenerFunc The callback function handling the event data.
   */
  addListener(
    eventName: string,
    listenerFunc: (info: any) => void,
  ): Promise<PluginListenerHandle>;
}

// ==========================================
// INTERFACES & TYPES
// ==========================================

export interface TrackingAuthorizationResult {
  /**
   * The current status of the App Tracking Transparency (ATT) authorization.
   * - `authorized`: The user authorized access to app-related data for tracking.
   * - `denied`: The user denied authorization to access app-related data for tracking.
   * - `notDetermined`: The user has not yet received an authorization request.
   * - `restricted`: The authorization to access app-related data is restricted.
   * - `unknown`: Platform does not support ATT (e.g., Android, Web, or older iOS).
   */
  status: 'authorized' | 'denied' | 'notDetermined' | 'restricted' | 'unknown';
}

export interface ConsentOptions {
  /**
   * Enables debug mode to simulate user location in the EEA (European Economic Area) for testing UMP.
   * VERY IMPORTANT: Set to false in production.
   * Default: false
   */
  debug?: boolean;

  /**
   * Clears all previously saved consent data. Useful for simulating a first-time app launch.
   * Default: false
   */
  reset?: boolean;

  /**
   * Sets whether the user is under the age of consent for GDPR purposes.
   * Default: false
   */
  tagForUnderAgeOfConsent?: boolean;

  /**
   * A specific hashed device ID for debugging.
   * If `debug` is true and this is left empty, the plugin will automatically attempt to detect and register the current device.
   */
  testDeviceId?: string;
}

export interface ConsentStatusResult {
  /**
   * Indicates whether you have obtained sufficient consent to safely request ads.
   * Do not load ads if this is false.
   */
  canRequestAds: boolean;

  /**
   * The exact string representation of the privacy options requirement.
   * Possible values: "REQUIRED", "NOT_REQUIRED", "UNKNOWN".
   */
  privacyOptionsRequirementStatus: string;

  /**
   * A simplified boolean check for `privacyOptionsRequirementStatus === 'REQUIRED'`.
   * If true, you MUST provide a button in your app's settings allowing the user to modify their privacy choices, 
   * which triggers `showPrivacyOptionsForm()`.
   */
  isPrivacyOptionsRequired: boolean;

  /**
   * The current consent status integer indicator:
   * - 0 = UNKNOWN: Consent status is unknown.
   * - 1 = REQUIRED: User consent is required but has not been obtained yet.
   * - 2 = NOT_REQUIRED: User consent is not required (e.g., user is outside the EEA).
   * - 3 = OBTAINED: User consent has been successfully obtained.
   */
  consentStatus: number;
}

export interface TCDataResult {
  /**
   * The raw Transparency and Consent String (TC String) encoded in Base64.
   */
  tcString: string;

  /**
   * A binary string representing user consent for specific IAB purposes (e.g., "10011...").
   * Index 0 represents Purpose 1, index 1 represents Purpose 2, etc.
   */
  purposeConsents: string;

  /**
   * A binary string representing user consent for specific vendors.
   */
  vendorConsents: string;

  /**
   * Indicates whether GDPR applies to this user.
   * - 1 = GDPR applies.
   * - 0 = GDPR does not apply.
   */
  gdprApplies: number;

  /**
   * A smart helper boolean parsed natively by the plugin.
   * Returns true if IAB Purpose 1 is granted OR if GDPR does not apply.
   * Useful for quickly deciding whether to load personalized or non-personalized ads manually.
   */
  isPersonalizedAllowed: boolean;

  /**
   * A human-readable message explaining the parsing result of the TCData.
   */
  statusMessage: string;
}

export interface InitializeOptions {
  /**
   * Automatically detect and register the current device as a test device.
   * VERY IMPORTANT: Set this to false or remove it before publishing your app to the Play Store.
   * Default: false
   */
  isTesting?: boolean;
  
  /**
   * Filter ads by content rating.
   * 'G': General audiences
   * 'PG': Parental guidance
   * 'T': Teens
   * 'MA': Mature audiences
   * Default: "" (No filter)
   */
  maxAdContentRating?: 'G' | 'PG' | 'T' | 'MA' | '';

  /**
   * For purposes of the Children's Online Privacy Protection Act (COPPA).
   * true: Child-directed
   * false: Not child-directed
   * undefined: Unspecified
   */
  tagForChildDirectedTreatment?: boolean;

  /**
   * For users under the age of consent (GDPR).
   * true: Under age of consent
   * false: Not under age of consent
   * undefined: Unspecified
   */
  tagForUnderAgeOfConsent?: boolean;
}

export interface InitializeResult {
  status: string;
}

export interface AppOpenOptions {
  adUnitId: string;
  /**
   * Minimum time (in milliseconds) before the next ad request is allowed.
   * Prevents spam requests and invalid traffic bans. Default is 5000.
   */
  retryInterval?: number;
}

export interface BannerOptions {
  adUnitId: string;
  
  /**
   * Automatically show the banner after it successfully loads. Default is true.
   */
  isAutoShow?: boolean;
  
  /**
   * "TOP" or "BOTTOM". Default is "BOTTOM".
   */
  position?: 'TOP' | 'BOTTOM';
  
  /**
   * Specifies the banner size or adaptive behavior.
   * Default is "ADAPTIVE" (Current Orientation Anchored Adaptive).
   */
  adSize?: 
    | 'BANNER'
    | 'LARGE_BANNER'
    | 'MEDIUM_RECTANGLE'
    | 'FULL_BANNER'
    | 'LEADERBOARD'
    | 'LARGE_LANDSCAPE_ANCHORED_ADAPTIVE'
    | 'LARGE_PORTRAIT_ANCHORED_ADAPTIVE'
    | 'CURRENT_ORIENTATION_INLINE_ADAPTIVE'
    | 'LARGE_ANCHORED_ADAPTIVE'
    | 'PORTRAIT_INLINE_ADAPTIVE'
    | 'ADAPTIVE';
    
  /**
   * If true, the banner overlaps the webview. If false, it pushes the webview up/down. Default is true.
   */
  isOverlap?: boolean;
  
  /**
   * Enables Google's collapsible banner feature. Default is false.
   */
  isCollapsible?: boolean;
  
  /**
   * Minimum time (in milliseconds) before the next ad request is allowed.
   * Prevents spam requests and invalid traffic bans. Default is 5000.
   */
  retryInterval?: number;
}

export interface NativeAdOptions {
  adUnitId: string;
  
  /**
   * The name of the template to use (e.g., "small" or "medium").
   * Default is "small".
   */
  template?: 'small' | 'medium';

  /**
   * The X coordinate on the screen where the ad should appear.
   * ⚠️ IMPORTANT: Must be an integer (whole number). If calculating from DOM elements, use `Math.round()`.
   * Default is 0.
   */
  x?: number;

  /**
   * The Y coordinate on the screen where the ad should appear.
   * ⚠️ IMPORTANT: Must be an integer (whole number). If calculating from DOM elements, use `Math.round()`.
   * Default is 0.
   */
  y?: number;

  /**
   * The width of the ad in density-independent pixels (dp/pt).
   * ⚠️ IMPORTANT: Must be an integer (whole number). Use `Math.round()` if getting width from `getBoundingClientRect()`.
   * If not provided, it defaults to matching the parent view's width (MATCH_PARENT).
   */
  width?: number;

  /**
   * The height of the ad in density-independent pixels (dp/pt).
   * Note: The height is usually constrained by the template type (e.g., Small is ~120, Medium is ~350).
   * Default is WRAP_CONTENT.
   */
  height?: number;

  /**
   * Minimum time (in milliseconds) before the next ad request is allowed.
   * Prevents spam requests and invalid traffic bans. Default is 5000.
   */
  retryInterval?: number;
}

export interface InterstitialOptions {
  adUnitId: string;
  /**
   * Minimum time (in milliseconds) before the next ad request is allowed.
   * Prevents spam requests and invalid traffic bans. Default is 5000.
   */
  retryInterval?: number;
}

export interface RewardedOptions {
  adUnitId: string;
  /**
   * Minimum time (in milliseconds) before the next ad request is allowed.
   * Prevents spam requests and invalid traffic bans. Default is 5000.
   */
  retryInterval?: number;
}

export interface RewardedInterstitialOptions {
  adUnitId: string;
  /**
   * Minimum time (in milliseconds) before the next ad request is allowed.
   * Prevents spam requests and invalid traffic bans. Default is 5000.
   */
  retryInterval?: number;
}

/**
 * Represents the impression-level ad revenue data.
 * * * IMPORTANT: To receive these events, you MUST turn on the setting for impression-level 
 * ad revenue in your AdMob account:
 * 1. Sign in to your AdMob account at https://apps.admob.com.
 * 2. Click Settings in the sidebar.
 * 3. Click the Account tab.
 * 4. In the Account controls section, click the Impression-level ad revenue toggle to turn on this setting.
 * * Reference: https://support.google.com/admob/answer/11322405
 */
export interface AdPaidEvent {
  /**
   * The Ad Unit ID that generated the revenue.
   */
  adUnitId: string;
  
  /**
   * The ad revenue value in micros.
   */
  valueMicros: number;
  
  /**
   * The currency code (e.g., "USD").
   */
  currencyCode: string;
  
  /**
   * The precision type of the revenue value (e.g., "ESTIMATED", "PUBLISHER_PROVIDED", "PRECISE").
   */
  precisionType: string;
}