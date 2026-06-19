#!/bin/bash
set -e

JAVA_HOME="/Users/tgtoledo/Workstation/JDK/zulu23.32.11-ca-jdk23.0.2-macosx_aarch64/zulu-23.jdk/Contents/Home"
JPACKAGE="$JAVA_HOME/bin/jpackage"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
TARGET_DIR="$PROJECT_DIR/target"
ICON="$PROJECT_DIR/src/main/resources/com/countmyh/CountMyHours.icns"

APP_NAME="CountMyHours"
APP_VERSION="2.0.0"
MAIN_JAR="count-my-hours-2.0-SNAPSHOT.jar"
MAIN_CLASS="com.countmyh.App"
VENDOR="countmyh"

echo "=== Building project ==="
mvn clean package -DskipTests

echo ""
echo "=== Preparing jpackage input ==="
# Copy main JAR into lib so everything is in one input directory
cp "$TARGET_DIR/$MAIN_JAR" "$TARGET_DIR/lib/"

# Separate JavaFX modular JARs for the module-path
JAVAFX_MODS_DIR="$TARGET_DIR/javafx-mods"
mkdir -p "$JAVAFX_MODS_DIR"
mv "$TARGET_DIR/lib"/javafx-*-mac-aarch64.jar "$JAVAFX_MODS_DIR/" 2>/dev/null || true
mv "$TARGET_DIR/lib"/javafx-controls-23*.jar "$JAVAFX_MODS_DIR/" 2>/dev/null || true
mv "$TARGET_DIR/lib"/javafx-graphics-23*.jar "$JAVAFX_MODS_DIR/" 2>/dev/null || true
mv "$TARGET_DIR/lib"/javafx-base-23*.jar "$JAVAFX_MODS_DIR/" 2>/dev/null || true

echo ""
echo "=== Packaging macOS .dmg with jpackage ==="
rm -rf "$TARGET_DIR/installer"

$JPACKAGE \
  --type dmg \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --vendor "$VENDOR" \
  --icon "$ICON" \
  --input "$TARGET_DIR/lib" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --dest "$TARGET_DIR/installer" \
  --module-path "$JAVAFX_MODS_DIR" \
  --add-modules javafx.controls,javafx.graphics,javafx.base \
  --description "Work hours tracker and visualizer" \
  --mac-package-name "$APP_NAME" \
  --java-options "-Xmx512m"

echo ""
echo "=== Done ==="
echo "DMG installer created:"
ls -lh "$TARGET_DIR/installer/"*.dmg
echo ""
echo "To install: open the .dmg and drag CountMyHours to Applications"
