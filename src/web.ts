import { WebPlugin } from '@capacitor/core';

import type { 
  AdMobNextGenPlugin, 
  InitializeResult 
} from './definitions';

export class AdMobNextGenWeb extends WebPlugin implements AdMobNextGenPlugin {
  async initialize(): Promise<InitializeResult> {
    return { status: 'INITIALIZATION_BYPASSED_ON_WEB' };
  }

  async loadInterstitial(): Promise<void> {

  }

  async showInterstitial(): Promise<void> {

  }
}
