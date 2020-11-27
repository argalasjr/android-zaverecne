# firebase-crashlytics

To enable debug logging on your development device, set an adb shell flag before running your app:


'''adb shell setprop log.tag.FirebaseCrashlytics DEBUG'''


To view the logs in your device logs, run:


'''adb logcat -s FirebaseCrashlytics'''


To disable debug logging, set the flag back to INFO:


'''adb shell setprop log.tag.FirebaseCrashlytics INFO'''




get certificate fingerprint


'''keytool -list -v -alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore'''

pass: android

