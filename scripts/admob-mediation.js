const fs = require('fs');
const path = require('path');

console.log("-----------------------------------------");
console.log("AdMob MEDIATION SUITE (Capacitor Next-Gen)");
console.log("-----------------------------------------");

const projectRoot = process.cwd();
const pluginRoot = path.join(projectRoot, 'node_modules', 'capacitor-admob-nextgen');

// Read admobMediation configuration from main app package.json
function getConfig() {
  try {
    const packageJsonPath = path.join(projectRoot, 'package.json');
    if (fs.existsSync(packageJsonPath)) {
      const packageData = JSON.parse(fs.readFileSync(packageJsonPath, 'utf-8'));
      return packageData.admobMediation || {};
    }
  } catch (e) {
    console.warn("Failed to read package.json configuration.");
  }
  return {};
}

const config = getConfig();

// Read global switch with separate pod and spm support
// Default: spm on (Capacitor 8+), pod off, android and ios on
const globalPlatforms = config.platforms || { ios: true, pod: false, spm: true, android: true };

function isEnabled(key, platformName) {
  const val = config[key];

  if (typeof val === 'boolean') {
    if (!val) return false;
    return globalPlatforms[platformName] !== false;
  }

  if (typeof val === 'object' && val !== null) {
    return val[platformName] === true;
  }

  return false;
}

function getVer(key, defaultVer) {
  return config[key] || defaultVer;
}

function injectBetweenMarkers(content, startMarker, endMarker, injectionString) {
  const regex = new RegExp(`(${startMarker})[\\s\\S]*?(${endMarker})`, 'g');
  return content.replace(regex, `$1\n${injectionString}\n  $2`);
}

// =================================================================
// NETWORK DEFINITIONS 
// =================================================================
const NETWORKS = [
  {
    id: 'Facebook', toggle: 'enableFacebook', vAnd: 'verFacebookAndroid', dAnd: '6.16.0.0', vIos: 'verFacebookIos', dIos: '6.21.101',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:facebook:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationFacebook', '${v}'`,
    spmDep: (v) => `        .package(url: "https://github.com/googleads/googleads-mobile-ios-mediation-meta.git", exact: "${v}"),`,
    spmTarget: () => `                .product(name: "MetaAdapterTarget", package: "googleads-mobile-ios-mediation-meta"),`
  },

  {
    id: 'AppLovin', toggle: 'enableAppLovin', vAnd: 'verAppLovinAndroid', dAnd: '11.11.3.0', vIos: 'verAppLovinIos', dIos: '13.6.0',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:applovin:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationAppLovin', '${v}'`,
    spmDep: (v) => `        .package(url: "https://github.com/googleads/googleads-mobile-ios-mediation-applovin.git", exact: "${v}"),`,
    spmTarget: () => `                .product(name: "AppLovinAdapterTarget", package: "googleads-mobile-ios-mediation-applovin"),`
  },

  {
    id: 'Unity', toggle: 'enableUnity', vAnd: 'verUnityAndroid', dAnd: '4.9.2.0', vIos: 'verUnityIos', dIos: '4.17.0',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:unity:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationUnity', '${v}'`,
    spmDep: (v) => `        .package(url: "https://github.com/googleads/googleads-mobile-ios-mediation-unity.git", exact: "${v}"),`,
    spmTarget: () => `                .product(name: "UnityAdapterTarget", package: "googleads-mobile-ios-mediation-unity"),`
  },

  {
    id: 'ironSource', toggle: 'enableIronSource', vAnd: 'verIronSourceAndroid', dAnd: '8.7.0.0', vIos: 'verIronSourceIos', dIos: '9.4.10001',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:ironsource:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationIronSource', '${v}'`,
    spmDep: (v) => `        .package(url: "https://github.com/googleads/googleads-mobile-ios-mediation-ironsource.git", exact: "${v}"),`,
    spmTarget: () => `                .product(name: "IronSourceAdapterTarget", package: "googleads-mobile-ios-mediation-ironsource"),`
  },

  {
    id: 'Vungle', toggle: 'enableVungle', vAnd: 'verVungleAndroid', dAnd: '7.4.3.0', vIos: 'verVungleIos', dIos: '7.7.300',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:vungle:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationVungle', '${v}'`,
    spmDep: (v) => `        .package(url: "https://github.com/googleads/googleads-mobile-ios-mediation-liftoffmonetize.git", exact: "${v}"),`,
    spmTarget: () => `                .product(name: "LiftoffMonetizeAdapterTarget", package: "googleads-mobile-ios-mediation-liftoffmonetize"),`
  },

  {
    id: 'Chartboost', toggle: 'enableChartboost', repo: "        maven { url 'https://cboost.jfrog.io/artifactory/chartboost-ads/' }", vAnd: 'verChartboostAndroid', dAnd: '9.11.1.1', vIos: 'verChartboostIos', dIos: '9.12.1',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:chartboost:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationChartboost', '${v}'`,
    spmDep: (v) => `        .package(url: "https://github.com/googleads/googleads-mobile-ios-mediation-chartboost.git", exact: "${v}"),`,
    spmTarget: () => `                .product(name: "ChartboostAdapterTarget", package: "googleads-mobile-ios-mediation-chartboost"),`
  },

  {
    id: 'DT Exchange', toggle: 'enableDtExchange', vAnd: 'verDtExchangeAndroid', dAnd: '8.4.5.0', vIos: 'verDtExchangeIos', dIos: '8.4.701',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:fyber:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationFyber', '${v}'`,
    spmDep: (v) => `        .package(url: "https://github.com/googleads/googleads-mobile-ios-mediation-dtexchange.git", exact: "${v}"),`,
    spmTarget: () => `                .product(name: "DTExchangeAdapterTarget", package: "googleads-mobile-ios-mediation-dtexchange"),`
  },

  {
    id: 'iMobile', toggle: 'enableImobile', vAnd: 'verImobileAndroid', dAnd: '2.3.2.3', vIos: 'verImobileIos', dIos: '2.3.407',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:imobile:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationIMobile', '${v}'`,
    spmDep: (v) => `        .package(url: "https://github.com/googleads/googleads-mobile-ios-mediation-imobile.git", exact: "${v}"),`,
    spmTarget: () => `                .product(name: "IMobileAdapterTarget", package: "googleads-mobile-ios-mediation-imobile"),`
  },

  {
    id: 'InMobi', toggle: 'enableInmobi', repo: "        maven { url 'https://imobile.github.io/adnw-sdk-android' }", vAnd: 'verInmobiAndroid', dAnd: '11.2.0.0', vIos: 'verInmobiIos', dIos: '11.3.0',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:inmobi:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationInMobi', '${v}'`,
    spmDep: (v) => `        .package(url: "https://github.com/googleads/googleads-mobile-ios-mediation-inmobi.git", exact: "${v}"),`,
    spmTarget: () => `                .product(name: "InMobiAdapterTarget", package: "googleads-mobile-ios-mediation-inmobi"),`
  },

  {
    id: 'LINE', toggle: 'enableLine', vAnd: 'verLineAndroid', dAnd: '3.1.0.0', vIos: 'verLineIos', dIos: '3.0.101',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:line:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationLine', '${v}'`,
    spmDep: (v) => `        .package(url: "https://github.com/googleads/googleads-mobile-ios-mediation-line.git", exact: "${v}"),`,
    spmTarget: () => `                .product(name: "LineAdapterTarget", package: "googleads-mobile-ios-mediation-line"),`
  },

  {
    id: 'Maio', toggle: 'enableMaio', repo: "        maven { url 'https://imobile-maio.github.io/maven' }", vAnd: 'verMaioAndroid', dAnd: '2.0.8.2', vIos: 'verMaioIos', dIos: '2.2.102',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:maio:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationMaio', '${v}'`,
    spmDep: (v) => `        .package(url: "https://github.com/googleads/googleads-mobile-ios-mediation-maio.git", exact: "${v}"),`,
    spmTarget: () => `                .product(name: "MaioAdapterTarget", package: "googleads-mobile-ios-mediation-maio"),`
  },

  {
    id: 'Mintegral', toggle: 'enableMintegral', repo: "        maven { url 'https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea' }", vAnd: 'verMintegralAndroid', dAnd: '17.1.51.0', vIos: 'verMintegralIos', dIos: '8.1.400',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:mintegral:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationMintegral', '${v}'`,
    spmDep: (v) => `        .package(url: "https://github.com/googleads/googleads-mobile-ios-mediation-mintegral.git", exact: "${v}"),`,
    spmTarget: () => `                .product(name: "MintegralAdapterTarget", package: "googleads-mobile-ios-mediation-mintegral"),`
  },

  {
    id: 'Moloco', toggle: 'enableMoloco', vAnd: 'verMolocoAndroid', dAnd: '4.8.0.0', vIos: 'verMolocoIos', dIos: '4.6.0.0',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:moloco:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationMoloco', '${v}'`
  },

  {
    id: 'myTarget', toggle: 'enableMytarget', vAnd: 'verMytargetAndroid', dAnd: '5.45.3.0', vIos: 'verMytargetIos', dIos: '5.42.1.0',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:mytarget:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationMyTarget', '${v}'`
  },

  {
    id: 'Pangle', toggle: 'enablePangle', repo: "        maven { url 'https://artifact.bytedance.com/repository/pangle/' }", vAnd: 'verPangleAndroid', dAnd: '8.0.0.4.0', vIos: 'verPangleIos', dIos: '7.9.1.1.0',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:pangle:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationPangle', '${v}'`
  },

  {
    id: 'PubMatic', toggle: 'enablePubmatic', repo: "        maven { url 'https://repo.pubmatic.com/artifactory/public-repos' }", vAnd: 'verPubmaticAndroid', dAnd: '5.1.0.0', vIos: 'verPubmaticIos', dIos: '5.1.0.0',
    depAnd: (v) => `    implementation 'com.google.ads.mediation:pubmatic:${v}'`,
    depIos: (v) => `  s.dependency 'GoogleMobileAdsMediationPubMatic', '${v}'`
  }
];

// =================================================================
// ANDROID: INJECT GRADLE (Plugin Root)
// =================================================================
const buildGradlePath = path.join(pluginRoot, 'android', 'build.gradle');
if (fs.existsSync(buildGradlePath)) {
  let gradleData = fs.readFileSync(buildGradlePath, 'utf-8');

  let repoInjection = "    repositories {\n";
  let depsInjection = "";
  let hasCustomRepo = false;

  NETWORKS.forEach(net => {
    if (isEnabled(net.toggle, 'android')) {
      console.log(`Android: Mediating ${net.id}...`);
      const ver = getVer(net.vAnd, net.dAnd);
      depsInjection += net.depAnd(ver) + '\n';
      if (net.repo) {
        repoInjection += net.repo + '\n';
        hasCustomRepo = true;
      }
    }
  });
  repoInjection += "    }";

  gradleData = injectBetweenMarkers(gradleData, '// ADMOB_MEDIATION_REPOS_START', '// ADMOB_MEDIATION_REPOS_END', hasCustomRepo ? repoInjection : "");
  gradleData = injectBetweenMarkers(gradleData, '// ADMOB_MEDIATION_DEPENDENCIES_START', '// ADMOB_MEDIATION_DEPENDENCIES_END', depsInjection);

  fs.writeFileSync(buildGradlePath, gradleData, 'utf-8');
}

// =================================================================
// IOS: INJECT PACKAGE.SWIFT (SPM) - IF ENABLED
// =================================================================
if (globalPlatforms.spm !== false) {
  const packageSwiftPath = path.join(pluginRoot, 'Package.swift');
  if (fs.existsSync(packageSwiftPath)) {
    let packageData = fs.readFileSync(packageSwiftPath, 'utf-8');
    let spmDependenciesInjection = "";
    let spmTargetsInjection = "";

    NETWORKS.forEach(net => {
      if (isEnabled(net.toggle, 'ios') && net.spmDep && net.spmTarget) {
        console.log(`iOS (SPM): Mediating ${net.id}...`);
        const ver = getVer(net.vIos, net.dIos);
        spmDependenciesInjection += net.spmDep(ver) + '\n';
        spmTargetsInjection += net.spmTarget() + '\n';
      }
    });

    spmDependenciesInjection = spmDependenciesInjection.trimEnd();
    spmTargetsInjection = spmTargetsInjection.trimEnd();

    if (spmDependenciesInjection.endsWith(',')) spmDependenciesInjection = spmDependenciesInjection.slice(0, -1);
    if (spmTargetsInjection.endsWith(',')) spmTargetsInjection = spmTargetsInjection.slice(0, -1);

    packageData = injectBetweenMarkers(packageData, '// ADMOB_MEDIATION_SPM_DEPENDENCIES_START', '// ADMOB_MEDIATION_SPM_DEPENDENCIES_END', spmDependenciesInjection);
    packageData = injectBetweenMarkers(packageData, '// ADMOB_MEDIATION_SPM_TARGETS_START', '// ADMOB_MEDIATION_SPM_TARGETS_END', spmTargetsInjection);

    fs.writeFileSync(packageSwiftPath, packageData, 'utf-8');
    console.log("iOS: Package.swift configured successfully.");
  }
}

// =================================================================
// IOS: INJECT PODSPEC (COCOAPODS) - IF ENABLED
// =================================================================
if (globalPlatforms.pod === true) {
  if (fs.existsSync(pluginRoot)) {
    const files = fs.readdirSync(pluginRoot);
    const podspecFilename = files.find(f => f.toLowerCase().endsWith('.podspec'));

    if (podspecFilename) {
      const podspecPath = path.join(pluginRoot, podspecFilename);
      let podspecData = fs.readFileSync(podspecPath, 'utf-8');
      let iosInjection = "";

      NETWORKS.forEach(net => {
        if (isEnabled(net.toggle, 'ios') && net.depIos) {
          console.log(`iOS (Pods): Mediating ${net.id}...`);
          const ver = getVer(net.vIos, net.dIos);
          iosInjection += `  ${net.depIos(ver)}\n`;
        }
      });

      iosInjection = iosInjection.trimEnd();
      podspecData = injectBetweenMarkers(podspecData, '# ADMOB_MEDIATION_PODS_START', '# ADMOB_MEDIATION_PODS_END', iosInjection);
      fs.writeFileSync(podspecPath, podspecData, 'utf-8');
      console.log(`iOS: Dynamic validation passed. Podspec file [${podspecFilename}] updated successfully.`);
    }
  }
}

// =================================================================
// APP LEVEL INJECTIONS (AppLovin SDK Key)
// =================================================================
const appLovinKey = config.keyAppLovin;
const appLovinAndroidEnabled = isEnabled('enableAppLovin', 'android');
const appLovinIosEnabled = isEnabled('enableAppLovin', 'ios');

// Android Manifest
const androidManifestPath = path.join(projectRoot, 'android', 'app', 'src', 'main', 'AndroidManifest.xml');
if (fs.existsSync(androidManifestPath)) {
  let manifestData = fs.readFileSync(androidManifestPath, 'utf-8');

  manifestData = manifestData.replace(/<meta-data\s+android:name="applovin\.sdk\.key"[^>]*>\s*/g, '');

  if (appLovinAndroidEnabled && appLovinKey) {
    const metaTag = `<meta-data android:name="applovin.sdk.key" android:value="${appLovinKey}" />`;
    manifestData = manifestData.replace('</application>', `    ${metaTag}\n    </application>`);
    console.log("Android: AppLovin SDK Key Injected.");
  } else {
    console.log("Android: AppLovin SDK Key Cleaned/Removed.");
  }

  fs.writeFileSync(androidManifestPath, manifestData, 'utf-8');
}

// iOS Info.plist
const iosPlistPath = path.join(projectRoot, 'ios', 'App', 'App', 'Info.plist');
if (fs.existsSync(iosPlistPath)) {
  let plistData = fs.readFileSync(iosPlistPath, 'utf-8');

  plistData = plistData.replace(/<key>AppLovinSdkKey<\/key>\s*<string>.*?<\/string>\s*/g, '');

  if (appLovinIosEnabled && appLovinKey) {
    const keyTag = `<key>AppLovinSdkKey</key>\n\t<string>${appLovinKey}</string>`;
    plistData = plistData.replace('</dict>\n</plist>', `\t${keyTag}\n</dict>\n</plist>`);
    console.log("iOS: AppLovin SDK Key Injected.");
  } else {
    console.log("iOS: AppLovin SDK Key Cleaned/Removed.");
  }

  fs.writeFileSync(iosPlistPath, plistData, 'utf-8');
}

console.log("Mediation Suite Setup Completed.\n");