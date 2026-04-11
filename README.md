<!-- Plugin description -->
Atmosware Innovation Challenge 2026 kapsamında geliştirilmiş, yazılım geliştirme yaşam döngüsünü (SDLC) hızlandıran yapay zeka destekli IntelliJ IDE eklentisi.

JWL AI Work Log Generator, geliştiricilerin commit sırasında yaptığı değişiklikleri analiz ederek otomatik, kısa ve profesyonel work log üretir. Groq API ve Llama 3.3 (70B) modeli ile yüksek hızlı ve akıllı özetleme sağlar.

Commit ekranına entegre çalışır ve tek tıkla Jira/Azure DevOps için hazır açıklama üretir.
<!-- Plugin description end -->
# 🚀 JWL AI Work Log Generator (IntelliJ Plugin)

**Atmosware Innovation Challenge 2026** kapsamında geliştirilmiş, yazılım geliştirme yaşam döngüsünü (SDLC) hızlandıran yapay zeka destekli IntelliJ IDE eklentisi.
## 💡 Problem ve Çözüm
Geliştiriciler gün içinde yazdıkları kodlar için Jira veya Azure DevOps gibi platformlara "Work Log" (İş Özeti) girmek zorundadır. Bu işlem genellikle zaman alan, sıkıcı ve teknik detayların kaybolabildiği bir süreçtir.

**JWL AI Work Log Generator**, IDE'nin çekirdeğine entegre olarak geliştiricinin yaptığı commit değişikliklerini (diff) anında yakalar. **Groq API** ve **Llama 3.3 (70B)** modelinin gücünü kullanarak saniyeler içinde kısa, öz ve profesyonel bir iş özeti üretir.

## ✨ Öne Çıkan Yeni Özellikler
* **Kurumsal Güvenlik (PasswordSafe):** API anahtarları kod içerisine gömülmez. IntelliJ'in yerleşik `PasswordSafe` altyapısı kullanılarak IDE ayarlarında şifrelenmiş olarak güvenle saklanır.
* **Deterministik ve Akıllı Analiz:** Özel prompt mühendisliği, düşük `temperature` ve sabit `seed` ayarları sayesinde her seferinde kurumsal standartlara uygun, tutarlı ve %100 Türkçe çıktılar üretilir.
* **Gelişmiş Hata Yönetimi:** Dil modelinin belirlenen formattan (maddeleme) çıkması durumunda devreye giren otomatik doğrulama (post-process) ve yeniden deneme (retry) mekanizmaları ile stabilite garanti altına alınmıştır.
* **Görünmez Entegrasyon:** IntelliJ'in yerleşik Commit arayüzüne eklenen şık ve fütüristik özel buton ile pürüzsüz bir UX (Kullanıcı Deneyimi) sunulur.

## 🛠️ Kullanılan Teknolojiler
* **Dil & Platform:** Kotlin, IntelliJ Platform Plugin SDK
* **Yapay Zeka:** Groq API (Llama-3.3-70b-versatile)
* **Bağımlılıklar & Mimari:** Gson (JSON Parsing), Java 17 HttpClient, IntelliJ CredentialStore API

## 🚀 Kurulum ve Kullanım (Jüriler İçin)

Projenin test edilebilmesi için kurulum süreci son derece basitleştirilmiştir:

1. Bu repositordaki güncel `JWL_Plugin.zip` dosyasını indirin. (Eğer kaynak koddan derlediyseniz `build/distributions/` klasöründen alabilirsiniz).
2. IntelliJ IDE'nizde `Settings > Plugins > ⚙️ (Dişli İkonu) > Install Plugin from Disk...` yolunu izleyip zip dosyasını seçin ve IDE'yi yeniden başlatın.
3. IDE açıldıktan sonra **`Settings > Tools > JWL Work Log`** menüsüne gidin.
4. Groq API anahtarınızı ilgili alana yapıştırıp kaydedin. (Eklenti kullanıma hazırdır!)

## 🎯 Nasıl Çalışır?
1. Commit penceresinde değişiklik yaptığınız dosyaları seçin.
2. Alt kısımdaki parlayan mavi **"JWL Log Üret"** butonuna tıklayın.
