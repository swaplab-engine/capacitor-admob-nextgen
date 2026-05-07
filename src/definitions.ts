import type { PluginListenerHandle } from '@capacitor/core';

export interface AdMobNextGenPlugin {
  /**
   * Initializes the GMA Next-Gen SDK.
   */
  initialize(options: InitializeOptions): Promise<InitializeResult>;

  /**
   * Loads an Interstitial Ad.
   */
  loadInterstitial(options: InterstitialOptions): Promise<void>;

  /**
   * Shows the loaded Interstitial Ad.
   */
  showInterstitial(): Promise<void>;

  /**
   * Listens for Ad events (e.g., onAdShowed, onAdDismissed).
   */
  addListener(
    eventName: string,
    listenerFunc: (info: any) => void,
  ): Promise<PluginListenerHandle>;
}

export interface InitializeOptions {
  appId: string;
}

export interface InitializeResult {
  status: string;
}

export interface InterstitialOptions {
  adUnitId: string;
}
