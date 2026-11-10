# IGNOU MCA Tracker — Android

Native Android starter for MCA_NEW students. It retrieves notices from official IGNOU websites, classifies them into project, assignments, exams, results and registration categories, and schedules periodic Android notifications.

## Official sources included
- IGNOU announcements: https://www.ignou.ac.in/announcements/0?nav=6
- MCA_NEW assignments: https://webservices.ignou.ac.in/assignments/schools/socis/mca_new/mca_new.html
- IGNOU Regional Centre Kozhikode: https://rckozhikode.ignou.ac.in/

## Notifications
This build uses WorkManager to poll official sources approximately every 6 hours and issue local Android notifications when new links are detected. Android may defer periodic jobs due to battery optimisation. These are not Firebase server-push notifications. True FCM push requires a Firebase project, app registration and a deployed backend.

## Build APK
1. Install Android Studio and Android SDK Platform 35.
2. Open this folder in Android Studio and allow Gradle sync.
3. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. APK output: `app/build/outputs/apk/debug/app-debug.apk`.

The included GitHub Actions workflow can also build the APK after this source folder is pushed to a GitHub repository.

## Important
The announcement parser uses page links and keyword matching. IGNOU may change its website layout, and some general notices may or may not apply to MCA_NEW. Always open the original notice and verify its applicability and deadlines.
