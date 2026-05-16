const fs = require('fs');
const path = require('path');

const projectRoot = process.cwd();
const packageJsonPath = path.join(projectRoot, 'package.json');

if (!fs.existsSync(packageJsonPath)) {
  console.log('[AdMob Next-Gen] package.json not found. Skipping App ID injection.');
  process.exit(0);
}

const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf8'));
const admobConfig = packageJson.admob;

if (!admobConfig) {
  console.log('⚠️ [AdMob Next-Gen] "admob" configuration object not found in package.json. Skipping injection.');
  process.exit(0);
}

if (!admobConfig.androidAppId && !admobConfig.iosAppId) {
  console.log('⚠️ [AdMob Next-Gen] AdMob App IDs not found in package.json. Skipping injection.');
  process.exit(0);
}

// ==========================================
// 1. ANDROID (AndroidManifest.xml)
// ==========================================
if (admobConfig.androidAppId) {
  const manifestPath = path.join(projectRoot, 'android', 'app', 'src', 'main', 'AndroidManifest.xml');

  if (fs.existsSync(manifestPath)) {
    let manifestContent = fs.readFileSync(manifestPath, 'utf8');
    const androidAppId = admobConfig.androidAppId;

    const metaDataRegex = /<meta-data\s+android:name="com\.google\.android\.gms\.ads\.APPLICATION_ID"\s+android:value="[^"]*"\s*\/>/g;
    const newMetaData = `<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="${androidAppId}" />`;

    if (manifestContent.match(metaDataRegex)) {
      manifestContent = manifestContent.replace(metaDataRegex, newMetaData);
      console.log(`✅ [AdMob Next-Gen] Updated Android App ID in AndroidManifest.xml`);
    } else {
      manifestContent = manifestContent.replace('</application>', `    ${newMetaData}\n    </application>`);
      console.log(`✅ [AdMob Next-Gen] Injected Android App ID into AndroidManifest.xml`);
    }

    fs.writeFileSync(manifestPath, manifestContent, 'utf8');
  } else {
    console.log(`ℹ️  [AdMob Next-Gen] Android platform folder not found. Skipping Android injection.`);
  }
}

// ==========================================
// 2. IOS (Info.plist)
// ==========================================
if (admobConfig.iosAppId) {
  const plistPath = path.join(projectRoot, 'ios', 'App', 'App', 'Info.plist');

  if (fs.existsSync(plistPath)) {
    let plistContent = fs.readFileSync(plistPath, 'utf8');
    const iosAppId = admobConfig.iosAppId;
    const trackingDesc = admobConfig.userTrackingDescription || "This identifier will be used to deliver personalized ads to you.";

    const gadRegex = /<key>GADApplicationIdentifier<\/key>\s*<string>[^<]*<\/string>/;
    const newGad = `<key>GADApplicationIdentifier</key>\n\t<string>${iosAppId}</string>`;

    if (plistContent.match(gadRegex)) {
      plistContent = plistContent.replace(gadRegex, newGad);
    } else {
      plistContent = plistContent.replace('</dict>\n</plist>', `\t${newGad}\n</dict>\n</plist>`);
    }

    const attRegex = /<key>NSUserTrackingUsageDescription<\/key>\s*<string>[^<]*<\/string>/;
    const newAtt = `<key>NSUserTrackingUsageDescription</key>\n\t<string>${trackingDesc}</string>`;

    if (plistContent.match(attRegex)) {
      plistContent = plistContent.replace(attRegex, newAtt);
    } else {
      plistContent = plistContent.replace('</dict>\n</plist>', `\t${newAtt}\n</dict>\n</plist>`);
    }

    // https://developers.google.com/admob/ios/3p-skadnetworks
    if (!plistContent.includes('<key>SKAdNetworkItems</key>')) {
      const skAdNetworkXML = `\t<key>SKAdNetworkItems</key>
\t<array>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>cstr6suwn9.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>4fzdc2evr5.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>2fnua5tdw4.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>ydx93a7ass.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>p78axxw29g.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>v72qych5uu.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>ludvb6z3bs.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>cp8zw746q7.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>3sh42y64q3.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>c6k4g5qg8m.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>s39g8k73mm.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>wg4vff78zm.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>3qy4746246.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>f38h382jlk.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>hs6bdukanm.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>mlmmfzh3r3.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>v4nxqhlyqp.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>wzmmz9fp6w.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>su67r6k2v3.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>yclnxrl5pm.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>t38b2kh725.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>7ug5zh24hu.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>gta9lk7p23.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>vutu7akeur.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>y5ghdn5j9k.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>v9wttpbfk9.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>n38lu8286q.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>47vhws6wlr.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>kbd757ywx3.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>9t245vhmpl.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>a2p9lx4jpn.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>22mmun2rn5.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>44jx6755aq.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>k674qkevps.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>4468km3ulz.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>2u9pt9hc89.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>8s468mfl3y.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>klf5c3l5u5.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>ppxm28t8ap.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>kbmxgpxpgc.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>uw77j35x4d.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>578prtvx9j.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>4dzt52r2t5.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>tl55sbb4fm.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>c3frkrj4fj.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>e5fvkxwrpn.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>8c4e2ghe7u.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>3rd42ekr43.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>97r2b46745.skadnetwork</string></dict>
\t\t<dict><key>SKAdNetworkIdentifier</key><string>3qcr597p9d.skadnetwork</string></dict>
\t</array>`;

      plistContent = plistContent.replace('</dict>\n</plist>', `${skAdNetworkXML}\n</dict>\n</plist>`);
      console.log(`✅ [AdMob Next-Gen] Injected SKAdNetworkItems into Info.plist`);
    } else {
      console.log(`ℹ️  [AdMob Next-Gen] SKAdNetworkItems already exist in Info.plist. Skipping to prevent duplication.`);
    }

    fs.writeFileSync(plistPath, plistContent, 'utf8');
    console.log(`✅ [AdMob Next-Gen] Updated iOS App ID and ATT description in Info.plist`);
  } else {
    console.log(`ℹ️  [AdMob Next-Gen] iOS platform folder not found. Skipping iOS injection.`);
  }
}