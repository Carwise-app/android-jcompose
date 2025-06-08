package com.carwise.android.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carwise.android.ui.theme.appRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KVKKScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "KVKK Aydınlatma Metni",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = appRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = appRed
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Kişisel Verilerin Korunması Kanunu Aydınlatma Metni",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = appRed
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Son Güncelleme: 1 Mart 2024",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            KVKKSection(
                title = "1. Veri Sorumlusu",
                content = "6698 sayılı Kişisel Verilerin Korunması Kanunu (\"KVKK\") uyarınca, Carwise olarak " +
                        "veri sorumlusu sıfatıyla, kişisel verilerinizi aşağıda açıklanan kapsamda işleyebileceğimizi " +
                        "bilgilerinize sunarız."
            )

            KVKKSection(
                title = "2. Kişisel Verilerin İşlenme Amacı",
                content = "Kişisel verileriniz, aşağıdaki amaçlar doğrultusunda işlenmektedir:\n\n" +
                        "• Hizmetlerimizin sunulması ve geliştirilmesi\n" +
                        "• Müşteri ilişkileri yönetimi\n" +
                        "• Yasal yükümlülüklerin yerine getirilmesi\n" +
                        "• Güvenlik ve dolandırıcılık önleme\n" +
                        "• Müşteri memnuniyeti ve hizmet kalitesinin artırılması"
            )

            KVKKSection(
                title = "3. İşlenen Kişisel Veriler",
                content = "İşlenen kişisel verileriniz şunlardır:\n\n" +
                        "• Kimlik bilgileri (ad, soyad, T.C. kimlik no)\n" +
                        "• İletişim bilgileri (e-posta, telefon, adres)\n" +
                        "• Araç bilgileri (plaka, marka, model, yıl)\n" +
                        "• Konum bilgileri\n" +
                        "• Kullanım verileri ve tercihler"
            )

            KVKKSection(
                title = "4. Kişisel Verilerin Aktarılması",
                content = "Kişisel verileriniz, KVKK'nın 8. ve 9. maddelerinde belirtilen kişisel veri işleme " +
                        "şartları ve amaçları kapsamında, aşağıdaki alıcı gruplarına aktarılabilecektir:\n\n" +
                        "• Yasal merciler\n" +
                        "• Hizmet sağlayıcılarımız\n" +
                        "• İş ortaklarımız\n" +
                        "• Denetim ve danışmanlık şirketleri"
            )

            KVKKSection(
                title = "5. Kişisel Veri Sahibinin Hakları",
                content = "KVKK'nın 11. maddesi uyarınca, kişisel veri sahibi olarak aşağıdaki haklara sahipsiniz:\n\n" +
                        "• Kişisel verilerinizin işlenip işlenmediğini öğrenme\n" +
                        "• Kişisel verileriniz işlenmişse buna ilişkin bilgi talep etme\n" +
                        "• Kişisel verilerinizin işlenme amacını ve bunların amacına uygun kullanılıp kullanılmadığını öğrenme\n" +
                        "• Yurt içinde veya yurt dışında kişisel verilerinizin aktarıldığı üçüncü kişileri bilme\n" +
                        "• Kişisel verilerinizin eksik veya yanlış işlenmiş olması hâlinde bunların düzeltilmesini isteme\n" +
                        "• KVKK'nın 7. maddesinde öngörülen şartlar çerçevesinde kişisel verilerinizin silinmesini veya yok edilmesini isteme"
            )

            KVKKSection(
                title = "6. Başvuru Hakkı",
                content = "KVKK kapsamında haklarınızı kullanmak için, kimliğinizi tespit edici gerekli bilgiler ile " +
                        "talep konunuzu içeren başvurunuzu aşağıdaki yöntemlerle iletebilirsiniz:\n\n" +
                        "• E-posta: kvkk@carwise.com\n" +
                        "• Telefon: +90 (212) XXX XX XX\n" +
                        "• Adres: İstanbul, Türkiye\n\n" +
                        "Başvurunuz, talebinizin niteliğine göre en kısa sürede ve en geç 30 (otuz) gün içinde " +
                        "ücretsiz olarak sonuçlandırılacaktır."
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun KVKKSection(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = appRed
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}