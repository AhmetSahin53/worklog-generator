worklog-generator
=================

Bu repo, IntelliJ/JetBrains tabanlı IDE içinde değişiklikleri (diff) analiz ederek Jira için Türkçe work log üreten bir eklenti/proje içerir.

Bu dosya, projeyi yerel Windows geliştirme ortamınızda çalıştırmak ve deterministic (tekrar üretilebilir) çıktılar elde etmek için gereksinimleri ve adımları açıklar.

Özet
-----
- IDE eklentisi: "JWL Work Log" (GenerateWorkLogAction) kullanılarak değişikliklerden otomatik work log üretilir.
- API: Groq/OpenAI uyumlu bir sohbet API'si kullanılıyor. API anahtarınızı ya Windows ortam değişkeni olarak (GROQ_API_KEY) ya da IDE içi PasswordSafe aracılığıyla kayıt ederek sağlayabilirsiniz.

Hızlı Kurulum (Windows)
------------------------
1) Java + IntelliJ ortamı
   - Java 17+ ve IntelliJ Platform SDK ile derleme için Gradle wrapper (projede var) kullanılır.
   - Windows üzerinde *cmd.exe* ile şu komutla derleme yapabilirsiniz:

```
gradlew.bat build
```

2) Groq API Anahtarı
   - Ortam değişkeni olarak ayarlamak (geçici terminal oturumu için):

```
setx GROQ_API_KEY "<SİZİN_API_ANAHTARINIZ>"
```

   - Veya eklenti içinde Settings > Tools > JWL Work Log (Groq) bölümünü kullanarak anahtarı IDE PasswordSafe içine kaydedin.

3) Eklentiyi yükleyin
   - Derleme sonrası ortaya çıkan `build/distributions/worklog-generator-0.0.1.zip` dosyasını IntelliJ'de "Plugins > Install plugin from disk..." ile yükleyin.

Deterministik (Tekrarlanabilir) Work Log için Öneriler
-----------------------------------------------------
Eklenti şu anda çıktı deterministikliğini artırmak için bazı önlemler alıyor (düşük temperature, top_p, sabit seed, sonradan doğrulama). Ancak hâlâ farklı çıktılar alıyorsanız aşağıdaki adımları deneyin:

1) Model seçiminde tutarlılık
   - `GenerateWorkLogAction.kt` içinde `model` alanı `llama-3.3-70b-versatile` olarak ayarlanmış.
   - Büyük/öğrenmeye açık modeller bazen nondeterministic davranabilir. Daha küçük, "deterministic" olarak bilinen veya daha kararlı modelleri test edin (örneğin Groq/OpenAI tarafında deterministik mod sağlayan bir model varsa onu kullanın).

2) Sampling parametreleri
   - `temperature`: 0.0 - 0.1 aralığına düşürün (0.0 daha deterministik).
   - `top_p`: 0.0 - 0.2 aralığına düşürün (0 daha deterministik).
   - `max_tokens`: çıktı uzunluğunu sınırlandırın (ör. 200).
   - Eğer API `seed` destekliyorsa sabit bir seed kullanın (ör. 42). Kod zaten `seed` kullanıyor ancak API sağlayıcınızın seed desteğini doğrulayın.

3) Sistem mesajını güçlendirin
   - `system` mesajını daha kesin yapın: "Sadece Türkçe, yalnızca 1-4 madde, her madde '-' ile başlamalı, başka hiçbir şey yazma." gibi net kurallar ekleyin.

4) Post-process ve katı doğrulama
   - Kodda zaten `looksNonTurkish` ve `enforceBulletFormat` fonksiyonları mevcut. Eğer hâlâ hata alıyorsanız `enforceBulletFormat`'u daha katı yapın: satır sayısı, her satırın Türkçe karakter içerip içermediği gibi kontroller ekleyin.

5) Çoklu denemeleri azaltma
   - Şu an iki istek atılıyor: ilk deneme, kurallara uymuyorsa daha sert bir retry. Bu tek seferlik davranışı koruyun ama model parametrelerini daha deterministik hale getirerek çoğu zaman ilk denemede doğru sonucu yakalayabilirsiniz.

6) Lokal şablon/kurallar kullanma (offline)
   - Eğer mümkünse basit kurallar ve diff özetleyicileriyle (dosya değişikliklerinden çıkarılabilecek deterministic kısa cümleler) tamamen local bir algoritma yazmayı düşünün — bu, harici model kaynaklı değişkenliği ortadan kaldırır.

Config (hangi dosyayı değiştirilecek)
--------------------------------------
- `src/main/kotlin/com/atmosware/worklog/GenerateWorkLogAction.kt` : model, temperature, top_p, max_tokens, sistem mesajı ve post-process değişiklikleri burada yapılabilir.

İlgili Ortam Değişkenleri
-------------------------
- GROQ_API_KEY: Groq/OpenAI API anahtarınız. Eklenti önce `System.getenv("GROQ_API_KEY")` sonra IDE PasswordSafe içindeki kaydı kontrol eder.

Gemini / Model Zip Placeholder
------------------------------
Eğer özel bir model paketleyip (ör. Gemini) eklentiye dahil etmek isterseniz, basit bir placeholder zip (`gemini-model.zip`) oluşturduk. Bu zip gerçek model verisi içermez; ancak README içinde zip'in nereye yerleştirileceğini ve nasıl kullanılacağını anlattık.

Dosyalar ekledik
----------------
- `README-LOCAL.md` — proje için Türkçe kurulum ve tavsiyeler (bu dosya şu an oluşturuldu).
- `tools\create_gemini_placeholder.bat` — gemini zip placeholder oluşturmak için Windows batch script.
- `models\gemini-model.zip` — boş placeholder (README ve küçük meta içerir).

Sonraki Adımlar
--------------
- İsterseniz doğrudan `GenerateWorkLogAction.kt` içinde önerilen parametre değişikliklerini uygulayıp denemeler yapabilirim. Bunun için onay verin; ben değişiklikleri yapıp derleme hatalarını kontrol edeceğim.
- Veya sadece README ve yardımcı script yeterliyse ek bir işlem yapmayacağım.


