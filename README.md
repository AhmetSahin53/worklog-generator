# 🚀 JWL AI Work Log Generator (IntelliJ Plugin)

**Atmosware Innovation Challenge 2026** kapsamında geliştirilmiş, yazılım geliştirme yaşam döngüsünü (SDLC) hızlandıran yapay zeka destekli IntelliJ IDE eklentisi.

## 💡 Problem ve Çözüm
Geliştiriciler gün içinde yazdıkları kodlar için Jira veya Azure DevOps gibi platformlara "Work Log" (İş Özeti) girmek zorundadır. Bu işlem genellikle zaman alan, sıkıcı ve teknik detayların kaybolabildiği bir süreçtir. 

**JWL AI Work Log Generator**, IDE'nin çekirdeğine entegre olarak geliştiricinin yaptığı commit değişikliklerini anında yakalar. **Groq API** ve **Llama 3.3 (70B)** modelinin gücünü kullanarak saniyeler içinde kısa, öz ve profesyonel bir iş özeti üretir.

## ✨ Öne Çıkan Özellikler
* **Görünmez Entegrasyon:** IntelliJ'in yerleşik Commit arayüzüne eklenen şık, fütüristik **JWL** logolu özel buton.
* **Akıllı Kod Analizi:** Değişen Java dosyalarının eski ve yeni versiyonlarını okuyarak anlamsal bir "diff" analizi yapar.
* **Yüksek Hız:** Groq altyapısı sayesinde devasa değişiklikler bile saniyeler içinde analiz edilir.
* **Özel UX Tasarımı:** Üretilen metin, doğrudan panoya (clipboard) kopyalama imkanı sunan şık bir pop-up pencere ile gösterilir.

## 🛠️ Kullanılan Teknolojiler
* **Dil:** Kotlin
* **Platform:** IntelliJ Platform Plugin SDK
* **Yapay Zeka:** Groq API (Llama-3.3-70b-versatile)
* **Bağımlılıklar:** Gson (JSON Parsing), Java 17 HttpClient

## 🚀 Kurulum ve Kullanım

### Jüriler İçin (Hızlı Kurulum)
1. `build/distributions/` klasöründeki `.zip` dosyasını indirin.
2. IntelliJ IDE'nizde `Settings > Plugins > ⚙️ (Dişli İkonu) > Install Plugin from Disk...` yolunu izleyin.
3. İndirdiğiniz `.zip` dosyasını seçin ve IDE'yi yeniden başlatın.

### Geliştiriciler İçin (Kaynaktan Derleme)
1. Repoyu klonlayın.
2. `src/main/kotlin/.../GenerateWorkLogAction.kt` dosyasını açın.
3. `apiKey` değişkenine kendi [Groq API](https://console.groq.com/) anahtarınızı girin.
4. Gradle üzerinden `runIde` komutunu çalıştırarak sandbox ortamında test edin.

## 🎯 Nasıl Çalışır?
1. Commit penceresinde değişiklik yaptığınız dosyaları seçin.
2. Alt kısımdaki parlayan mavi **"JWL Log Üret"** butonuna tıklayın.
3. Ekrana gelen diyalog penceresindeki "Panoya Kopyala" butonuyla metni alın ve Jira'ya yapıştırın!
