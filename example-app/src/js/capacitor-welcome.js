import { AdMobNextGen } from 'capacitor-admob-nextgen';
import { SplashScreen } from '@capacitor/splash-screen';
import { SystemBars } from '@capacitor/core';


window.customElements.define(
  'capacitor-welcome',
  class extends HTMLElement {
    constructor() {
      super();
      this.attachShadow({ mode: 'open' });
    }

    connectedCallback() {
      this.shadowRoot.innerHTML = `
          <style>
          :host {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            display: block;
            height: 100vh;
            width: 100vw;
            margin: 0;
            padding: 0;
            overflow: hidden; 
            background-color: #000; 
          }

          .app-container {
            display: flex;
            flex-direction: column;
            height: 100%;
            background-color: #f4f5f8;
            padding-top: env(safe-area-inset-top, 0px);
            padding-bottom: env(safe-area-inset-bottom, 0px);
            box-sizing: border-box;
          }

          .debug-header, .debug-footer {
            background: repeating-linear-gradient(45deg, #ffc409, #ffc409 10px, #e0ab08 10px, #e0ab08 20px);
            color: #000; text-align: center; font-weight: 900; font-size: 12px; padding: 8px 0;
            text-transform: uppercase; letter-spacing: 2px; flex-shrink: 0; z-index: 10;
          }

          .scroll-area {
            flex: 1; 
            overflow-y: auto;
            padding: 10px;
            display: grid;
            grid-template-columns: 1fr 1fr; 
            gap: 10px;
            align-content: start;
          }

          .section {
            background: white; border-radius: 8px; padding: 10px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
            display: flex; flex-direction: column; gap: 6px;
          }
          .section.full-width { grid-column: 1 / -1; }
          .section-title { font-size: 13px; font-weight: bold; color: #3880ff; text-align: center; margin: 0 0 4px 0; }
          
          .controls {
            background: #f8f9fa; border: 1px solid #e0e0e0; border-radius: 6px; padding: 6px;
            display: grid; grid-template-columns: 1fr 1fr; gap: 4px; font-size: 11px; align-items: center;
          }
          .controls label { display: flex; justify-content: space-between; align-items: center; }
          .controls select, .controls input { padding: 2px; margin: 0; }

          .btn-row { display: flex; gap: 6px; }
          .btn {
            flex: 1; border: none; border-radius: 4px; padding: 8px 4px;
            font-size: 11px; font-weight: bold; color: white; cursor: pointer; text-align: center;
            transition: all 0.2s ease;
          }
          .btn:active { opacity: 0.8; transform: scale(0.98); }
          
          /* Colors */
          .btn-sys { background-color: #3880ff; } 
          .btn-load { background-color: #eb445a; } 
          .btn-show { background-color: #2dd36f; } 
          .btn-preload { background-color: #8e24aa; } /* Purple for next-gen APIs */
          .btn-alt { background-color: #92949c; } 
          
          /* Disabled State */
          .btn:disabled { 
            background-color: #cccccc !important; 
            color: #888888 !important;
            cursor: not-allowed; 
            transform: none;
            opacity: 0.7;
          }

          .terminal-wrapper {
            height: 32vh; 
            background-color: #222428;
            display: flex; flex-direction: column;
            border-top: 2px solid #111;
          }
          .terminal-header {
            background: #1a1b1e; color: #fff; font-size: 10px; padding: 4px 10px; font-weight: bold;
            display: flex; justify-content: space-between;
          }
          .status-badge { color: #eb445a; }
          .status-badge.ready { color: #2dd36f; }

          #terminal {
            flex: 1; overflow-y: auto; padding: 8px 10px;
            color: #2fdf75; font-family: monospace; font-size: 11px; word-wrap: break-word;
          }
          .log-time { color: #888; font-size: 9px; margin-right: 4px; }
        </style>

        <div class="app-container">
          <div class="debug-header">⬆ WEBVIEW TOP ⬆</div>

          <div class="scroll-area">
            
            <div class="section full-width">
              <h3 class="section-title">1. System Setup</h3>
              <div class="btn-row">
              <button class="btn btn-sys" id="btn-fullscreen">Fullscreen</button>
                <button class="btn btn-sys" id="btn-consent">Consent</button>
                <button class="btn btn-sys" id="btn-privacy">Privacy</button>
                <button class="btn btn-sys" id="btn-tcdata">TCData</button>
                <button class="btn btn-sys" id="btn-init">Init SDK</button>
              </div>
            </div>

            <div class="section full-width">
              <h3 class="section-title">2. Banner Ad</h3>
              <div class="controls">
                <label>Pos: <select id="banner-pos"><option value="BOTTOM">BTM</option><option value="TOP">TOP</option></select></label>
                <label>Overlap: <input type="checkbox" id="banner-overlap"></label>
                <label>Collapsible: <input type="checkbox" id="banner-col"></label>
                <label>AutoShow: <input type="checkbox" id="banner-auto" checked></label>
              </div>
              <div class="btn-row">
                <button class="btn btn-load ad-action" id="btn-banner-create" disabled>Create</button>
                <button class="btn btn-show ad-action" id="btn-banner-show" disabled>Show</button>
                <button class="btn btn-alt ad-action" id="btn-banner-hide" disabled>Hide</button>
                <button class="btn btn-alt ad-action" id="btn-banner-destroy" disabled>Destroy</button>
              </div>
            </div>

            <div class="section full-width" style="border: 1px solid #8e24aa;">
              <h3 class="section-title" style="color: #8e24aa;">2b. Banner (Next-Gen Preload)</h3>
              <div class="controls" style="text-align: center; color: #555;">
                Starts a background queue for Banner Ads. Overlap configurations are saved during Start.
              </div>
              <div class="btn-row" style="margin-bottom: 6px;">
                <button class="btn btn-preload ad-action" id="btn-ban-preload-start" disabled>Start Buffer</button>
                <button class="btn btn-alt ad-action" id="btn-ban-preload-check" disabled>Check Status</button>
                <button class="btn btn-show ad-action" id="btn-ban-preload-poll" disabled>Poll & Show</button>
              </div>
              <div class="btn-row">
                <button class="btn btn-alt ad-action" id="btn-ban-preload-hide" disabled>Hide</button>
                <button class="btn btn-alt ad-action" id="btn-ban-preload-destroy" disabled>Destroy</button>
              </div>
            </div>

            <div class="section">
              <h3 class="section-title">3. Interstitial</h3>
              <div class="btn-row">
                <button class="btn btn-load ad-action" id="btn-int-load" disabled>Load</button>
                <button class="btn btn-show ad-action" id="btn-int-show" disabled>Show</button>
              </div>
            </div>

            <div class="section full-width" style="border: 1px solid #8e24aa;">
              <h3 class="section-title" style="color: #8e24aa;">3b. Interstitial (Next-Gen Preload)</h3>
              <div class="controls" style="text-align: center; color: #555;">
                Starts a background queue for Interstitials.
              </div>
              <div class="btn-row">
                <button class="btn btn-preload ad-action" id="btn-int-preload-start" disabled>Start Buffer</button>
                <button class="btn btn-alt ad-action" id="btn-int-preload-check" disabled>Check Status</button>
                <button class="btn btn-show ad-action" id="btn-int-preload-poll" disabled>Poll & Show</button>
              </div>
            </div>

            <div class="section">
              <h3 class="section-title">4. Rewarded</h3>
              <div class="btn-row">
                <button class="btn btn-load ad-action" id="btn-rew-load" disabled>Load</button>
                <button class="btn btn-show ad-action" id="btn-rew-show" disabled>Show</button>
              </div>
            </div>

            <div class="section full-width" style="border: 1px solid #8e24aa;">
              <h3 class="section-title" style="color: #8e24aa;">4b. Rewarded (Next-Gen Preload)</h3>
              <div class="controls" style="text-align: center; color: #555;">
                Starts a background queue for Rewarded Ads.
              </div>
              <div class="btn-row">
                <button class="btn btn-preload ad-action" id="btn-rew-preload-start" disabled>Start Buffer</button>
                <button class="btn btn-alt ad-action" id="btn-rew-preload-check" disabled>Check Status</button>
                <button class="btn btn-show ad-action" id="btn-rew-preload-poll" disabled>Poll & Show</button>
              </div>
            </div>

            <div class="section">
              <h3 class="section-title">5. Rewarded Int.</h3>
              <div class="btn-row">
                <button class="btn btn-load ad-action" id="btn-rewint-load" disabled>Load</button>
                <button class="btn btn-show ad-action" id="btn-rewint-show" disabled>Show</button>
              </div>
            </div>

            <div class="section full-width" style="border: 1px solid #8e24aa;">
              <h3 class="section-title" style="color: #8e24aa;">5b. Rewarded Int. (Next-Gen Preload)</h3>
              <div class="controls" style="text-align: center; color: #555;">
                Starts a background queue for Rewarded Interstitial Ads.
              </div>
              <div class="btn-row">
                <button class="btn btn-preload ad-action" id="btn-rewint-preload-start" disabled>Start Buffer</button>
                <button class="btn btn-alt ad-action" id="btn-rewint-preload-check" disabled>Check Status</button>
                <button class="btn btn-show ad-action" id="btn-rewint-preload-poll" disabled>Poll & Show</button>
              </div>
            </div>

            <div class="section">
              <h3 class="section-title">6. App Open (Classic)</h3>
              <div class="btn-row">
                <button class="btn btn-load ad-action" id="btn-appopen-load" disabled>Load</button>
                <button class="btn btn-show ad-action" id="btn-appopen-show" disabled>Show</button>
              </div>
            </div>

            <div class="section full-width" style="border: 1px solid #8e24aa;">
              <h3 class="section-title" style="color: #8e24aa;">7. App Open (Next-Gen Preload)</h3>
              <div class="controls" style="text-align: center; color: #555;">
                Starts a background queue. Run 'Poll' to pull instantly!
              </div>
              <div class="btn-row">
                <button class="btn btn-preload ad-action" id="btn-preload-start" disabled>Start Buffer</button>
                <button class="btn btn-alt ad-action" id="btn-preload-check" disabled>Check Status</button>
                <button class="btn btn-show ad-action" id="btn-preload-poll" disabled>Poll & Show</button>
              </div>
            </div>

            <div class="section full-width" style="border: 1px solid #3880ff;">
              <h3 class="section-title">8. Native Ads</h3>
              <div class="controls">
                <label>Template: <select id="native-template"><option value="small">Small</option><option value="medium">Medium</option></select></label>
              </div>
              <div class="btn-row">
                <button class="btn btn-show ad-action" id="btn-native-show" disabled>Show Native</button>
                <button class="btn btn-alt ad-action" id="btn-native-hide" disabled>Hide Native</button>
              </div>
              <div id="native-anchor" class="native-ad-placeholder">
                NATIVE AD WILL APPEAR OVER THIS AREA
              </div>
            </div>

          </div> <div class="terminal-wrapper">
            <div class="terminal-header">
              <span>Real-time Event Log</span>
              <span class="status-badge" id="sdk-status">SDK LOCKED</span>
            </div>
            <div id="terminal"></div>
          </div>

          <div class="debug-footer">⬇ WEBVIEW BOTTOM ⬇</div>
        </div>
      `;

      SplashScreen.hide();

      this.isSystemBarsHidden = false;


      this.terminal = this.shadowRoot.getElementById('terminal');
      this.sdkStatusBadge = this.shadowRoot.getElementById('sdk-status');
      this.setupEventListeners();
      this.registerPluginEvents();

      this.logToTerminal('App Loaded. Please run Consent then Init SDK.', 'SYS');
    }

    logToTerminal(message, type = 'INFO') {
      const now = new Date();
      const timeStr = String(now.getSeconds()).padStart(2, '0') + '.' + String(now.getMilliseconds()).padStart(3, '0');
      const logElement = document.createElement('div');

      let color = '#2fdf75'; // Green
      if (type === 'ERROR') color = '#eb445a';
      if (type === 'EVENT') color = '#3dc2ff';
      if (type === 'SYS') color = '#ffce00';
      if (type === 'SUCCESS') color = '#2dd36f';
      if (type === 'NEXTGEN') color = '#e040fb'; // Purple for next-gen

      logElement.innerHTML = `<span class="log-time">[${timeStr}]</span> <span style="color: ${color}">${message}</span>`;
      this.terminal.appendChild(logElement);
      this.terminal.scrollTop = this.terminal.scrollHeight;
    }

    unlockAdButtons() {
      const adButtons = this.shadowRoot.querySelectorAll('.ad-action');
      adButtons.forEach(btn => btn.removeAttribute('disabled'));

      this.sdkStatusBadge.innerText = 'SDK READY';
      this.sdkStatusBadge.classList.add('ready');
      this.logToTerminal('✅ SDK Initialized! All Ad Buttons Unlocked.', 'SUCCESS');
    }

    setupEventListeners() {
      const getById = (id) => this.shadowRoot.getElementById(id);

      // ==========================================
      // 1. SYSTEM
      // ==========================================
      getById('btn-consent').addEventListener('click', async () => {
        try {
          this.logToTerminal('Requesting Consent Info...', 'SYS');
          const result = await AdMobNextGen.requestConsentInfo({
            debug: true,  // default: false
            reset: false, // default: false
            tagForUnderAgeOfConsent: false // default: false
          });
          this.logToTerminal(`Consent: canRequestAds=${result.canRequestAds}`);
        } catch (error) {
          this.logToTerminal(`Consent Error: ${error.message || error}`, 'ERROR');
        }
      });

      getById('btn-fullscreen').addEventListener('click', async () => {
        try {
          const btn = getById('btn-fullscreen');

          if (!this.isSystemBarsHidden) {
            this.logToTerminal('Toggling SystemBars (Fullscreen)...', 'SYS');

            await SystemBars.hide();

            this.isSystemBarsHidden = true;
            btn.innerText = 'Show SystemBars'; // Update UI label
            this.logToTerminal('Fullscreen Mode Active', 'SUCCESS');
          } else {
            this.logToTerminal('Restoring SystemBars (Normal)...', 'SYS');

            await SystemBars.show();

            this.isSystemBarsHidden = false;
            btn.innerText = 'Fullscreen'; // Reset UI label
            this.logToTerminal('Normal Mode Active', 'SUCCESS');
          }
        } catch (error) {
          this.logToTerminal(`Fullscreen Error: ${error.message || error}`, 'ERROR');
        }
      });

      getById('btn-privacy').addEventListener('click', async () => {
        try { await AdMobNextGen.showPrivacyOptionsForm(); }
        catch (error) { this.logToTerminal(`Privacy Form Error: ${error}`, 'ERROR'); }
      });

      getById('btn-tcdata').addEventListener('click', async () => {
        try {
          const result = await AdMobNextGen.getTCData();
          this.logToTerminal(`TCData: Personalized=${result.isPersonalizedAllowed}`);
        } catch (error) { this.logToTerminal(`TCData Error: ${error}`, 'ERROR'); }
      });

      getById('btn-init').addEventListener('click', async () => {
        try {
          this.logToTerminal('Initializing SDK...', 'SYS');
          const result = await AdMobNextGen.initialize({
            isTesting: true, // default: false
            maxAdContentRating: 'G', // default: ''
            tagForChildDirectedTreatment: false, // default: null
            tagForUnderAgeOfConsent: false // default: null
          });

          if (result.status) {
            this.unlockAdButtons();
          }
        } catch (error) {
          this.logToTerminal(`Init Error: ${error}`, 'ERROR');
        }
      });

      // ==========================================
      // ATT IOS
      // ==========================================

      /*
      const { status } = await AdMobNextGen.requestTrackingAuthorization();
            if (status === 'authorized') {
      }
      */

      // ==========================================
      // 2. BANNER
      // ==========================================
      getById('btn-banner-create').addEventListener('click', async () => {
        try {
          const pos = getById('banner-pos').value;
          const overlap = getById('banner-overlap').checked;
          const collaps = getById('banner-col').checked;
          const auto = getById('banner-auto').checked;

          this.logToTerminal(`Creating Banner [Pos:${pos}, Overlap:${overlap}]`, 'SYS');

          // Using a different test ID for Banner to avoid overlapping with App Open
          await AdMobNextGen.createBanner({
            adUnitId: 'ca-app-pub-3940256099942544/9214589741',
            position: pos,
            adSize: 'ADAPTIVE',
            isAutoShow: auto,
            isOverlap: overlap,
            isCollapsible: collaps
          });
        } catch (error) { this.logToTerminal(`Banner Error: ${error}`, 'ERROR'); }
      });

      getById('btn-banner-show').addEventListener('click', () => AdMobNextGen.showBanner());
      getById('btn-banner-hide').addEventListener('click', () => AdMobNextGen.hideBanner());
      getById('btn-banner-destroy').addEventListener('click', () => {
        AdMobNextGen.destroyBanner();
        this.logToTerminal('Banner Destroyed.', 'SYS');
      });

      // ==========================================
      // 2b. BANNER (NEXT-GEN PRELOAD)
      // ==========================================
      getById('btn-ban-preload-start').addEventListener('click', async () => {
        try {
          const pos = getById('banner-pos').value;
          const overlap = getById('banner-overlap').checked;
          const collaps = getById('banner-col').checked;

          this.logToTerminal('Starting Banner Buffer...', 'NEXTGEN');
          await AdMobNextGen.startPreloadBanner({
            adUnitId: 'ca-app-pub-3940256099942544/9214589741',
            position: pos,
            isOverlap: overlap,
            isCollapsible: collaps,
            bufferSize: 2
          });
        } catch (error) { this.logToTerminal(`Preload Start Error: ${error}`, 'ERROR'); }
      });

      getById('btn-ban-preload-check').addEventListener('click', async () => {
        try {
          const result = await AdMobNextGen.isBannerPreloadAvailable();
          this.logToTerminal(`Banner Preload Ready Status: ${result.isAvailable}`, 'NEXTGEN');
        } catch (error) { this.logToTerminal(`Preload Check Error: ${error}`, 'ERROR'); }
      });

      getById('btn-ban-preload-poll').addEventListener('click', async () => {
        try {
          this.logToTerminal('Polling Banner Buffer...', 'NEXTGEN');
          await AdMobNextGen.pollAndShowBanner();
        } catch (error) { this.logToTerminal(`Preload Poll Error: ${error}`, 'ERROR'); }
      });

      getById('btn-ban-preload-hide').addEventListener('click', () => AdMobNextGen.hidePreloadedBanner());
      getById('btn-ban-preload-destroy').addEventListener('click', () => {
        AdMobNextGen.destroyPreloadedBanner();
        this.logToTerminal('Preloaded Banner Destroyed.', 'SYS');
      });

      // ==========================================
      // 3. INTERSTITIAL
      // ==========================================
      getById('btn-int-load').addEventListener('click', async () => {
        try {
          this.logToTerminal('Loading Interstitial...', 'SYS');
          await AdMobNextGen.loadInterstitial({ adUnitId: 'ca-app-pub-3940256099942544/1033173712' });
        } catch (error) { this.logToTerminal(`Int Load Error: ${error}`, 'ERROR'); }
      });
      getById('btn-int-show').addEventListener('click', () => AdMobNextGen.showInterstitial());

      // ==========================================
      // 3b. INTERSTITIAL (NEXT-GEN PRELOAD)
      // ==========================================
      getById('btn-int-preload-start').addEventListener('click', async () => {
        try {
          this.logToTerminal('Starting Interstitial Buffer...', 'NEXTGEN');
          await AdMobNextGen.startPreloadInterstitial({
            adUnitId: 'ca-app-pub-3940256099942544/1033173712',
            bufferSize: 2 // Default: 1, maximum 3
          });
        } catch (error) { this.logToTerminal(`Preload Start Error: ${error}`, 'ERROR'); }
      });

      getById('btn-int-preload-check').addEventListener('click', async () => {
        try {
          const result = await AdMobNextGen.isInterstitialPreloadAvailable();
          this.logToTerminal(`Int Preload Ready Status: ${result.isAvailable}`, 'NEXTGEN');
        } catch (error) { this.logToTerminal(`Preload Check Error: ${error}`, 'ERROR'); }
      });

      getById('btn-int-preload-poll').addEventListener('click', async () => {
        try {
          this.logToTerminal('Polling Interstitial Buffer...', 'NEXTGEN');
          await AdMobNextGen.pollAndShowInterstitial();
        } catch (error) { this.logToTerminal(`Preload Poll Error: ${error}`, 'ERROR'); }
      });

      // ==========================================
      // 4. REWARDED
      // ==========================================
      getById('btn-rew-load').addEventListener('click', async () => {
        try {
          this.logToTerminal('Loading Rewarded...', 'SYS');
          await AdMobNextGen.loadRewarded({ adUnitId: 'ca-app-pub-3940256099942544/5224354917' });
        } catch (error) { this.logToTerminal(`Rew Load Error: ${error}`, 'ERROR'); }
      });
      getById('btn-rew-show').addEventListener('click', () => AdMobNextGen.showRewarded());

      // ==========================================
      // 4b. REWARDED (NEXT-GEN PRELOAD)
      // ==========================================
      getById('btn-rew-preload-start').addEventListener('click', async () => {
        try {
          this.logToTerminal('Starting Rewarded Buffer...', 'NEXTGEN');
          await AdMobNextGen.startPreloadRewarded({
            adUnitId: 'ca-app-pub-3940256099942544/5224354917',
            bufferSize: 2 // Default: 1, maximum 3
          });
        } catch (error) { this.logToTerminal(`Preload Start Error: ${error}`, 'ERROR'); }
      });

      getById('btn-rew-preload-check').addEventListener('click', async () => {
        try {
          const result = await AdMobNextGen.isRewardedPreloadAvailable();
          this.logToTerminal(`Rew Preload Ready Status: ${result.isAvailable}`, 'NEXTGEN');
        } catch (error) { this.logToTerminal(`Preload Check Error: ${error}`, 'ERROR'); }
      });

      getById('btn-rew-preload-poll').addEventListener('click', async () => {
        try {
          this.logToTerminal('Polling Rewarded Buffer...', 'NEXTGEN');
          await AdMobNextGen.pollAndShowRewarded();
        } catch (error) { this.logToTerminal(`Preload Poll Error: ${error}`, 'ERROR'); }
      });

      // ==========================================
      // 5. REWARDED INTERSTITIAL
      // ==========================================
      getById('btn-rewint-load').addEventListener('click', async () => {
        try {
          this.logToTerminal('Loading Rewarded Int...', 'SYS');
          await AdMobNextGen.loadRewardedInterstitial({ adUnitId: 'ca-app-pub-3940256099942544/5354046379' });
        } catch (error) { this.logToTerminal(`RewInt Load Error: ${error}`, 'ERROR'); }
      });
      getById('btn-rewint-show').addEventListener('click', () => AdMobNextGen.showRewardedInterstitial());

      // ==========================================
      // 5b. REWARDED INT. (NEXT-GEN PRELOAD)
      // ==========================================
      getById('btn-rewint-preload-start').addEventListener('click', async () => {
        try {
          this.logToTerminal('Starting Rewarded Int Buffer...', 'NEXTGEN');
          await AdMobNextGen.startPreloadRewardedInterstitial({
            adUnitId: 'ca-app-pub-3940256099942544/5354046379',
            bufferSize: 2 // Default: 1, maximum 3
          });
        } catch (error) { this.logToTerminal(`Preload Start Error: ${error}`, 'ERROR'); }
      });

      getById('btn-rewint-preload-check').addEventListener('click', async () => {
        try {
          const result = await AdMobNextGen.isRewardedInterstitialPreloadAvailable();
          this.logToTerminal(`RewInt Preload Ready Status: ${result.isAvailable}`, 'NEXTGEN');
        } catch (error) { this.logToTerminal(`Preload Check Error: ${error}`, 'ERROR'); }
      });

      getById('btn-rewint-preload-poll').addEventListener('click', async () => {
        try {
          this.logToTerminal('Polling Rewarded Int Buffer...', 'NEXTGEN');
          await AdMobNextGen.pollAndShowRewardedInterstitial();
        } catch (error) { this.logToTerminal(`Preload Poll Error: ${error}`, 'ERROR'); }
      });

      // ==========================================
      // 6. APP OPEN (CLASSIC)
      // ==========================================
      getById('btn-appopen-load').addEventListener('click', async () => {
        try {
          this.logToTerminal('Loading Classic App Open...', 'SYS');
          await AdMobNextGen.loadAppOpen({ adUnitId: 'ca-app-pub-3940256099942544/9257395921' });
        } catch (error) { this.logToTerminal(`AppOpen Load Error: ${error}`, 'ERROR'); }
      });
      getById('btn-appopen-show').addEventListener('click', () => AdMobNextGen.showAppOpen());

      // ==========================================
      // 7. APP OPEN (NEXT-GEN PRELOAD)
      // ==========================================
      getById('btn-preload-start').addEventListener('click', async () => {
        try {
          this.logToTerminal('Starting Preload Buffer...', 'NEXTGEN');
          await AdMobNextGen.startPreloadAppOpen({ adUnitId: 'ca-app-pub-3940256099942544/9257395921' });
        } catch (error) { this.logToTerminal(`Preload Start Error: ${error}`, 'ERROR'); }
      });

      getById('btn-preload-check').addEventListener('click', async () => {
        try {
          const result = await AdMobNextGen.isAppOpenPreloadAvailable();
          this.logToTerminal(`Preload Ready Status: ${result.isAvailable}`, 'NEXTGEN');
        } catch (error) { this.logToTerminal(`Preload Check Error: ${error}`, 'ERROR'); }
      });

      getById('btn-preload-poll').addEventListener('click', async () => {
        try {
          this.logToTerminal('Polling Buffer...', 'NEXTGEN');
          await AdMobNextGen.pollAndShowAppOpen();
        } catch (error) { this.logToTerminal(`Preload Poll Error: ${error}`, 'ERROR'); }
      });

      // ==========================================
      // 8. NATIVE ADS
      // ==========================================
      getById('btn-native-show').addEventListener('click', async () => {
        try {
          const template = getById('native-template').value;
          const anchor = getById('native-anchor');

          if (!anchor) return;

          const rect = anchor.getBoundingClientRect();

          this.logToTerminal(`Showing Native Ad (${template})...`, 'SYS');

          // MUST use Math.round() so that JS does not send decimal numbers to Java
          const result = await AdMobNextGen.showNativeAd({
            adUnitId: 'ca-app-pub-3940256099942544/2247696110',
            template: template,
            x: Math.round(rect.left + (window.scrollX || 0)),
            y: Math.round(rect.top + (window.scrollY || 0)),
            width: Math.round(rect.width)
          });

          // === Responsive HTML Container ===
          if (result && result.height) {
            anchor.innerHTML = '';
            anchor.style.boxSizing = 'border-box';

            // Force HTML height to match the height returned from Native Android/iOS
            anchor.style.height = result.height + 'px';

            this.logToTerminal(`Native Ad Rendered. Height: ${result.height}px`, 'SUCCESS');
          }

        } catch (error) {
          this.logToTerminal(`Native Show Error: ${error}`, 'ERROR');
        }
      });

      getById('btn-native-hide').addEventListener('click', async () => {
        try {
          await AdMobNextGen.hideNativeAd();
          this.logToTerminal('Native Ad Hidden/Destroyed.', 'SYS');
        } catch (error) {
          this.logToTerminal(`Native Hide Error: ${error}`, 'ERROR');
        }
      });

    }


    // ==========================================
    // GLOBAL PLUGIN EVENT LISTENERS
    // ==========================================
    registerPluginEvents() {
      const bindEvent = (eventName, customType = 'EVENT') => {
        AdMobNextGen.addListener(eventName, (data) => {
          let extra = data ? JSON.stringify(data) : '';
          // Clean up the JSON string for the small terminal window
          if (extra.length > 50) extra = extra.substring(0, 50) + '...}';
          this.logToTerminal(`${eventName} ${extra}`, customType);
        });
      };



      // (Banner Event Optional )
      // Variable to hold the loop
      let resizeTimeout;

      AdMobNextGen.addListener("onBannerOrientationChanged", async (data) => {
        console.log(`Device rotated to: ${data.orientation} - Active Unit: ${data.adUnitId}`);

        // 1. Clear timeout to prevent double calling if the user rotates the screen wildly
        clearTimeout(resizeTimeout);

        // 2. Please allow 300ms for the OS UI (Notch/Safe Area) to finish transitioning.
        resizeTimeout = setTimeout(async () => {
          try {
            await AdMobNextGen.destroyBanner();

            await AdMobNextGen.createBanner({
              adUnitId: data.adUnitId,
              position: 'BOTTOM',
              adSize: 'ADAPTIVE',
              isAutoShow: true,
              isOverlap: false,
              retryInterval: 0  // <--- IMPORTANT KEY: 5 second Anti-Spam Bypass specifically for rotation
            });
          } catch (error) {
            console.error("Failed to reload banner on orientation change:", error);
          }
        }, 300);
      });



      /*
      Unified Events & Preloader Exclusives
      To streamline development, the Preloader triggers the exact same lifecycle events as the Classic mode 
      (e.g., onBannerAdLoaded, onBannerAdPaid). You only need to write one event listener for both modes.
      */
      ['onConsentInfoUpdated', 'onConsentFormDismissed', 'onConsentStatusChange', 'onConsentError'].forEach(e => bindEvent(e));
      ['onBannerAdLoaded', 'onBannerAdFailedToLoad', 'onBannerAdImpression', 'onBannerAdClicked', 'onBannerAdPaid', 'onBannerOrientationChanged'].forEach(e => bindEvent(e));
      ['onInterstitialAdLoaded', 'onInterstitialAdFailedToLoad', 'onInterstitialAdShowed', 'onInterstitialAdDismissed', 'onInterstitialAdPaid'].forEach(e => bindEvent(e));
      ['onRewardedAdLoaded', 'onRewardedAdFailedToLoad', 'onRewardedAdShowed', 'onRewardedAdDismissed', 'onRewardedAdReward', 'onRewardedAdPaid'].forEach(e => bindEvent(e));
      ['onRewardedInterstitialAdLoaded', 'onRewardedInterstitialAdFailedToLoad', 'onRewardedInterstitialAdShowed', 'onRewardedInterstitialAdDismissed', 'onRewardedInterstitialAdReward', 'onRewardedInterstitialAdPaid'].forEach(e => bindEvent(e));
      ['onAppOpenAdLoaded', 'onAppOpenAdFailedToLoad', 'onAppOpenAdShowed', 'onAppOpenAdDismissed', 'onAppOpenAdPaid'].forEach(e => bindEvent(e));
      // Native Ads Events
      ['onNativeAdLoaded', 'onNativeAdFailedToLoad', 'onNativeAdShowed', 'onNativeAdDismissed', 'onNativeAdFailedToShow', 'onNativeAdImpression', 'onNativeAdClicked', 'onNativeAdPaid'].forEach(e => bindEvent(e));

      // Preloader Exclusive Event: The only event strictly unique to the Preloader is the exhausted event. 
      // This fires when the buffer pool is completely empty and the SDK stops trying to fetch new ads.
      // Next-Gen Preload Events
      ['onBannerPreloadExhausted'].forEach(e => bindEvent(e, 'NEXTGEN'));
      ['onAppOpenPreloadExhausted'].forEach(e => bindEvent(e, 'NEXTGEN'));
      ['onInterstitialPreloadExhausted'].forEach(e => bindEvent(e, 'NEXTGEN'));
      ['onRewardedPreloadExhausted'].forEach(e => bindEvent(e, 'NEXTGEN'));
      ['onRewardedInterstitialPreloadExhausted'].forEach(e => bindEvent(e, 'NEXTGEN'));

      /*
       Optional: Checking the Event Source If you need to execute specific conditional logic for the preloader, 
       you can check data.source. (Remember: This flag is Android-only).
       
       AdMobNextGen.addListener("onBannerAdLoaded", (data) => {
          let data = e.data || e;
          if (data && data.source === "preloader") {
              console.log("Ad loaded via Android Preloader Engine");
            } else {
           console.log("Ad loaded via Classic Engine (or iOS)");
          }
        });
       */


    }
  }
);