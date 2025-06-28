# Voboost Config Library - Manual Test Plan

## Overview
This test plan provides manual testing procedures based on the automated test suite for the Voboost Config Android library. The library provides type-safe YAML configuration management with real-time file watching capabilities.

## Test Environment Setup

### Prerequisites
- Android device or emulator (API 28+)
- Test YAML configuration files
- File manager or text editor for modifying files
- Android app with Voboost Config library integrated

### Test Data Files

Create the following test configuration files:

**sample_config.yaml** (Complete configuration):
```yaml
settings:
  language: en
  theme: dark
  interface-shift-x: 0
  interface-shift-y: 0

vehicle:
  fuel-mode: intellectual
  drive-mode: comfort
```

**partial_config.yaml** (Partial configuration):
```yaml
settings:
  language: en
  theme: dark

# Missing vehicle section entirely
```

**invalid_config.yaml** (Invalid configuration):
```yaml
settings:
  language: en
  theme: dark
  invalid-yaml-structure: [
    - missing closing bracket
vehicle:
  fuel-mode: invalid_mode
  drive-mode: 123
    invalid-indentation
```

---

## Test Suite 1: Basic Configuration Manager Functionality

### Test 1.1: ConfigManager Initialization
**Objective**: Verify that ConfigManager can be instantiated successfully.

**Steps**:
1. Open the test application
2. Verify that ConfigManager instance is created without errors
3. Check that no exceptions are thrown during initialization

**Expected Result**: ConfigManager should be instantiated successfully without any errors.

---

### Test 1.2: Load Non-Existent Configuration File
**Objective**: Verify proper error handling when loading a non-existent file.

**Steps**:
1. Attempt to load a configuration file that doesn't exist (e.g., "nonexistent.yaml")
2. Observe the result

**Expected Result**:
- Operation should fail gracefully
- Should return a failure result (not crash)
- Error message should indicate that the file doesn't exist

---

## Test Suite 2: Configuration Data Model Tests

### Test 2.1: Create Configuration with Partial Data
**Objective**: Verify that Config objects can be created with only some fields set.

**Steps**:
1. Create a Config object with only settings.language = "en" and settings.theme = "dark"
2. Verify the created object

**Expected Result**:
- Language should be set to "en"
- Theme should be set to "dark"
- Vehicle section should be null
- Unset fields should be null

---

### Test 2.2: Create Empty Configuration
**Objective**: Verify that Config objects can be created with all fields null.

**Steps**:
1. Create a Config object with no parameters
2. Verify all fields

**Expected Result**:
- All fields (settings, vehicle) should be null by default

---

## Test Suite 3: Configuration Difference (Diff) Logic Tests

### Test 3.1: No Changes Detection
**Objective**: Verify that diff calculation correctly identifies when no changes occurred.

**Steps**:
1. Create two identical Config objects with:
   - settings.language = "en"
   - settings.theme = "dark"
   - settings.interfaceShiftX = 10
   - settings.interfaceShiftY = 20
   - vehicle.fuelMode = "intellectual"
   - vehicle.driveMode = "comfort"
2. Calculate diff between them

**Expected Result**:
- All fields in diff should be null (no changes detected)

---

### Test 3.2: Single Field Change Detection
**Objective**: Verify that diff calculation correctly identifies single field changes.

**Steps**:
1. Create first Config with settings.language = "en"
2. Create second Config with settings.language = "ru" (all other fields identical)
3. Calculate diff between them

**Expected Result**:
- Only settings.language should be non-null in diff (set to "ru")
- All other fields should be null in diff

---

### Test 3.3: Multiple Changes Detection
**Objective**: Verify that diff calculation correctly identifies multiple field changes.

**Steps**:
1. Create first Config with:
   - settings.language = "en"
   - settings.theme = "dark"
   - settings.interfaceShiftX = 10
   - vehicle.fuelMode = "intellectual"
   - vehicle.driveMode = "comfort"
2. Create second Config with:
   - settings.language = "ru" (changed)
   - settings.theme = "light" (changed)
   - settings.interfaceShiftX = 10 (same)
   - vehicle.fuelMode = "electric" (changed)
   - vehicle.driveMode = "comfort" (same)
3. Calculate diff

**Expected Result**:
- Diff should contain only changed fields:
  - settings.language = "ru"
  - settings.theme = "light"
  - vehicle.fuelMode = "electric"
- Unchanged fields should be null in diff

---

### Test 3.4: Null to Value Change Detection
**Objective**: Verify that changes from null to value are detected.

**Steps**:
1. Create first Config with settings.language = null
2. Create second Config with settings.language = "en"
3. Calculate diff

**Expected Result**:
- Diff should contain settings.language = "en"

---

### Test 3.5: Value to Null Change Detection
**Objective**: Verify that changes from value to null are detected.

**Steps**:
1. Create first Config with settings.language = "en"
2. Create second Config with settings.language = null
3. Calculate diff

**Expected Result**:
- Diff should contain settings.language = null

---

## Test Suite 4: File Watching Functionality Tests

### Test 4.1: Start Watching Non-Existent File
**Objective**: Verify proper error handling when starting to watch a non-existent file.

**Steps**:
1. Attempt to start watching a file that doesn't exist
2. Provide a valid change listener
3. Observe the result

**Expected Result**:
- Operation should fail
- Should return IllegalArgumentException
- Error message should mention "Configuration file does not exist"

---

### Test 4.2: Stop Watching Safety
**Objective**: Verify that stopping file watching is safe to call multiple times.

**Steps**:
1. Call stopWatching() multiple times in succession
2. Verify no exceptions are thrown

**Expected Result**:
- No exceptions should be thrown
- Multiple calls should be safe

---

### Test 4.3: Stop Watching Without Starting
**Objective**: Verify that stopping file watching is safe even if never started.

**Steps**:
1. Create a new ConfigManager instance
2. Call stopWatching() without ever calling startWatching()
3. Verify no exceptions are thrown

**Expected Result**:
- No exceptions should be thrown
- Should be safe to call

---

### Test 4.4: Configuration Change Listener Interface
**Objective**: Verify that the change listener interface works correctly.

**Steps**:
1. Create a change listener that tracks callback invocations
2. Manually trigger the callback with test data
3. Verify callback parameters

**Expected Result**:
- Callback should be invoked
- New config parameter should be passed correctly
- Diff parameter should be passed correctly

---

### Test 4.5: Real File Change Detection
**Objective**: Verify that actual file modifications are detected and trigger callbacks.

**Steps**:
1. Create a valid config file with initial content:
   ```yaml
   settings:
     language: en
     theme: light
   vehicle:
     fuel-mode: fuel
   ```
2. Start watching the file with a change listener
3. Wait for watcher to initialize (few seconds)
4. Modify the file to:
   ```yaml
   settings:
     language: ru
     theme: dark
   vehicle:
     fuel-mode: electric
   ```
5. Wait for callback (up to 10 seconds)
6. Verify callback data

**Expected Result**:
- Change callback should be triggered within 10 seconds
- New config should contain updated values:
  - settings.language = "ru"
  - settings.theme = "dark"
  - vehicle.fuelMode = "electric"
- Diff should contain only changed fields

---

## Test Suite 5: Integration Tests with Real YAML Files

### Test 5.1: Load Complete Configuration File
**Objective**: Verify loading a complete YAML configuration file.

**Steps**:
1. Place sample_config.yaml in the app's files directory
2. Load the configuration using ConfigManager
3. Verify all fields

**Expected Result**:
- Loading should succeed
- All fields should be loaded correctly:
  - settings.language = "en"
  - settings.theme = "dark"
  - settings.interfaceShiftX = 0
  - settings.interfaceShiftY = 0
  - vehicle.fuelMode = "intellectual"
  - vehicle.driveMode = "comfort"

---

### Test 5.2: Load Configuration with All Enum Values
**Objective**: Verify that all possible enum values can be loaded from YAML.

**Steps**:
1. Create a config file with different enum values:
   ```yaml
   settings:
     language: ru
     theme: auto
     interface-shift-x: 15
     interface-shift-y: -10
   vehicle:
     fuel-mode: electric
     drive-mode: sport
   ```
2. Load the configuration
3. Verify enum parsing

**Expected Result**:
- Loading should succeed
- Enum values should be correctly parsed:
  - settings.language = "ru"
  - settings.theme = "auto"
  - vehicle.fuelMode = "electric"
  - vehicle.driveMode = "sport"

---

### Test 5.3: Load Partial Configuration
**Objective**: Verify loading YAML with only some fields set.

**Steps**:
1. Place partial_config.yaml in the app's files directory
2. Load the configuration
3. Verify field values

**Expected Result**:
- Loading should succeed
- Set fields should have correct values:
  - settings.language = "en"
  - settings.theme = "light"
  - vehicle.fuelMode = "fuel"
- Unset fields should be null:
  - settings.interfaceShiftX = null
  - settings.interfaceShiftY = null
  - vehicle.driveMode = null

---

### Test 5.4: Load Invalid Configuration
**Objective**: Verify proper error handling for invalid YAML files.

**Steps**:
1. Place invalid_config.yaml in the app's files directory
2. Attempt to load the configuration
3. Observe the result

**Expected Result**:
- Loading should fail
- Should return a failure result with exception details
- Should not crash the application

---

### Test 5.5: Save and Load Round Trip
**Objective**: Verify that saving and loading preserves all data.

**Steps**:
1. Create a Config object with all fields set:
   ```
   settings.language = "ru"
   settings.theme = "dark"
   settings.interfaceShiftX = 25
   settings.interfaceShiftY = -5
   vehicle.fuelMode = "save"
   vehicle.driveMode = "individual"
   ```
2. Save the configuration to a file
3. Load the configuration back from the file
4. Compare original and loaded configurations

**Expected Result**:
- Save operation should succeed
- Load operation should succeed
- All fields should match exactly between original and loaded configs

---

### Test 5.6: All Drive Mode Values
**Objective**: Verify that all DriveMode enum values can be loaded.

**Steps**:
For each drive mode value ("eco", "comfort", "sport", "snow", "outing", "individual"):
1. Create a config file with the drive mode value
2. Load the configuration
3. Verify the enum is correctly parsed

**Expected Result**:
- All drive mode values should load successfully
- Each should map to the correct enum value

---

### Test 5.7: All Fuel Mode Values
**Objective**: Verify that all FuelMode enum values can be loaded.

**Steps**:
For each fuel mode value ("intellectual", "electric", "fuel", "save"):
1. Create a config file with the fuel mode value
2. Load the configuration
3. Verify the enum is correctly parsed

**Expected Result**:
- All fuel mode values should load successfully
- Each should map to the correct enum value

---

## Test Suite 6: YAML Conversion Tests

### Test 6.1: Config to YAML Conversion
**Objective**: Verify that Config objects are correctly converted to YAML format.

**Steps**:
1. Create a Config object with:
   - settings.language = "en"
   - settings.theme = "dark"
   - settings.interfaceShiftX = 10
   - vehicle.fuelMode = "intellectual"
   - vehicle.driveMode = "comfort"
2. Convert to YAML
3. Verify YAML structure and content

**Expected Result**:
YAML should contain:
- "settings:" section
- "language: en"
- "theme: dark"
- "interface-shift-x: 10"
- "vehicle:" section
- "fuel-mode: intellectual"
- "drive-mode: comfort"

---

### Test 6.2: Enum to YAML String Conversion
**Objective**: Verify that enum values are correctly converted to YAML strings.

**Steps**:
1. Create Config with various enum values:
   - settings.language = "ru"
   - settings.theme = "light"
   - vehicle.fuelMode = "electric"
   - vehicle.driveMode = "sport"
2. Convert to YAML
3. Verify string representations

**Expected Result**:
YAML should contain correct string values:
- "language: ru"
- "theme: light"
- "fuel-mode: electric"
- "drive-mode: sport"

---

### Test 6.3: All Enum Values to YAML
**Objective**: Verify that all enum values convert to correct YAML strings.

**Steps**:
Test each enum value individually:

**Language enum**:
- Language.en → "language: en"
- Language.ru → "language: ru"

**Theme enum**:
- Theme.auto → "theme: auto"
- Theme.light → "theme: light"
- Theme.dark → "theme: dark"

**FuelMode enum**:
- FuelMode.intellectual → "fuel-mode: intellectual"
- FuelMode.electric → "fuel-mode: electric"
- FuelMode.fuel → "fuel-mode: fuel"
- FuelMode.save → "fuel-mode: save"

**DriveMode enum**:
- DriveMode.eco → "drive-mode: eco"
- DriveMode.comfort → "drive-mode: comfort"
- DriveMode.sport → "drive-mode: sport"
- DriveMode.snow → "drive-mode: snow"
- DriveMode.outing → "drive-mode: outing"
- DriveMode.individual → "drive-mode: individual"

**Expected Result**:
- Each enum value should convert to its correct YAML string representation
- YAML structure should be valid

---

## Test Execution Guidelines

### Test Environment
- Use a clean Android environment for each test suite
- Ensure proper file permissions for configuration files
- Use temporary directories for file operations when possible

### Error Handling Verification
- Verify that all error conditions return proper Result objects
- Ensure no uncaught exceptions crash the application
- Check that error messages are descriptive and helpful

### Performance Considerations
- File watching tests may take several seconds to complete
- Allow adequate time for file system operations
- Monitor memory usage during file watching operations

### Test Data Management
- Clean up test files after each test
- Use unique file names to avoid conflicts
- Verify file permissions before testing

---

## Expected Test Results Summary

### All Tests Should Pass If:
1. **Basic Functionality**: ConfigManager initializes and handles errors gracefully
2. **Data Models**: Config objects handle partial and complete data correctly
3. **Diff Logic**: Change detection works accurately for all scenarios
4. **File Watching**: Real-time file monitoring triggers appropriate callbacks
5. **Integration**: YAML files load correctly with proper enum parsing
6. **Conversion**: Config objects serialize to valid YAML format

### Common Failure Points to Watch:
- File permission issues
- Enum value parsing errors
- File watching callback timing
- Memory leaks in file watching
- Invalid YAML structure handling
- Null value handling in configurations

This test plan ensures comprehensive coverage of all functionality provided by the Voboost Config library and validates that it works correctly in real-world scenarios.