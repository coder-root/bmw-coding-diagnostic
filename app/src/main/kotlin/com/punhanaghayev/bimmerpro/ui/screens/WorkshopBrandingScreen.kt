package com.punhanaghayev.bimmerpro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.data.WorkshopProfile
import com.punhanaghayev.bimmerpro.ui.components.BmwPrimaryButton
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen

@Composable
fun WorkshopBrandingScreen(
    workshopProfile: WorkshopProfile,
    onSaveWorkshopProfile: (WorkshopProfile) -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val theme = LocalThemeStyle.current

    var wName by remember(workshopProfile) { mutableStateOf(workshopProfile.workshopName) }
    var wPhone by remember(workshopProfile) { mutableStateOf(workshopProfile.phoneNumber) }
    var wAddress by remember(workshopProfile) { mutableStateOf(workshopProfile.address) }
    var wTech by remember(workshopProfile) { mutableStateOf(workshopProfile.technicianName) }

    val screenTitle = when (strings.language) {
        AppLanguage.EN -> "Workshop & PDF Branding"
        AppLanguage.AZ -> "Servis və PDF Markalaması"
        AppLanguage.RU -> "Брендинг сервиса и PDF"
        AppLanguage.TR -> "Servis & PDF Markalama"
    }
    val subtitle = when (strings.language) {
        AppLanguage.EN -> "Your workshop details and certified technician signature will automatically appear on all exported PDF and HTML inspection reports."
        AppLanguage.AZ -> "Servis məlumatlarınız və sertifikatlı texnik imzanız bütün PDF və HTML ekspertiza hesabatlarında avtomatik göstəriləcək."
        AppLanguage.RU -> "Данные вашего автосервиса и подпись техника будут автоматически добавляться во все PDF и HTML отчеты."
        AppLanguage.TR -> "Servis bilgileriniz ve sertifikalı teknisyen imzanız oluşturulan tüm PDF ve HTML ekspertiz raporlarına otomatik olarak eklenir."
    }
    val nameLabel = when (strings.language) {
        AppLanguage.EN -> "Workshop / Company Name"
        AppLanguage.AZ -> "Servis / Şirkət Adı"
        AppLanguage.RU -> "Название сервиса / компании"
        AppLanguage.TR -> "Servis / Firma Adı"
    }
    val phoneLabel = when (strings.language) {
        AppLanguage.EN -> "Phone Number"
        AppLanguage.AZ -> "Telefon Nömrəsi"
        AppLanguage.RU -> "Телефон"
        AppLanguage.TR -> "Telefon Numarası"
    }
    val addressLabel = when (strings.language) {
        AppLanguage.EN -> "Address / City"
        AppLanguage.AZ -> "Ünvan / Şəhər"
        AppLanguage.RU -> "Адрес / Город"
        AppLanguage.TR -> "Adres / Şehir"
    }
    val techLabel = when (strings.language) {
        AppLanguage.EN -> "Certified Technician Name"
        AppLanguage.AZ -> "Sertifikatlı Texnik Adı"
        AppLanguage.RU -> "Сертифицированный техник"
        AppLanguage.TR -> "Sertifikalı Teknisyen Adı"
    }
    val saveLabel = when (strings.language) {
        AppLanguage.EN -> "Save Workshop Profile"
        AppLanguage.AZ -> "Servis Profilini Yadda Saxla"
        AppLanguage.RU -> "Сохранить профиль сервиса"
        AppLanguage.TR -> "Servis Bilgilerini Kaydet"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.textPrimary)
                }
                Icon(Icons.Default.Business, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(screenTitle, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(subtitle, color = theme.textSecondary, fontSize = 12.sp)

                    OutlinedTextField(
                        value = wName,
                        onValueChange = { wName = it },
                        label = { Text(nameLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wPhone,
                        onValueChange = { wPhone = it },
                        label = { Text(phoneLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wAddress,
                        onValueChange = { wAddress = it },
                        label = { Text(addressLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wTech,
                        onValueChange = { wTech = it },
                        label = { Text(techLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val draft = WorkshopProfile(wName, wPhone, wAddress, wTech)
                    if (draft.isConfigured) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "PDF Header:",
                                color = StatusGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (wName.isNotBlank()) {
                                Text(text = wName.trim(), color = theme.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            if (wPhone.isNotBlank()) {
                                Text(text = wPhone.trim(), color = theme.textSecondary, fontSize = 11.sp)
                            }
                            if (wAddress.isNotBlank()) {
                                Text(text = wAddress.trim(), color = theme.textSecondary, fontSize = 11.sp)
                            }
                            if (wTech.isNotBlank()) {
                                Text(text = wTech.trim(), color = theme.primaryBright, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    BmwPrimaryButton(
                        text = saveLabel,
                        icon = Icons.Default.Save,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onSaveWorkshopProfile(draft) }
                    )
                }
            }
        }
    }
}
