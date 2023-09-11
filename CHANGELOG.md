# VERSION: DEV

* Properly added a changelog file
* Added config option for the upgrades to change how much they do and how often they do it, closes #204
* Cached controller voxel shape to avoid performance issues, closes #170
* Handle compacting drawer recipe checking only on the server, closes #159
* Added progress bar indicators to the drawers, closes #169
* Changed the downgrade upgrade to change the base value of a drawer to 64 so it can function with the other storage
  upgrades, closes #117