# Changelog

## UNRELEASED
- Updated localization files.
- Fixed wrong config translation

## 1.9.18 - 2026-06-10
- Fixed Stacked Photographs item not behaving correctly in Refined Storage or Create Stock Ticker. 

## 1.9.17 - 2026-06-08
- Fixed dedicated server crash with Crossroads.
- Potential fix related to larger anvil renaming limit of Interplanar Projector.

## 1.9.16 - 2026-04-02
- Attempt to fix weird issue with config "not being loaded".  

## 1.9.15 - 2026-03-25
- Images loaded with Interplanar Projector are now limited to maximum of 10000x10000.
- While playing on the multiplayer server - projecting images from URL will only work for allowed domains by default (configurable). This is a client-only setting, so players can change it at any time.
- Added a config option to allow projecting images from file only relatively to game directory (while on multiplayer server).

## 1.9.14 - 2026-03-04
- Added compatibility with **Easy Anvils** for Interplanar Projector extended renaming length.
- Fixed mod icon.

## 1.9.13 - 2025-12-30
- Capturing the image no longer sets the game to 'panoramic mode' by default.
  - Setting to enable it is added to config. Enable if world is not looking right in the photos.
  - This was done to fix conflict with other mods (specifically `Snapper`).
- Fixed viewfinder reversing mouse sensitivity at low FOV settings. 
- Updated localization files.

## 1.9.12 - 2025-10-30
- [NeoForge] Fixed crash with Create 6.0.7.

## 1.9.11 - 2025-09-26
- Camera on stand will now malfunction when attempting to take a shot with Interplanar Projector without an owner nearby.
  - Added `camera_stand.fallback_to_other_players_projector` config option original behavior is desired.
- Updated localization files.

## 1.9.10 - 2025-06-21
- Create Deployer can now install/swap attachments on Camera Stand.
- Fixed not being able to extract a Camera item from Refined Storage grids. 

## 1.9.9 - 2025-06-15
- Fixed github query hanging the game for long time when it's not available.

## 1.9.8 - 2025-06-07
- Added `ProtoManly's Weather` to the `force_direct_capture_default_mods` config option, to fix their sky not appearing in photos.
  - This change will not apply to existing configs, so you'll need to delete existing file to let it regenerate or add it manually. 
- Updated localization files.

## 1.9.7 - 2025-05-30
- Fixed rendering issue when Distance Horizons and Iris are both installed and shader is activated by force enabling direct capture when these mods are detected together.
- Fixed photographs dropped as item in creative mode from photograph screen not setting their type and thus require wrong dyes in copying recipe. 

## 1.9.6 - 2025-05-24
- Fixed small server-side error.
- Updated localization files.

## 1.9.5 - 2025-05-24
- Added 'hide_hud_while_in_viewfinder' and 'status_icon_offset_<x/y>' config options.

## 1.9.4 - 2025-05-23
- Background capture method now works with Iris, Oculus and Distant Horizons. (thanks Inconn)
  - Renamed config option 'force_direct_capture_mods' to 'force_direct_capture_default_mods' to reset this option to default (to remove now compatible mods in existing configs).
    - If you have added custom values to this config option, you'll need to re-add them.
    - Removed 'effective' from this config value and added 'veil', as it's the one that causes issues.
- Reworked how Camera model is created.
  - Resourcepacks that change the camera would probably need to be updated. Sorry.
- Readjusted selfie mode camera for holder POV and outside POV.
- Fixed wrong selfie camera model showing when in selfie mode and looking at yourself from outside (Camera Stand for example).
- Camera Stand tooltip no longer renders when HUD is hidden.

## 1.9.3 - 2025-05-21
- Added 'exposure:film_dither_mode' ("dithered" or "clean") component for films.
- Added 'photograph_frame_image_offset' that can be used to fix some issues with 3D resourcepacks.
- Fixed Camera model holding pose in thirdperson selfie mode. 
- Fixed photograph not rendering correctly in Item Frame (when 'photograph_renders_in_item_frame' is enabled).

## 1.9.2 - 2025-04-24
- [Fabric] Config screen can now be accessed when ModMenu is installed.
- Fixed missing Create spout developing recipes for high sensitivity films.
- Images loaded with Interplanar Projector are no longer cropped. 

## 1.9.1 - 2025-04-09
- Added `#exposure:black_and_white_film_rolls` and `#exposure:color_film_rolls` item tags.
- Fixed not being able to develop high-sensitivity film rolls.

## 1.9.0 - 2025-04-09
- Added Camera Stand.
- Added Self-Timer to the Camera.
  - Mobs will look at the camera when timer is ticking.
- Added High-Sensitivity Films.
  - Removed shutter speeds above 1 second.
- Added dedicated creative tab for Exposure items
- Camera model now changes depending on attachments.
- Added tutorial toasts to Attachments menu.
- Added film info to attachments menu.
- Added `pixel_perfect_photograph_frame` client config option. Resizes images in Photograph Frame to match 16px grid.
<br><br>

- Changed selfie stick texture.
- Changed `entities_in_frame` for `exposure:frame_exposed` advancement trigger and `exposure:frame` predicate. It is now a list instead of singular entity.
- `/exposure debug expose_rgb` now requires player to hold a Camera.
<br><br>

- Fixed projecting image from URL always timing-out.
- Fixed extra row of pixels in Lightroom screen and process toggle button position.
- Fixed flash breaking underwater vegetation.
- [NeoForge] Fixed Exposure's loot not generating in chests.

## 1.8.11 - 2025-03-16
- Fixed crash with some mods (Little Tiles is a known one) due to conflicting mixin methods.

## 1.8.10 - 2025-03-15
- Added `force_direct_capture_mods` config option. 
- Added `Distant Horizons` to the list of mods that force direct capture if installed.
- Fixed slashes (and other invalid chars) in player nicknames causing exposures to not save properly.

## 1.8.9 - 2025-03-09
- Made small change and added config option that can potentially help to fix occasional server hanging.

## 1.8.8 - 2025-03-05
- Fixed 16th page in Album losing content when album is opened.  

## 1.8.7 - 2025-03-05
- [NeoForge] Fixed Create Sequenced Assembly film developing not working with Create 6.0.1.

## 1.8.6 - 2025-03-02
- [NeoForge] Added **Create** `Sequenced Assembly` Film developing recipes. 

*Note:*
- If you want to change sequenced developing recipes - only `filling` steps can be used, otherwise Film data would be erased.

## 1.8.5 - 2025-02-28
- Added camera panning in selfie mode. Hold sprint key (ctrl) and move the mouse.
- Added ability to pan the camera when camera controls are showing. Just drag with left click.


- Photograph view screen will now update when new photographs are added to the item being viewed.
- Fixed count number not showing in Stacked Photographs tooltip.
- [Fabric] Fixed images showing only the sky with `Effective` installed.
- Added `/exposure debug highlight_entities_in_frame` command.
- Moved `waist_level_viewfinder` config option from client to server config.
- Fixed `/exposure palette convert png_to_json` producing wrong results in some cases. 
- Internal changes for addon compatibility.
- Added mexican spanish translation.

## 1.8.4 - 2025-02-21
- Added config option for Lightroom light level requirement.

## 1.8.3 - 2025-02-15
- Moved `different_developing_potions_colors` config option from client to common.
  - Fixes dedicated server crash when trying to read the config value. 
- Fixed log message appearing on each level save.

## 1.8.2 - 2025-02-14 
- Fixed shader shadows being weird when looking through Viewfinder.
- Fixed incompatibility with some zoom mods.

## 1.8.1 - 2025-02-13
- Fixed crash when hovering over a Photograph from loot chest.
- Fixed Film renaming menu not opening.

## 1.8.0 - 2025-02-13
- Added mipmaps to exposures. (thanks to _bravely-beep_ and _MapMipMapMod by Jalvaviel_)
- Added Glass Photograph Frame.
  - Frame would not be visible when it has Photograph.
  - Axe and Glass Pane can no longer be used on regular Photograph Frames.
- Added animation for Camera interactions (changing viewfinder settings and attachments) - player will move their hand slightly.

Capturing:
- Added new capture method - background. Captures an image, you guessed it, in background, without rendering it on the screen.
  - Screen no longer flickers from hiding the HUD when taking a photo.
  - Direct method (old) still exists, and will be used if Iris is installed (due to technical limitation).
  - Direct method can also be toggled in config, in case you experience issues with background method. 
- Filter post effects are now processed separately from vanilla post effects.
  - Vanilla effects will not be rendered when image is captured, unless turned on in a config.

Color Palette:
- Exposure no longer uses vanilla MapColors directly. Color palettes are now data-driven. 
- Default color palette still uses all vanilla map colors, but with few additions, mainly brighter colors. Long exposure photos now look slightly better.
- Added ability to create custom palettes.

Interplanar Projector:
- Can now load images from URL.
- When renaming it in Anvil, name length limit has been increased to 150 characters (from vanilla 50), to allow for longer paths.
- Projector now breaks when loading has failed instead of being consumed. 

Lightroom:
- Printing now requires light level greater than 12 at position above the Lightroom.
- No longer emits light.
- Chromatic Printing now requires Tinted Glass placed on top of the Lightroom.

Advancements:
- Added Splitting the Photon advancement for exposing a frame with R/G/B filter.
- **Moment in Time** and **Complex Composite Compound** are now granted when printing a photo, instead of just obtaining it.
- Added `exposure:frame_printed` trigger. Uses player that last interacted with Lightroom, or closest if last is unavailable.
- Predicates added by the mod and `exposure:frame_exposed` trigger have been changed.

Misc:
- Camera interaction sounds are now audible for other players.
- Reduced color photograph print time from 10 to 8 seconds.
- Closing Camera Attachments menu will now go back to inventory menu if it was opened from it (by right-clicking).
- Mundane, Awkward and Thick potions now have different colors to tell them apart easier.
- Film Roll renaming screen now has a sound when item is renamed and Enter key can be used to quickly apply changes.
- Photographs are no longer rendered as images in Item Frames by default. Can be turned back on in the config.
- Aged Photograph recipe now requires a Brush.
- You can now select specific Album page in a Lectern by clicking on page number. Allows controlling Lectern's Comparator output properly.
- Changed how custom Lenses and Filters are defined.
- Added `/exposure debug develop_film_in_hand` command.
- Added `/exposure debug expose_rgb` command.
- Added `/exposure debug chromatic_from_last_three_exposures` command.

Config:
- Most of the 'common' settings has been moved to 'server' config.
- Added server config option to change default exposure size.
- Added server config option to disable loading images with Interplanar Projector.
- Added server config option for configuring which dyes will be consumed when printing. 
- Added client config option to shift view to where camera actually is when held at waist-level.

## 1.7.6 - 2024-08-17
- Fixed Album disappearing from Lectern when Amendments is installed.
- Added es_es lang.

## 1.7.5 - 2024-07-25
- Fixed Camera resetting zoom when attachments change.
- Minor adjustments to unlocking recipes in a recipe book
  - Fixed Camera recipe being unlocked from the start
- Fixed crash trying to render a Photograph without NBT-tag. 

## 1.7.4 - 2024-07-20
- Fixed negatives in Lightroom rendering with larger size than they should.  

## 1.7.3 - 2024-07-19
- Fixed `export` command not working/crashing on a dedicated server.
- Added config options to hide projected/all photographs made by other players.
- [Real Camera compat] Player model will not be rendered while looking through viewfinder. 

## 1.7.2 - 2024-07-04
- Fixed dedicated server crash when chromatic photograph is printed.
- [Forge] Fixed Camera recipe change from 1.7.0 not being applied. 
- Viewfinder zooming animation is now slightly faster.
- Photograph Frame and Interplanar Projector recipes now added to recipe book if player has required items. 

## 1.7.1 - 2024-06-28
- Fixed Interplanar Projector not working when Supplementaries is installed.

## 1.7.0 - 2024-06-28
- Added **Photograph Frames**
  - **Photographs** can no longer be hanged on the wall. Existing hanging photographs will not be removed.
- Added **Film Roll renaming**. Use the item to open renaming UI. Renaming this way does not cost any experience.
- Added **Interplanar Projector** filter. Allows loading custom images by renaming the item to filepath. 
- Updated item textures.
- Added several photographs that generate in loot chests.
- Added several new advancements
  - Existing advancements have been modified internally, players will have to complete them again.
  - Internal changes have been made to exposure:frame_exposed trigger.

<br>

#### Updated Camera Attachments UI:
- You can now hover over camera components to see tooltip with information about it.
- All available **Lenses** and **Filters** can be viewed by clicking on the lens and filter respectively.
- Added ability to render custom Filter texture in Camera Attachments UI `//WIKI`

<br>
  
- **Lightroom** comparator output now based on the number of Photographs in the output slot rather than the selected frame.
- **Chromatic Process** can now be enabled for all black and white images by placing an Amethyst Cluster on top of a Lightroom.
- **Color Film** recipe now requires Gold ingots and nuggets instead of Iron.
- **Camera** recipe now uses only Iron Ingots instead of Iron Nuggets and Pressure Plate. 
- **Film** developing recipe will now also accept potions defined without `minecraft:` namespace - i.e. `{Potion:thick}`.
- Viewfinder will now show an indicator for last three frames to remind you that the Film Roll is about to end.
- Slightly reduced brightness increase per shutter speed.
- Improved player arms positions in selfie pose.
- Fixed player selfie pose sometimes not applying when activating/deactivating Camera rapidly.  
- Loaded exposures now support images with transparency, but only when the pixel is fully transparent.

## 1.6.0 - 2024-05-11

- Middle mouse button can now be used to open Camera controls.
  - Allows opening controls without dismounting from a horse. Or jumping off a plane mid-flight. (Without rebinding sneak) 
  - This is independent of the main hotkey, and will work alongside it. Can be disabled in config.
- Right-clicking on the Camera in inventory will open Camera configuration screen.
  - Right-click with item install/swap an attachment.

- Added KubeJS integration - [Wiki](https://github.com/mortuusars/Exposure/wiki/KubeJS-Integration) 
- Added three java events (same as in KubeJS) for addon developers. 
- Added Jade integration to Lightroom: it now shows printing progress arrow. 
- Added config option to disable attacking while looking through Viewfinder.
- Added config option to delay capture (useful when GUI elements are not hidden fully, which happens with some shaders)
- Stacking two Photographs (by right-clicking in GUI) will keep the resulted item in slot instead of picking it up.
- Camera tooltip now shows exposed/available frames of the inserted Film Roll.
- Developed rolls now show frame size (if custom) in tooltip, same as undeveloped rolls.
- Adjusted 'No Film' icon position with different GUI scales.
- Adjusted Viewfinder 'catching-up' movement for different GUI scales.
- Photograph paper texture is now randomly rotated to have more variation.  
- Slightly modified Aged Photograph texture to have less rounded corners.
- GUI titles now have a separate lang entries instead of using corresponding block/item entries. 

- Fixed custom Lenses not syncing to the player when they join a server.
- [Fabric] Fixed Sequenced Film Developing (with Create) recipes not showing in EMI

## 1.5.1 - 2024-04-06

- Added advancement for getting a Photograph create with Chromatic process.
- Fixed Chromatic Photograph not displaying correctly when trying to render it too quickly after printing (Usually when mouse was hovering over result slot).
- Updated one localization file.

## 1.5.0 - 2024-04-04
- Added chromatic (trichrome) printing process
- Red, Green and Blue filters now have stronger effect to enable chromatic process


- Lenses configuration is now data-driven. [Custom Lenses Wiki](https://github.com/mortuusars/Exposure/wiki/Additional-Information#custom-lenses-)
  - LensFocalRanges config setting has been removed.
- Changed how custom filters are configured, allowing the use of shaders from vanilla or other mods. [Custom Filters Wiki](https://github.com/mortuusars/Exposure/wiki/Additional-Information#custom-filters-%EF%B8%8F)


Exporting:
- Aged Photographs can be exported now (same as regular - when screen is opened). PNG will have `_aged` suffix.  
- Exported PNGs are now X2 the size. Configurable in client config. 
- Exported PNG files will have their `Date Created` attribute set to time when they were taken. Only for exposures made on this version an up.  


Commands:
- Renamed literal `exposure` to `id` in `/exposure show` command.
- Added two optional arguments to `/exposure export` command:
  - `size` - Defaults to "X1". (X1/X2/X3/X4)
  - `look` - Defaults to "regular". (regular/aged/negative/negative_film)
- `id` argument (in export and show commands) will show all available exposure IDs as autocomplete suggestions.
- `path` argument (in `/exposure show texture` command) will show a list of all available textures as autocomplete suggestions.


Lightroom:
- When in creative mode, you can now hold [Shift] to print exposure instantly and without dyes/paper   
- Experience points, granted for printing an image, are now different per process - bw/color/chromatic. Config now has three options instead of one to configure this.


- Fixed crash when rendering a non-square texture as a Photograph.
- [Forge] Fixed crash when clicking on exposure in mods menu. For real this time.  
- [Fabric] Fixed camera recipe not unlocking in recipe book when iron ingot is obtained.

## 1.4.1 - 2024-03-15
- Fixed Lenses config resetting and not working properly.
- Fixed issue with `Cold Sweat` when closing viewfinder with filter installed causing blur shader to apply when it shouldn't.
- Fixed `FTB Teams`, `REI` and `REI Plugin Compat` causing the game to freeze indefinitely when Album is opened.  


- [Fabric] Fixed Create's Spout crashing with latest `Create Fabric`.
- There will be no crash anymore if the `Create` version is not supported. Spout Film Developing will not work for incompatible versions instead. 
- Create 0.5.1f is needed for Spout Film Developing to work. 

## 1.4.0 - 2024-03-02
- Added Aged Photographs. Created by crafting a Photograph with brown dye
- Stacked Photographs max stack size can now be changed in a config


- Lowered required Fabric API version to 0.88.1+
- Updated localization files 


- Fixed incorrect position of a Photograph in Quark's Glass Item Frame
- Fixed crash when opening Album that's placed in a Lectern
- Fixed crash when clicking on Exposure entry in Mods menu

## 1.3.1 - 2024-02-09
- Added advancement for taking a selfie
- Slightly changed order of advancements
- Entities in frame (in photograph NBT) will now work correctly for selfies 
- Black and white photograph copying recipe now correctly uses only black dye instead of all four colors
- Changed Film Frame Exposed advancement trigger from `minecraft:frame_exposed` to `exposure:frame_exposed`
- [Fabric] Proper message will now show when `Fabric API` or `Forge Config Api Port` is not installed 
- [Fabric] Fixed crash when signing album

## 1.3.0 - 2024-02-04
- Added Photo Album 
  - Can store up to 16 photographs and some notes
  - Can be placed on Chiseled Bookshelves and Lecterns


- Changed photograph paper texture to not be so rough at the edges
- Holding use when opening Viewfinder will no longer cause Camera to shoot immediately after opening
- Camera controls key can be remapped now. Unbound means it will use sneak key, as before. Unbound by default.   


- Fixed non-english characters in player's nickname causing a crash when trying to render their photos 
- Fixed Lightroom not resetting selected frame back to 0 when film is ejected and another is inserted
- Fixed Lightroom not dropping due to the missing loot-table
- [Forge] Fixed item frame also rendering an item over the photograph 

## 1.2.2 - 2024-01-01
- Added `/exposure export` command. Allows exporting exposures to PNGs to `<world>/exposures` folder. Requires OP privileges.
- Added some **creative-mode** tools to Photograph screen:
  - **Ctrl+S** to save as PNG
  - **Ctrl+C** to copy exposure id to clipboard
  - **Ctrl+P** to give yourself current photograph in item form

- Fixed being able to copy `Copy of a copy` photograph.
- Fixed fov related issue that was causing problems with zoom mods.
- Fixed Lightroom Screen film bugging out when replacing Developed Film in slot with a film that has fewer frames than currently selected frame index.   
- Made some changes that may fix the crash with C2ME.

## 1.2.1 - 2023-12-24
- Fixed water not rendering properly with shaders when looking through Viewfinder.

## 1.2.0 - 2023-12-22
- Added `exposure:flashes` and `exposure:lenses` tags, allowing customization of items that can be attached to the camera. 
- Added Focal Length config options, allowing to configure default camera range and range per specific lens.

#### Changed Developing Recipe:
- Developing recipes will no longer show in vanilla Recipe Book due to the book not working well with this type of recipes (like with cloning written books or dyeing armor)   
- Developing can now be done with Create's Spouts. Configurable.
- Films no longer can be developed with Create's Mechanical Crafter

#### Misc: 
- Added zh_cn localization provided by 'IwasConfused'
- When JEI is not present, tooltips will be shown describing Developing and Photograph Copying recipes.
- Items can now be extracted from any side of a Lightroom block. And inserted through all but bottom side.
- Fixed Camera Attachments menu not opening from offhand.
- Pressing Inventory Key or Esc in thirdperson-back camera mode (when viewfinder is not visible) will now deactivate camera instead of opening inventory or pause menu.
- Slightly reduced z-fighting of the Hanging Photograph at greater distances.

## 1.1.1 - 2023-12-08
- Fixed third-person camera distance being closer when not looking through the Viewfinder.

## 1.1.0 - 2023-12-07
- Added selfies. Press F5 (by default) to be the star of the show. 

- Fixed Viewfinder Controls not showing up when sneak is bound to one of the mouse buttons.
- Fixed errors in log about developing and cloning recipes

## 1.0.2 - 2023-12-03
- Fixed Hanging Photograph not dropping when the block it's attached to is broken.
- Fixed crash when trying to add more than 16 photographs to the Stacked Photographs item.

## 1.0.1 - 2023-12-01
- Film developing recipe has been removed from Create's Automated Shapeless Crafting due to Mixer's whisk being too rough on the film, clearing any exposed images.   
