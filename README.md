# moregears

item schmoovement

## funnels

items that land on their input face (square) are inserted into the container at their output face (circle)

when powered they become closed and no longer insert items

funnels can chain into other funnels

they dont have an inventory so they are piston movable

if theres no container to insert into then nothing happens

top-down funnels can access items on conveyors above them

## siphons

when powered they extract a stack from the container at their input end and insert into the container at the output end, or spits it on the ground like a dropper

when spitting into a conveyor the item is created neatly on top of it

they can chain into funnels as well or into powered siphons

they dont extract continuously only when going from unpowered -> powered

# compatible containers

works on

- chests
- double chests
- crates
- furnace/blast furnace/refridgifreezer
- incinerator
- drawers

# config options

| option                  | description                                                                         | 
|-------------------------|-------------------------------------------------------------------------------------|
| Enable Funnels          | enable/disable funnels and siphons                                                  |
| Funnel Insert Limit     | limits how many items at a time funnels can insert (per collision)                  |
| Siphon Extract Limit    | limits how many items siphons extract when powered                                  |
| Funnel Chain Limit      | funnels can chain up to this many extra funnels/powered siphons, 0 for no chaining  |
| Unpowered Siphons Chain | allow unpowered siphons to chain                                                    |
| Min Drawer Count        | siphons will leave at least this many items in a drawer (filters!)                  |

items are sucked into funnels pretty fast but the insert limit should be equal to or higher than the extract limit to avoid buildup

# pictures

example autosmelter

[autosmelter](img/autosmelter.png)

[autosmelter top](img/autosmeltertop.png)