package com.punhanaghayev.bimmerpro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.BuildConfig
import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.core.update.OtaUpdateManager
import com.punhanaghayev.bimmerpro.ui.components.BmwRoundelLogo
import com.punhanaghayev.bimmerpro.ui.components.MSportAccentStrip
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle

@Composable
fun DeveloperInfoScreen(
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val theme = LocalThemeStyle.current

    val title = when (strings.language) {
        AppLanguage.EN -> "Developer"
        AppLanguage.AZ -> "Tərtibatçı"
        AppLanguage.RU -> "Разработчик"
        AppLanguage.TR -> "Geliştirici"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.textPrimary)
                }
                Icon(Icons.Default.Person, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.primary.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BmwRoundelLogo(size = 80.dp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Punhan Aghayev",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = "Lead Architect",
                            color = theme.primaryBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "BMW Diagnostics & Coding Engineer",
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                        Row(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(theme.cardElevated)
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(15.dp))
                            Text("cwghost@hotmail.com", color = theme.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(theme.cardElevated)
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(15.dp))
                            Text(
                                "github.com/${OtaUpdateManager.GITHUB_OWNER}/${OtaUpdateManager.GITHUB_REPO}",
                                color = theme.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "BMW Expert PRO v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                            color = theme.textSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    MSportAccentStrip()
                }
            }
        }
    }
}
