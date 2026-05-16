import { WebPlugin } from '@capacitor/core';

import type {
  AdMobNextGenPlugin,
  ConsentOptions,
  ConsentStatusResult,
  TCDataResult,
  InitializeOptions,
  InitializeResult,
  AppOpenOptions,
  BannerOptions,
  InterstitialOptions,
  RewardedOptions,
  RewardedInterstitialOptions
} from './definitions';

export class AdMobNextGenWeb extends WebPlugin implements AdMobNextGenPlugin {
  
  // ==========================================
  // CONSENT (UMP)
  // ==========================================
  async requestConsentInfo(_options?: ConsentOptions): Promise<ConsentStatusResult> {
    return {
      canRequestAds: true,
      privacyOptionsRequirementStatus: 'NOT_REQUIRED',
      isPrivacyOptionsRequired: false,
      consentStatus: 3 // OBTAINED
    };
  }

  async showPrivacyOptionsForm(): Promise<ConsentStatusResult> {
    return {
      canRequestAds: true,
      privacyOptionsRequirementStatus: 'NOT_REQUIRED',
      isPrivacyOptionsRequired: false,
      consentStatus: 3
    };
  }

  async getTCData(): Promise<TCDataResult> {
    return {
      tcString: '',
      purposeConsents: '',
      vendorConsents: '',
      gdprApplies: 0,
      isPersonalizedAllowed: true,
      statusMessage: 'Web fallback. Personalized allowed.'
    };
  }

  // ==========================================
  // INITIALIZATION
  // ==========================================
  async initialize(_options: InitializeOptions): Promise<InitializeResult> {
    return { status: 'INITIALIZATION_BYPASSED_ON_WEB' };
  }

  // ==========================================
  // APP OPEN
  // ==========================================
  async loadAppOpen(_options: AppOpenOptions): Promise<void> {
    // No-op for web
  }
  async showAppOpen(): Promise<void> {
    // No-op for web
  }

  // ==========================================
  // BANNER
  // ==========================================
  async createBanner(_options: BannerOptions): Promise<void> {
    // No-op for web
  }
  async showBanner(): Promise<void> {
    // No-op for web
  }
  async hideBanner(): Promise<void> {
    // No-op for web
  }
  async destroyBanner(): Promise<void> {
    // No-op for web
  }

  // ==========================================
  // INTERSTITIAL
  // ==========================================
  async loadInterstitial(_options: InterstitialOptions): Promise<void> {
    // No-op for web
  }
  async showInterstitial(): Promise<void> {
    // No-op for web
  }

  // ==========================================
  // REWARDED
  // ==========================================
  async loadRewarded(_options: RewardedOptions): Promise<void> {
    // No-op for web
  }
  async showRewarded(): Promise<void> {
    // No-op for web
  }

  // ==========================================
  // REWARDED INTERSTITIAL
  // ==========================================
  async loadRewardedInterstitial(_options: RewardedInterstitialOptions): Promise<void> {
    // No-op for web
  }
  async showRewardedInterstitial(): Promise<void> {
    // No-op for web
  }

  // ==========================================
  // NEXT-GEN PRELOADING API
  // ==========================================
  async startPreloadAppOpen(_options: AppOpenOptions): Promise<void> {
    // No-op for web
  }

  async pollAndShowAppOpen(): Promise<void> {
    // No-op for web
  }

  async isAppOpenPreloadAvailable(_options?: { adUnitId?: string }): Promise<{ isAvailable: boolean }> {
    // No-op for web
    return { isAvailable: false };
  }

  // ==========================================
  // NEXT-GEN PRELOADING API
  // ==========================================

  /* eslint-disable @typescript-eslint/no-empty-function */

  async startPreloadBanner(_options: BannerOptions): Promise<void> { }
  async pollAndShowBanner(): Promise<void> { }
  async hidePreloadedBanner(): Promise<void> { }
  async destroyPreloadedBanner(): Promise<void> { }

  /* eslint-enable @typescript-eslint/no-empty-function */

  async isBannerPreloadAvailable(_options?: { adUnitId?: string }): Promise<{ isAvailable: boolean }> {
    return { isAvailable: false };
  }

  async startPreloadInterstitial(_options: InterstitialOptions): Promise<void> {
    // No-op for web
  }

  async pollAndShowInterstitial(): Promise<void> {
    // No-op for web
  }

  async isInterstitialPreloadAvailable(_options?: { adUnitId?: string }): Promise<{ isAvailable: boolean }> {
    // No-op for web
    return { isAvailable: false };
  }

  async startPreloadRewarded(_options: RewardedOptions): Promise<void> {
    // No-op for web
  }

  async pollAndShowRewarded(): Promise<void> {
    // No-op for web
  }

  async isRewardedPreloadAvailable(_options?: { adUnitId?: string }): Promise<{ isAvailable: boolean }> {
    // No-op for web
    return { isAvailable: false };
  }

  async startPreloadRewardedInterstitial(_options: RewardedInterstitialOptions): Promise<void> {
    // No-op for web
  }

  async pollAndShowRewardedInterstitial(): Promise<void> {
    // No-op for web
  }

  async isRewardedInterstitialPreloadAvailable(_options?: { adUnitId?: string }): Promise<{ isAvailable: boolean }> {
    // No-op for web
    return { isAvailable: false };
  }
}