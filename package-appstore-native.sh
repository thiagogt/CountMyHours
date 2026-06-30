#!/bin/bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
TARGET_DIR="$PROJECT_DIR/target"
ICON="$PROJECT_DIR/src/main/resources/com/countmyh/CountMyHours.icns"
ENTITLEMENTS="$PROJECT_DIR/entitlements.plist"
ENTITLEMENTS_CHILD="$PROJECT_DIR/entitlements-child.plist"
PROVISION_PROFILE="$PROJECT_DIR/CountMyHours.provisionprofile"

APP_NAME="CountMyHours"
APP_VERSION="3.2.10"
BUNDLE_ID="com.countmyh.CountMyHours"

SIGN_APP="3rd Party Mac Developer Application: THIAGO TOLEDO (3TJGYWF79B)"
SIGN_PKG="3rd Party Mac Developer Installer: THIAGO TOLEDO (3TJGYWF79B)"

GRAALVM_HOME="/Users/tgtoledo/Workstation/JDK/graalvm-jdk-21.0.11+9.1/Contents/Home"
GLUON_APP="$TARGET_DIR/gluonfx/aarch64-darwin/count-my-hours.app"
APP_BUNDLE="$TARGET_DIR/appstore/$APP_NAME.app"

NATIVE_BIN="$TARGET_DIR/gluonfx/aarch64-darwin/count-my-hours"

echo "=== Unlocking previous signed bundle (if any) ==="
if [ -d "$TARGET_DIR/appstore" ]; then
  xattr -cr "$TARGET_DIR/appstore" 2>/dev/null || true
  chflags -R nouchg "$TARGET_DIR/appstore" 2>/dev/null || true
  chmod -R u+w "$TARGET_DIR/appstore" 2>/dev/null || true
  rm -rf "$TARGET_DIR/appstore" 2>/dev/null || true
fi

echo "=== Building native image ==="
JAVA_HOME="$GRAALVM_HOME" GRAALVM_HOME="$GRAALVM_HOME" mvn clean gluonfx:build -q

echo "=== Cleaning previous appstore build ==="
if [ -d "$TARGET_DIR/appstore" ]; then
  chmod -R u+w "$TARGET_DIR/appstore" 2>/dev/null || true
  rm -rf "$TARGET_DIR/appstore"
fi
mkdir -p "$TARGET_DIR/appstore"

echo "=== Creating .app bundle ==="
mkdir -p "$APP_BUNDLE/Contents/MacOS"
cp "$NATIVE_BIN" "$APP_BUNDLE/Contents/MacOS/$APP_NAME"

# Set proper Info.plist
cat > "$APP_BUNDLE/Contents/Info.plist" << PLISTEOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleName</key>
    <string>$APP_NAME</string>
    <key>CFBundleDisplayName</key>
    <string>$APP_NAME</string>
    <key>CFBundleIdentifier</key>
    <string>$BUNDLE_ID</string>
    <key>CFBundleVersion</key>
    <string>$APP_VERSION</string>
    <key>CFBundleShortVersionString</key>
    <string>$APP_VERSION</string>
    <key>CFBundleExecutable</key>
    <string>$APP_NAME</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleIconFile</key>
    <string>CountMyHours.icns</string>
    <key>LSMinimumSystemVersion</key>
    <string>12.0</string>
    <key>LSApplicationCategoryType</key>
    <string>public.app-category.business</string>
    <key>NSHighResolutionCapable</key>
    <true/>
    <key>CFBundleSupportedPlatforms</key>
    <array>
        <string>MacOSX</string>
    </array>
</dict>
</plist>
PLISTEOF

# Add icon
mkdir -p "$APP_BUNDLE/Contents/Resources"
cp "$ICON" "$APP_BUNDLE/Contents/Resources/CountMyHours.icns"

echo "=== Embedding provisioning profile ==="
cp "$PROVISION_PROFILE" "$APP_BUNDLE/Contents/embedded.provisionprofile"

echo "=== Removing quarantine attributes ==="
xattr -cr "$APP_BUNDLE"

echo "=== Signing executable ==="
codesign --force --sign "$SIGN_APP" --entitlements "$ENTITLEMENTS" --options runtime "$APP_BUNDLE/Contents/MacOS/$APP_NAME"

echo "=== Signing app bundle ==="
codesign --force --sign "$SIGN_APP" --entitlements "$ENTITLEMENTS" --options runtime "$APP_BUNDLE"

echo "=== Verifying signature ==="
codesign --verify --deep --strict "$APP_BUNDLE"
echo "Signature OK"

echo "=== Building signed .pkg ==="
PKG_OUTPUT="$TARGET_DIR/appstore/$APP_NAME-$APP_VERSION.pkg"

productbuild \
  --component "$APP_BUNDLE" /Applications \
  --sign "$SIGN_PKG" \
  "$PKG_OUTPUT"

echo ""
echo "=== Done ==="
ls -lh "$PKG_OUTPUT"
echo ""
echo "Upload with Transporter or:"
echo "  xcrun altool --upload-app -f \"$PKG_OUTPUT\" -t macos -u thimakgt@gmail.com"
