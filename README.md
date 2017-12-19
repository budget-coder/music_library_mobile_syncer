# Music Library Mobile Syncer
A Java program for syncing your local music library to your Android or other mobile device.

## Usage: <br/>
Currently, there are two not-so-practical ways to launch the application:
- If you have [ant](http://ant.apache.org/) installed, you can run the command "ant gui" from the root directory (i.e. where you can see lib-core, src, test etc. This will compile all the classes and launch the GUI.
- Otherwise, you will have to go inside "src" and compile everything with the "javac" command. Then you launch "UI.class" without any arguments.

## Useful links: <br/>
Testing: http://joel-costigliola.github.io/assertj/assertj-core-quick-start.html <br/>
Tag info: https://en.wikipedia.org/wiki/ID3 <br/>
Tag mappings: https://picard.musicbrainz.org/docs/mappings/ <br/>

## To-Do List: <br/>
- ~~Add an ant and ivy script for auto-downloading the libraries.~~
- Add tests and actually have code coverage.
- Currently, any portable device NOT set to "USB Storage Mode" (or any other mode that assigns it a drive letter) will not be selectable as a directory.
- Make it possible to auto-detect Android device.
- Add javadoc to all methods...
- ~~Add interrupt checking.~~
- Make it work for nested folders
- BUG: if a music piece cannot be added to the dst, then it will think it succeeded when the program is opened next time. This is because the music's name and last modified date is added to MLMS_LastSession.txt regardless of the result. (This might also apply to unsuccessful deletions.)
- somehow handle music on mobile with different time zone than the PC (e.g. my music' modified date is 1 hour behind the same music on the PC). *SOLUTION:* Use hashes (fastest is best)!
- add auto-scroll ability
- Have computer stay awake when it is synchronizing