#!/bin/bash
set -euo pipefail

JAVA_HOME="/Users/tgtoledo/Workstation/JDK/zulu23.32.11-ca-jdk23.0.2-macosx_aarch64/zulu-23.jdk/Contents/Home"
JPACKAGE="$JAVA_HOME/bin/jpackage"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
TARGET_DIR="$PROJECT_DIR/target"
ICON="$PROJECT_DIR/src/main/resources/com/countmyh/CountMyHours.icns"
ENTITLEMENTS="$PROJECT_DIR/entitlements.plist"
ENTITLEMENTS_CHILD="$PROJECT_DIR/entitlements-child.plist"
PROVISION_PROFILE="$PROJECT_DIR/CountMyHours.provisionprofile"

APP_NAME="CountMyHours"
APP_VERSION="2.2.6"
MAIN_JAR="count-my-hours-2.2-SNAPSHOT.jar"
MAIN_CLASS="com.countmyh.App"
BUNDLE_ID="com.countmyh.CountMyHours"

SIGN_APP="3rd Party Mac Developer Application: THIAGO TOLEDO (3TJGYWF79B)"
SIGN_PKG="3rd Party Mac Developer Installer: THIAGO TOLEDO (3TJGYWF79B)"

echo "=== Building project ==="
mvn clean package -DskipTests -q

echo "=== Preparing jpackage input ==="
cp "$TARGET_DIR/$MAIN_JAR" "$TARGET_DIR/lib/"

JAVAFX_MODS_DIR="$TARGET_DIR/javafx-mods"
mkdir -p "$JAVAFX_MODS_DIR"
mv "$TARGET_DIR/lib"/javafx-*-mac-aarch64.jar "$JAVAFX_MODS_DIR/" 2>/dev/null || true
mv "$TARGET_DIR/lib"/javafx-controls-23*.jar "$JAVAFX_MODS_DIR/" 2>/dev/null || true
mv "$TARGET_DIR/lib"/javafx-graphics-23*.jar "$JAVAFX_MODS_DIR/" 2>/dev/null || true
mv "$TARGET_DIR/lib"/javafx-base-23*.jar "$JAVAFX_MODS_DIR/" 2>/dev/null || true

echo "=== Cleaning previous build ==="
if [ -d "$TARGET_DIR/appstore" ]; then
  chmod -R u+w "$TARGET_DIR/appstore" 2>/dev/null || true
  rm -rf "$TARGET_DIR/appstore"
fi

echo "=== Creating .app bundle ==="

$JPACKAGE \
  --type app-image \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --vendor "THIAGO TOLEDO" \
  --icon "$ICON" \
  --input "$TARGET_DIR/lib" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --dest "$TARGET_DIR/appstore" \
  --module-path "$JAVAFX_MODS_DIR" \
  --add-modules javafx.controls,javafx.graphics,javafx.base \
  --description "Work hours tracker and visualizer" \
  --mac-package-identifier "$BUNDLE_ID" \
  --mac-package-name "$APP_NAME" \
  --java-options "-Xmx512m"

APP_BUNDLE="$TARGET_DIR/appstore/$APP_NAME.app"
INFO_PLIST="$APP_BUNDLE/Contents/Info.plist"

echo "=== Embedding provisioning profile ==="
cp "$PROVISION_PROFILE" "$APP_BUNDLE/Contents/embedded.provisionprofile"
cp "$PROVISION_PROFILE" "$APP_BUNDLE/Contents/runtime/Contents/embedded.provisionprofile"
xattr -cr "$APP_BUNDLE"

echo "=== Setting minimum macOS 12.0 (arm64-only) ==="
/usr/libexec/PlistBuddy -c "Delete :LSMinimumSystemVersion" "$INFO_PLIST" 2>/dev/null || true
/usr/libexec/PlistBuddy -c "Add :LSMinimumSystemVersion string 12.0" "$INFO_PLIST"

echo "=== Signing JARs and dylibs (child entitlements) ==="
find "$APP_BUNDLE" -type f \( -name "*.jar" -o -name "*.dylib" \) -exec \
  codesign --force --sign "$SIGN_APP" --entitlements "$ENTITLEMENTS_CHILD" --options runtime {} \;

echo "=== Signing executables (child entitlements) ==="
find "$APP_BUNDLE/Contents/runtime" -type f -perm +111 ! -name "*.jar" ! -name "*.dylib" -exec \
  codesign --force --sign "$SIGN_APP" --entitlements "$ENTITLEMENTS_CHILD" --options runtime {} \;

echo "=== Signing runtime bundle (child entitlements) ==="
codesign --force --deep --sign "$SIGN_APP" --entitlements "$ENTITLEMENTS_CHILD" --options runtime "$APP_BUNDLE/Contents/runtime"

echo "=== Signing app bundle (full entitlements) ==="
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
