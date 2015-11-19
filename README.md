# Game Manager API Codelab Sample

This is a Android sample game using the [Google Cast Game Manager API](https://developers.google.com/cast/docs/gaming). The Google Cast Game Manager allows native Android, iOS, and Chrome games to connect with a Google Cast device and have communication and synchronization of player and game states across devices.

## Dependencies

### Android

* google-play-services_lib library from Android SDK (at least version 7.5.71)
* android-support-v7-appcompat (version 21 or above)
* android-support-v7-mediarouter (version 20 or above)

### Receiver

* Check out code from GitHub and go to the sample folder under `receiver` with index.html.
* Host the receiver files on a web server.
* Create a new app ID for your receiver sample using the Google Cast SDK Developer Console: https://cast.google.com/publish/
* Update app IDs in the strings.xml resource file.

## Setup Instructions

* Check out code from GitHub and go to a sample subfolder under `game-done` with a `build.gradle` file.
* In Android Studio, select `Open Existing Android Studio Project` and select the `build.gradle` file.
* You may need to provide the path to your local gradle installation. You can select the gradle binary inside your Android Studio installation directory.
* You can now run the app normally from within Android Studio, or use `gradlew build` from the command line.

If you prefer to use your local gradle installation to generate the gradle wrapper, type`gradle wrapper` from the project directory.

## References and How to report bugs

* [Cast APIs](http://developers.google.com/cast/)
* [Game Manager API](https://developers.google.com/cast/docs/gaming)
* [Design Checklist](http://developers.google.com/cast/docs/design_checklist)
* [Cast Game UX
  Guidelines (PDF)](https://developers.google.com/cast/downloads/GoogleCastGameUXguidelinesV0.9.pdf)
* If you find any issues, please open a bug here on GitHub.

## How to make contributions?

Please read and follow the steps in the CONTRIBUTING.md

## License

See LICENSE

## Google+

Google Cast Developers Community on Google+ [http://goo.gl/TPLDxj](http://goo.gl/TPLDxj)