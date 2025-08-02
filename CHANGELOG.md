# VERSION 1.5.2

* Update zh_cn.json 1.21 - Mooncywin
* Create es_es.json and es_mx.json - ArrivedBog593
* Support fractional factors and base amounts - Matyrobbrt

# VERSION 1.5.1

* Fixed being able to override compacting drawers setup if the top slot was empty, closes #432 - Satherov
* Improved drawer interaction in claimed chunks  - Satherov

# VERSION 1.5.0

* Added ua_ua.json for Ukrainian localization - inceon
* Update Chinese localization #414 - ZHAY10086
* Added support for UP and DOWN orientations in drawer blocks
* Full numbers when viewing items / fluids inside the gui - Satherov
* Allow items to be placed in any compacting slot - Satherov

# VERSION 1.4.3

* Added item tags to fluid drawers, closes #411
* Add translatable strings - StarskyXIII
* Examine all destination slots - kylev
* Armory Cabinet providers for Jade and TOP - Christofmeg
* Fixed being able to extract upgrades when going over the possible limit closes #406 blame @Matyrobbrt
* Added extra checks for the reconnection of the Controller Extension, closes #395
* Fixed recipes that used drawers could use drawers with items, closes #331 closes #407

# VERSION 1.4.2
* Fix upgrades not being insertable/extractable in compacting drawers by Matyrobbrt

# VERSION 1.4.1
* Fixed drawers not saving storage upgrades when broken

# VERSION 1.4.0
* Reworked all the upgrades to use the component system so modpack makers can create custom upgrades by Matyrobbrt
* Added missing lang entry for simple compacting drawer, closes #343, #356 by kylev
* Allow compacting drawers to be crafted with any type of stone, closes #284 by kylev
* Fixed framed fluid drawers not having the locked indicator, closes #364 by kylev
* Fixed relative direction not showing in Fluid Drawers, closes #357
* Override getViewDistance() to get render decisions in constant time by kylev
* Model and textures changes to fluid drawers by kylev
* Chinese localization update by ZHAY10086
* Fixed void upgrade not working properly in Ender Drawers, closes #358
* Fixed locked fluid drawers not accepting fluid when being interacted on, closes #351

# VERSION 1.3.7
* Add upgrade stack to `FunctionalUpgradeItem#work` by Matyrobbrt
* Create ja_jp.json by suthibu

# VERSION 1.3.6
* Drawers now show up if they are creative or void in their tooltip
* Added nbt value to drawers to lock the storage upgrades slot
* Fixed a couple edge cases where the creative upgrade max value wasn't showing

# VERSION 1.3.5
* Increased controller range render a bit to avoid Z-Fighting
* Fixed Framing recipe changing the original value
* Added Fluid Framed Drawers

# VERSION 1.3.4
* fix: fixed #322 by making copyIndex variable
* improve zh_cn.json for 1.21
* Fixed Upgrades being accessible externally in a fluid drawer, closes #336
* Added obsidian generator upgrade, closes #334
* Fixed compacting recipes generating in the wrong path
* Added a tick for access points to update connections, closes #327
* Fixed NBT getting readded every time drawers get placed and broken, closes #330

# VERSION 1.3.3
* Added dripping upgrade that will fill fluid drawers with lava
* Added water source upgrade that will fill fluid drawers with water
* Fix rebuild voxel shape cost too much time, close #314
* fix: #310 remove instantiate of java.awt.Color

# VERSION 1.3.2
* Fix fluid drawers not being accessible through controllers, closes #301

# VERSION 1.3.1
* Fixed Oak Drawer recipe
* Added examples for how to craft framed drawers
* Fixed Armory Cabinet extracting more than expected
* Fixed interacting with drawers in creative

# VERSION 1.3.0
* Updated to 1.21

# VERSION 1.2.10

* Added drawer renders to items
* Added config option to modify the values of upgrades by ProGoofster
* Added the ability to disable fluid drawers renders, closes #281
* Added a minimum to the UPGRADE_TICK config
* Fixed drawers not pushing items if there was a slot not full with a different item, closes #276 
* Fixed empty locked fluid drawers in a controller network stalling fluid insertion, closes #258 closes #275
* Added Max Storage Upgrade closes #279

# VERSION 1.2.9
* Fixed creative vending upgrades using the base stack, closes #239

# VERSION 1.2.8
* Sort drawers insertion from closest to furthest to the drawer controller, closes #103

# VERSION 1.2.7
* Fixed cascading loading issues, closes #243

# VERSION 1.2.6
* Renamed Controller Extension to Controller Access Point
* Added a way to increase the Controller range by using upgrades
* Fixed Amethyst compacting recipe, closes #230 
* Added JEI compat for Custom Compacting Recipes

# VERSION 1.2.5
* Fixed ice compacting, closes #223

# VERSION: 1.2.4

* Changed the wood types list to a concurrent list, closes #214
* Improved compacting slot checking, closes #210
* Add timer for before next block removal to prevent double-clicking by ChampionAsh5357
* Check fluid stack isn't empty before setting amount by ChampionAsh5357
* Change texture directory from blocks to block by ChampionAsh5357

# VERSION: 1.2.3

* Properly added a changelog file
* Added config option for the upgrades to change how much they do and how often they do it, closes #204
* Cached controller voxel shape to avoid performance issues, closes #170
* Handle compacting drawer recipe checking only on the server, closes #159
* Added progress bar indicators to the drawers, closes #169
* Changed the downgrade upgrade to change the base value of a drawer to 64 so it can function with the other storage
  upgrades, closes #117
* Check if an upgrade can go inside a drawer to validate if proper storage numbers, closes #197
* Force the Creative Vending upgrade to go to the proper slot when right clicked into a drawer, closes #184
* Added item tooltip display to compacting drawers and a proper capability, closes #48
* Fixed Simple Compacting Drawers being breakable in creative when interacting with them, closes #187
* Prevent infinite loop when drawers check for their parent stuff, closes #73
* Now having the configuration tool in the offhand it will toggle the selected action when placing a drawer, closes #156
* Fixed pushing upgrade only pushing to the first slot, closes #164
* Added Framed Simple Compacting Drawer, closes #179
* Added custom compacting recipes, changed recipes that used the tag to use the custom recipe, closes #150
