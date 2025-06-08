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
fun PrivacyPolicyScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Gizlilik Politikası",
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
                text = "Gizlilik Politikası",
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

            PrivacySection(
                title = "1. Giriş",
                content = "Carwise uygulamasını kullandığınız için teşekkür ederiz. Bu gizlilik politikası, " +
                        "uygulamamızı kullanırken kişisel verilerinizin nasıl toplandığını, kullanıldığını ve " +
                        "korunduğunu açıklamaktadır."
            )

            PrivacySection(
                title = "2. Toplanan Veriler",
                content = "Uygulamamız aşağıdaki verileri toplayabilir:\n\n" +
                        "• Hesap bilgileri (e-posta, isim)\n" +
                        "• Araç bilgileri (plaka, marka, model)\n" +
                        "• Konum bilgileri (araç takibi için)\n" +
                        "• Cihaz bilgileri (model, işletim sistemi)\n" +
                        "• Kullanım istatistikleri"
            )

            PrivacySection(
                title = "3. Verilerin Kullanımı",
                content = "Toplanan veriler aşağıdaki amaçlar için kullanılmaktadır:\n\n" +
                        "• Hizmetlerimizin sağlanması ve iyileştirilmesi\n" +
                        "• Güvenlik ve dolandırıcılık önleme\n" +
                        "• Müşteri desteği\n" +
                        "• Yasal yükümlülüklerin yerine getirilmesi"
            )

            PrivacySection(
                title = "4. Veri Güvenliği",
                content = "Verilerinizin güvenliği bizim için önemlidir. Verilerinizi korumak için " +
                        "endüstri standardı güvenlik önlemleri kullanıyoruz. Bu önlemler şunları içerir:\n\n" +
                        "• Veri şifreleme\n" +
                        "• Güvenli veri depolama\n" +
                        "• Düzenli güvenlik güncellemeleri\n" +
                        "• Erişim kontrolü ve yetkilendirme"
            )

            PrivacySection(
                title = "5. Veri Paylaşımı",
                content = "Kişisel verileriniz, aşağıdaki durumlar dışında üçüncü taraflarla paylaşılmaz:\n\n" +
                        "• Yasal zorunluluk durumunda\n" +
                        "• Hizmet sağlayıcılarımızla (veri işleme amaçlı)\n" +
                        "• İş ortaklarımızla (sizin onayınızla)\n" +
                        "• Şirket birleşme veya satın alma durumlarında"
            )

            PrivacySection(
                title = "6. Kullanıcı Hakları",
                content = "Verilerinizle ilgili aşağıdaki haklara sahipsiniz:\n\n" +
                        "• Verilerinize erişim\n" +
                        "• Verilerinizin düzeltilmesi\n" +
                        "• Verilerinizin silinmesi\n" +
                        "• Veri işlemeye itiraz\n" +
                        "• Veri taşınabilirliği"
            )

            PrivacySection(
                title = "7. İletişim",
                content = "Gizlilik politikamız hakkında sorularınız için bize ulaşabilirsiniz:\n\n" +
                        "E-posta: privacy@carwise.com\n" +
                        "Telefon: +90 (212) XXX XX XX\n" +
                        "Adres: İstanbul, Türkiye"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PrivacySection(
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