# Trinea Android Common

## Overview
Trinea Android Common is a collection of cache components, UI widgets, and utility helpers that power thousands of Android apps. The project focuses on reproducible solutions for list pagination, image/network caching, download management, and day-to-day toolkit gaps so that small teams can ship stable products quickly.

## Modules at a Glance
- **Cache suite**: in-memory/disk image caches, HTTP cache, and preload data cache with FIFO/LIFO/LRU/etc. policies plus persistence helpers.
- **Reusable views**: pull-to-refresh & infinite scroll `ListView`, paged `Gallery`, responsive `ScrollView`, ad/banner carousel, and more.
- **Utility helpers**: `DownloadManagerPro`, shell/package/resource/file/json/string/collection utilities, silent install helpers, time/date helpers, and random utilities used across apps.
- **Dev Tools App**: [Dev Tools on Google Play](https://play.google.com/store/apps/details?id=cn.trinea.android.developertools) lets developers browse the latest OSS projects, inspect activities, decompile APKs, perform color pick, dump manifest info, and toggle developer options quickly.

## Getting Started
1. Import the library module or add the dependency:
   ```groovy
   implementation 'cn.trinea.android.common:trinea-android-common:4.2.15'
   ```
2. Proguard
    ``` xml
    -keep class cn.trinea.android.** { *; }
    -keepclassmembers class cn.trinea.android.** { *; }
    -dontwarn cn.trinea.android.**
    ```   
3. Review the [API Guide](https://trinea.github.io/doc/trinea_android_common/index.html) and [sample app](https://github.com/Trinea/AndroidDemo) for usage patterns.
4. When integrating as a library project, open *Project Properties → Android → Library* and add **TrineaAndroidCommon**.

## Maintenance & Roadmap
Although the codebase has been quiet recently, the components remain in active use across the community. We are preparing modernization work (AndroidX migration, refreshed sample app, expanded tests) and will leverage Claude/Codex for OSS to accelerate documentation, governance, and CI improvements. Community members can watch GitHub Issues/Discussions for upcoming milestones.

## License
Apache License 2.0 — see [LICENSE](./LICENSE).

## Contact & Community
- GitHub: [@Trinea](https://github.com/Trinea)
- Website: [codekk.com](https://codekk.com/)
- Email: [trinea.cn@gmail.com](mailto:trinea.cn@gmail.com)

We welcome issues and pull requests that help modernize the toolkit and keep long-lived apps healthy.
