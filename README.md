# Music Library Mobile Syncer
Music Library Mobile Syncer (MLMS, for short) A Java program for syncing your local music library to your Android or other mobile device.

## Usage <br/>
Currently, there are two not-so-practical ways to launch the application:
- If you have [ant](http://ant.apache.org/) installed, you can run the command "ant gui" from the root directory (i.e. where you can see lib-core, src, test etc.). This will compile all the classes and launch the GUI.
- Otherwise, you will have to go inside "src" and compile everything with the "javac" command. Then you launch "UI.class" without any arguments.

## Useful links <br/>
Testing: http://joel-costigliola.github.io/assertj/assertj-core-quick-start.html

Tag info: https://en.wikipedia.org/wiki/ID3

Tag mappings: https://picard.musicbrainz.org/docs/mappings/

## To-Do List <br/>
- ~~Add an ant and ivy script for auto-downloading the libraries.~~
- Add tests and actually have code coverage. (~30 %)
- Refactor internal methods back to private and test their public methods instead.
- ~~Currently, any portable device NOT set to "USB Storage Mode" (or any other mode that assigns it a drive letter) will not be selectable as a directory.~~
- ~~Make it possible to auto-detect Android device.~~ (auto-detect <b>will</b> not be implemented, but it can detect, and traverse, MTP devices)
- Add javadoc to all methods...
- ~~Add interrupt checking.~~
- The current library, jMTPe, doesn't <i>seem</i> to support anything other than .mp3 (i.e. .m4a files won't be recognized). Using WpdInfo tool to select an .m4a file reveals that <b>not all</b> the tags are shown anyways, leading me to believe that Microsoft's WPD API, one way or the other, doesn't support dissecting anything other than .mp3 audio files...
- Make it work for nested folders
- ~~somehow handle music on mobile with different time zone than the PC (e.g. my music' modified date is 1 hour behind the same music on the PC). *SOLUTION:* Use hashes (fastest is best)!~~
- ~~add auto-scroll ability~~
- ~~Have computer stay awake when it is synchronizing~~

    
## Known bugs <br/>
- If a music piece cannot be added to the dst, then it will think it succeeded when the program is opened next time. This is because the music's name and last modified date is added to MLMS_LastSession.txt regardless of the result. (This might also apply to unsuccessful deletions.)
- The library jmtpe has a class PortableDevice which represents MTP devices. Its implementation of the close() method (given by PortableDeviceImplWin32.class) does <b>NOT</b> work. The consequence is that once a device has been opened, it is not fully closed until program execution stops. 
- <b>Won't fix:</b>
    - Progress bar reaches 100 % faster than it should if the destination folder contains 1 or more non-music files. This is only an aesthetic bug; synchronization won't finish before all music files have been checked. (This is also seen by the fact that the start/stop button still says "Stop!").

## License <br/>
MLMS is primarily distributed under the terms of GPL-3.0.

See [LICENSE.md](LICENSE.md) for details.

### Third party software <br/>
This product includes software developed by Joel Costigliola, Pascal Schumacher (assertj) and Paul Taylor (jaudiotagger) among others.

In binary form, this product includes a revised version of [jmtpe](https://github.com/ultrah/jMTPe/) under the terms of GPL-3.0. You can find it in the [lib-core](lib-core/) folder under the name "[jmtpe.jar](lib-core/jmtpe.jar)".

See [LICENSE-THIRD-PARTY.md](LICENSE-THIRD-PARTY.md) for details.
