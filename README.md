# حاسبة جرعة الأطفال (Android)

تطبيق أندرويد بسيط يغلّف صفحة HTML تعمل دون إنترنت: حساب جرعة الباراسيتامول والإيبوبروفين للأطفال،
مع سجل للجرعات ودرجة الحرارة ورسم بياني. البيانات تُحفظ على الجهاز فقط.

> أداة مساعدة فقط ولا تغني عن الطبيب أو الصيدلاني.

## الملفات المهمة
- `app/src/main/assets/index.html` : الصفحة نفسها (عدّل هنا الحسابات والواجهة)
- `app/src/main/java/com/example/dosecalc/MainActivity.kt` : WebView يفتح الصفحة
- `.github/workflows/android.yml` : يبني ملف APK تلقائيًا على GitHub

## الحصول على APK
1. ارفع المشروع إلى GitHub (فرع `main`).
2. من تبويب **Actions** افتح آخر تشغيل ونزّل `dosecalc-debug-apk`.
3. أو أنشئ وسم إصدار (`git tag v1.0 && git push origin v1.0`) فيُرفق APK في **Releases**.

## البناء محليًا
افتح المجلد في Android Studio، أو من الطرفية بعد تثبيت Gradle 8.9:

    gradle assembleDebug

الناتج: `app/build/outputs/apk/debug/app-debug.apk`
