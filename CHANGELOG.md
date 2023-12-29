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