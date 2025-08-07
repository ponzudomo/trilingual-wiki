# Language Settings Menu Access Fix

## Issue #17: Language settings not accessible

### Problem
Users reported that they could not see any way to access the language settings UI. The settings functionality was fully implemented but not accessible through the user interface.

### Root Cause Analysis
1. **Main Issue**: The menu item in `main_menu.xml` was configured with `android:showAsAction="ifRoom"`, which means it would only appear if there was sufficient space on the action bar. On devices with smaller screens or longer app titles, this would result in the menu item being hidden.

2. **Secondary Issue**: The landscape/wide layout (`layout-w600dp/activity_main.xml`) was missing the toolbar component entirely, so users on tablets or in landscape mode would have no way to access any menu items.

### Solution
1. **Menu Visibility Fix**: Changed the menu configuration to force the settings item to always be visible:
   ```xml
   <!-- Before -->
   <item android:showAsAction="ifRoom" />
   
   <!-- After -->
   <item android:showAsAction="always" app:showAsAction="always" />
   ```

2. **Layout Consistency Fix**: Added the missing toolbar to the wide layout to ensure menu access across all screen sizes and orientations.

### Files Modified
- `app/src/main/res/menu/main_menu.xml`: Updated showAsAction attribute
- `app/src/main/res/layout-w600dp/activity_main.xml`: Added missing toolbar component

### Manual Verification Steps
1. Build and install the app
2. Check that a settings/preferences icon appears in the top-right of the action bar
3. Tap the settings icon and verify the language settings dialog opens
4. Rotate device to landscape/use tablet and verify settings icon is still accessible
5. Test on different screen sizes to ensure consistent behavior

### Expected Behavior After Fix
- Settings icon (gear/preferences icon) should always be visible in the action bar
- Tapping the settings icon should open the language selection dialog
- This should work consistently across all device orientations and screen sizes
- Users can now select their preferred 3 languages for the trilingual Wikipedia viewer

### Code Flow
1. User taps settings icon in action bar
2. `MainActivity.onOptionsItemSelected()` is called with `R.id.action_settings`
3. `showLanguageSettingsDialog()` method is invoked
4. `LanguageSettingsDialog` is displayed with current language configuration
5. User can select/modify their language preferences
6. Changes are saved via `LanguageManager` and UI is updated