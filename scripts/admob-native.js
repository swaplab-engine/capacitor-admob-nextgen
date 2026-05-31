const fs = require('fs');
const path = require('path');

console.log("-----------------------------------------");
console.log("AdMob NATIVE ADS SETUP");
console.log("-----------------------------------------");

const projectRoot = process.cwd();
const pluginRoot = path.join(projectRoot, 'node_modules', 'capacitor-admob-nextgen');

function getConfig() {
    try {
        const packageJsonPath = path.join(projectRoot, 'package.json');
        if (fs.existsSync(packageJsonPath)) {
            const packageData = JSON.parse(fs.readFileSync(packageJsonPath, 'utf-8'));
            return packageData.admob || {};
        }
    } catch (e) {
        console.warn("Failed to read package.json configuration.");
    }
    return {};
}

const config = getConfig();

let enableAndroid = false;
let enableIos = false;

if (typeof config.enableNativeAds === 'boolean') {
    enableAndroid = config.enableNativeAds;
    enableIos = config.enableNativeAds;
} else if (typeof config.enableNativeAds === 'object' && config.enableNativeAds !== null) {
    enableAndroid = config.enableNativeAds.android === true;
    enableIos = config.enableNativeAds.ios === true;
}

function injectBetweenMarkers(content, startMarker, endMarker, injectionString) {
    const regex = new RegExp(`(${startMarker})[\\s\\S]*?(${endMarker})`, 'g');
    return content.replace(regex, `$1\n${injectionString}\n    $2`);
}

// =================================================================
// 1. ANDROID INJECTIONS
// =================================================================
const buildGradlePath = path.join(pluginRoot, 'android', 'build.gradle');
if (fs.existsSync(buildGradlePath)) {
    let gradleData = fs.readFileSync(buildGradlePath, 'utf-8');
    
    // Inject Source Sets
    let resInjection = enableAndroid ? "        res.srcDirs += ['src/main/native-templates/res']" : "";
    gradleData = injectBetweenMarkers(gradleData, '// ADMOB_NATIVE_RES_START', '// ADMOB_NATIVE_RES_END', resInjection);

    // Inject Dependencies
    let depsInjection = enableAndroid ? "    implementation 'androidx.constraintlayout:constraintlayout:2.2.1'" : "";
    gradleData = injectBetweenMarkers(gradleData, '// ADMOB_NATIVE_DEPENDENCIES_START', '// ADMOB_NATIVE_DEPENDENCIES_END', depsInjection);

    fs.writeFileSync(buildGradlePath, gradleData, 'utf-8');
    console.log(`Android: Native Ads ${enableAndroid ? 'ENABLED' : 'DISABLED'}`);
}

// =================================================================
// 2. IOS INJECTIONS
// =================================================================
const packageSwiftPath = path.join(pluginRoot, 'Package.swift');
if (fs.existsSync(packageSwiftPath)) {
    let packageData = fs.readFileSync(packageSwiftPath, 'utf-8');
    
    let resourceInjection = enableIos ? '                .process("NativeTemplates")' : "";
    packageData = injectBetweenMarkers(packageData, '// ADMOB_NATIVE_RESOURCES_START', '// ADMOB_NATIVE_RESOURCES_END', resourceInjection);

    fs.writeFileSync(packageSwiftPath, packageData, 'utf-8');
    console.log(`iOS: Native Ads ${enableIos ? 'ENABLED' : 'DISABLED'}`);
}

console.log("Native setup completed.\n");