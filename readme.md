# Productive Bees

Here bee puns

### Modifying Productive Bees
Most aspects of Productive Bees can be modified with a data pack.

#### Adding or removing bees

The first thing to do is to create a directory called productivebees on the same level as recipes and tags. In that folder you will put the json files for your bees. The filename will be the name of the bee clay.json becomes Clay Bee and green_grass.json becomes Green Grass Bee. The internal name you will later use in recipes will be productivebees:clay and productivebees:green_grass. There are not many fields in the json yet, but you must include the primary (head) and secondary (abdomen) colors. You can use conditions just like in recipes.
See this example `productivebees/productivebees/ender_biotite.json`
There are two other fields description which is the description shown in JEI and flowerTag which is the name of the block tag this bee will use as flowers. The tag name would be something like minecraft:flowers or forge:storage_blocks/lead. It defaults to minecraft:flowers if nothing is defined.

Next you can make breeding recipes or item conversion recipes, depending on how you want to obtain the bee. The location for the breeding recipe is recipes/bee_breeding and for conversion recipes/bee_conversion. An example of breeding `productivebees/recipes/bee_breeding/constantan_bee.json` and conversion `productivebees/recipes/bee_conversion/manasteel_bee.json`

You will then need to make a recipe for the bees production when inside a hive, the recipe is located in recipes/bee_produce. You will most likely just follow this format `productivebees/recipes/bee_produce/constantan_bee.json`
The comb output has to be `productivebees:configurable_honeycomb` and the bee_type need to be the internal name of your bee.
item_output is the item produced if combs are disabled in the config.

Last you need a centrifuge recipe for your comb in recipes/centrifuge. For metals I usually make two recipes, one for the ingot and one for the dust (if any). The ingot recipe is disabled if a dust exist.
`productivebees/recipes/centrifuge/honeycomb_constantan.json`, `productivebees/recipes/centrifuge/honeycomb_constantan_dust.json`
The ingredient has to be productivebees:configurable_honeycomb and comb_type is the internal name of your bee.

Recipe for making comb blocks is done automatically.

Existing bees implemented using the data pack system can be removed by overriding their json file. Existing bees can be found in
 `productivebees/productivebees`
To disable a bee, copy the json file to your data pack and change the conditions to something that evaluates to false.

#### Changing the comb block recipes

By default it takes 4 honeycombs to make a comb block. To change the count you need to override recipes in `productivebees/recipes/comb_block`. `configurable_comb_block.json` and `configurable_honeycomb.json` has a count property which is the one used for calculating the data pack bees comb recipes.

#### Changing solitary bee spawns 

Recipes in `productivebees/recipes/bee_spawning` can be overridden to change which bees spawn from which nest. The repopulation cooldown can also be defined here.