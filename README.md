## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).

11/09/2026, 4:08pm
NOTES:

- Added strategy pattern for human movement, and several move options depending on if they see a cure, are fleeing from a zombie, are trying to cure a zombie, or are too far to be scared and instead wander.
- Added generics (find nearest for zombie and human, both entities use this for deciding where else to move)
- Added exceptions (WorldSetup throws exception when the free tile finder fails after a high number of attempts, GamePanel elevates the throw to the Main.Java)
- Corrected "zombie stacking" bug (checks for other zombies moving to desired tile before moving)
- Corrected the same bug but for humans. It was still happening (no check to ensure another entity was inhabiting that spot before moving to it)
- Corrected zombie counter no longer increases dramatically towards the end of the simulation
- Changed filetree, src and bin are properly implemented, settings.json now in .vscode folder.
- I still need to implement the strategy pattern for the zombies too.
- Refactoring required