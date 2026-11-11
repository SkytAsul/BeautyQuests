---
title: Stage
description: How to create a custom stage
---

Stages define what the questers actually have to do in order to advance in a quest.

Custom stages must extend the `AbstractStage` class. The newly created subclass is responsible for:
- Holding the _static_ stage configuration (e.g. which location to go to, which type of mobs to kill...)
- Listening to the appropriate events and triggering the `super.finishStage(quester)` method when appropriate.

## Stage registration

They can be registered using:
```java
QuestsAPI.getAPI().getStages().register(...)
```
TODO

## How to manage quester data
TODO
