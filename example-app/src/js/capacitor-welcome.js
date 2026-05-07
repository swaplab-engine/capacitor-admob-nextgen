import { AdMobNextGen } from 'capacitor-admob-nextgen';
import { SplashScreen } from '@capacitor/splash-screen'; 

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
            font-family: -apple-system, sans-serif;
            display: block;
            padding: 20px;
            text-align: center;
          }
          .btn {
            background-color: #3880ff;
            color: white;
            border: none;
            border-radius: 8px;
            padding: 12px 24px;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
            margin: 10px;
            width: 80%;
          }
          .btn:active { background-color: #2b61c4; }
          .btn-secondary { background-color: #5260ff; }
          .btn-success { background-color: #2dd36f; }
          #status-container {
            margin-top: 20px;
            padding: 15px;
            background-color: #f4f5f8;
            border-radius: 8px;
            word-wrap: break-word;
          }
        </style>

        <div>
          <h2>AdMob Next-Gen Plugin</h2>
          <p>Testing environment for Capacitor Android.</p>
          
          <button class="btn" id="btn-initialize">1. Initialize SDK</button>
          <button class="btn btn-secondary" id="btn-load-interstitial">2. Load Interstitial</button>
          <button class="btn btn-success" id="btn-show-interstitial">3. Show Interstitial</button>
          
          <div id="status-container">
            <strong>Log:</strong> <span id="status-text">Ready</span>
          </div>
        </div>
      `;

      SplashScreen.hide();

      const statusText = this.shadowRoot.getElementById('status-text');

      // 1. Initialize
      this.shadowRoot.getElementById('btn-initialize').addEventListener('click', async () => {
        try {
          statusText.innerText = 'Initializing SDK...';
          const options = { appId: 'ca-app-pub-3940256099942544~3347511713' };
          const result = await AdMobNextGen.initialize(options);
          statusText.innerText = result.status;
        } catch (error) {
          statusText.innerText = error.message;
        }
      });

      // 2. Load Interstitial
      this.shadowRoot.getElementById('btn-load-interstitial').addEventListener('click', async () => {
        try {
          statusText.innerText = 'Loading Ad...';
          // Using the Test Ad Unit ID from Google documentation
          await AdMobNextGen.loadInterstitial({ adUnitId: 'ca-app-pub-3940256099942544/1033173712' });
          statusText.innerText = 'Interstitial Ad Loaded!';
        } catch (error) {
          statusText.innerText = 'Load Error: ' + error.message;
        }
      });

      // 3. Show Interstitial
      this.shadowRoot.getElementById('btn-show-interstitial').addEventListener('click', async () => {
        try {
          statusText.innerText = 'Showing Ad...';
          await AdMobNextGen.showInterstitial();
          statusText.innerText = 'Ad is visible on screen.';
        } catch (error) {
          statusText.innerText = 'Show Error: ' + error.message;
        }
      });

      // Event Listeners (Optional but recommended)
      AdMobNextGen.addListener('onAdDismissed', () => {
        statusText.innerText = 'Ad closed by user.';
      });
    }
  }
);