package com.hamzawy.sat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF071D3A)
private val Blue = Color(0xFF0878E8)
private val Cyan = Color(0xFF19C4F4)
private val Pale = Color(0xFFF2F7FD)
private val Green = Color(0xFF079B70)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Blue,
                    secondary = Cyan,
                    background = Pale,
                    surface = Color.White,
                    onPrimary = Color.White,
                    onBackground = Navy,
                    onSurface = Navy
                )
            ) { HamzawyApp() }
        }
    }
}

private enum class Page { START, TECH_LOGIN, TECH_HOME, CUSTOMER_HOME, TRACK, NEW_ORDER, PROFILE }

@Composable
private fun HamzawyApp() {
    var page by remember { mutableStateOf(Page.START) }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    var service by remember { mutableStateOf("تركيب دش") }

    Column(Modifier.fillMaxSize().background(Pale)) {
        if (page != Page.START) {
            Row(
                Modifier.fillMaxWidth().background(Navy).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { page = if (page == Page.TECH_HOME || page == Page.CUSTOMER_HOME) Page.START else if (page == Page.TRACK || page == Page.NEW_ORDER || page == Page.PROFILE) Page.CUSTOMER_HOME else Page.START }) {
                    Text("رجوع", color = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text("حمزاوي سات", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Spacer(Modifier.width(8.dp))
                SatelliteLogo(38)
            }
        }
        Box(Modifier.fillMaxSize()) {
            when (page) {
                Page.START -> StartPage(
                    onTechnician = { notice = ""; page = Page.TECH_LOGIN },
                    onCustomer = { notice = ""; page = Page.CUSTOMER_HOME }
                )
                Page.TECH_LOGIN -> TechnicianLogin(phone, { phone = it }, password, { password = it }, notice) {
                    notice = "واجهة الدخول جاهزة للتصميم، لكن التحقق من الحساب يحتاج ربط الخادم. لم يتم تسجيل الدخول."
                }
                Page.TECH_HOME -> SimpleDashboard("لوحة الفني", listOf("الطلبات المتاحة", "طلباتي", "المحفظة", "حسابي"), { notice = it }, notice)
                Page.CUSTOMER_HOME -> CustomerPage(
                    onNew = { page = Page.NEW_ORDER },
                    onTrack = { page = Page.TRACK },
                    onProfile = { page = Page.PROFILE }
                )
                Page.TRACK -> FormPage("متابعة الطلب", "اكتب كود الطلب", code, { code = it }, "بحث عن الطلب") {
                    notice = "واجهة البحث جاهزة، لكن لم يتم الاتصال بالخادم بعد؛ لا توجد نتيجة طلب فعلية."
                }.also { }
                Page.NEW_ORDER -> ServicePage(service, { service = it }, notice) {
                    notice = "تم تجهيز اختيار الخدمة في الواجهة فقط. إرسال الطلب يحتاج ربط الخادم."
                }
                Page.PROFILE -> SimpleDashboard("حساب العميل", listOf("بيانات الحساب", "طلباتي", "مساعدة ودعم"), { notice = it }, notice)
            }
        }
    }
}

@Composable
private fun StartPage(onTechnician: () -> Unit, onCustomer: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.fillMaxWidth().background(Navy, RoundedCornerShape(28.dp)).padding(vertical = 28.dp, horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SatelliteLogo(112)
                Spacer(Modifier.height(10.dp))
                Text("HAMZAWY SAT", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.5.sp)
                Text("حمزاوي سات", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Cyan)
                Spacer(Modifier.height(8.dp))
                Text("خدمات تركيب وصيانة الدش والأجهزة", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(26.dp))
        Text("أهلاً بيك", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Navy)
        Text("اختار طريقة الدخول للمتابعة", color = Color(0xFF66758A), fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))
        RoleButton("أنا فني", "تسجيل دخول الفني وإدارة الطلبات", "⚒", Blue, onTechnician)
        Spacer(Modifier.height(14.dp))
        RoleButton("أنا عميل", "اطلب خدمة وتابع حالة طلبك", "⌂", Green, onCustomer)
        Spacer(Modifier.height(22.dp))
        Text("خدمة موثوقة • متابعة سهلة • دعم فني", color = Color(0xFF748197), fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SatelliteLogo(size: Int) {
    Canvas(Modifier.size(size.dp)) {
        val w = this.size.width
        val h = this.size.height
        val c = Color(0xFF18BDF3)
        drawArc(c, 205f, 125f, false, topLeft = androidx.compose.ui.geometry.Offset(w * .18f, h * .16f), size = androidx.compose.ui.geometry.Size(w * .68f, h * .68f), style = Stroke(width = w * .055f, cap = StrokeCap.Round))
        drawArc(c, 220f, 100f, false, topLeft = androidx.compose.ui.geometry.Offset(w * .37f, h * .02f), size = androidx.compose.ui.geometry.Size(w * .58f, h * .58f), style = Stroke(width = w * .05f, cap = StrokeCap.Round))
        val dish = Path().apply {
            moveTo(w * .18f, h * .58f)
            quadraticTo(w * .30f, h * .22f, w * .62f, h * .32f)
            quadraticTo(w * .60f, h * .60f, w * .35f, h * .73f)
            close()
        }
        drawPath(dish, Color.White)
        drawLine(Navy, androidx.compose.ui.geometry.Offset(w * .35f, h * .70f), androidx.compose.ui.geometry.Offset(w * .57f, h * .84f), w * .055f, cap = StrokeCap.Round)
        drawLine(c, androidx.compose.ui.geometry.Offset(w * .57f, h * .84f), androidx.compose.ui.geometry.Offset(w * .76f, h * .84f), w * .05f, cap = StrokeCap.Round)
        drawCircle(c, radius = w * .045f, center = androidx.compose.ui.geometry.Offset(w * .76f, h * .84f))
    }
}

@Composable
private fun RoleButton(title: String, description: String, symbol: String, color: Color, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(54.dp).background(color.copy(alpha = .12f), CircleShape), contentAlignment = Alignment.Center) {
                Text(symbol, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Navy)
                Spacer(Modifier.height(4.dp))
                Text(description, fontSize = 12.sp, color = Color(0xFF66758A))
                Spacer(Modifier.height(10.dp))
                Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = color)) { Text("متابعة  ←", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun TechnicianLogin(phone: String, onPhone: (String) -> Unit, password: String, onPassword: (String) -> Unit, notice: String, onLogin: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(18.dp))
        SatelliteLogo(78)
        Spacer(Modifier.height(12.dp))
        Text("تسجيل دخول الفني", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("أدخل بيانات حسابك", color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = phone, onValueChange = onPhone, modifier = Modifier.fillMaxWidth(), label = { Text("رقم الهاتف / اسم المستخدم") }, singleLine = true, shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = onPassword, modifier = Modifier.fillMaxWidth(), label = { Text("كلمة المرور") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.height(20.dp))
        Button(onClick = onLogin, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) { Text("دخول", fontSize = 17.sp, fontWeight = FontWeight.Bold) }
        if (notice.isNotBlank()) { Spacer(Modifier.height(14.dp)); Text(notice, color = Color(0xFF9A5600), textAlign = TextAlign.Center, fontSize = 13.sp) }
    }
}

@Composable
private fun CustomerPage(onNew: () -> Unit, onTrack: () -> Unit, onProfile: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("مرحباً بيك 👋", fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text("خدمات حمزاوي سات في مكان واحد", color = Color.Gray)
        Spacer(Modifier.height(20.dp))
        ActionCard("طلب خدمة جديدة", "اختار الخدمة اللي محتاجها", "＋", onNew)
        Spacer(Modifier.height(12.dp))
        ActionCard("متابعة الطلبات", "تابع حالة طلبك باستخدام الكود", "⌕", onTrack)
        Spacer(Modifier.height(12.dp))
        ActionCard("حسابي", "بيانات الحساب والمساعدة", "♙", onProfile)
        Spacer(Modifier.height(22.dp))
        Text("خدماتنا", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        listOf("تركيب دش", "صيانة دش", "تركيب كاميرات", "صيانة أجهزة").forEach { item ->
            Card(Modifier.fillMaxWidth().padding(bottom = 8.dp), shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("◉", color = Blue, fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(item, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun ActionCard(title: String, description: String, symbol: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(Blue.copy(alpha = .10f), CircleShape), contentAlignment = Alignment.Center) { Text(symbol, color = Blue, fontSize = 27.sp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) { Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(4.dp)); Text(description, fontSize = 12.sp, color = Color.Gray) }
            Text("‹", fontSize = 28.sp, color = Blue)
        }
    }
}

@Composable
private fun FormPage(title: String, label: String, value: String, onValue: (String) -> Unit, button: String, onSubmit: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(28.dp))
        Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("اكتب البيانات المطلوبة للمتابعة", color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = value, onValueChange = onValue, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true, shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) { Text(button) }
    }
}

@Composable
private fun ServicePage(service: String, onService: (String) -> Unit, notice: String, onSubmit: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
        Text("طلب خدمة جديدة", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("اختار نوع الخدمة المطلوبة", color = Color.Gray)
        Spacer(Modifier.height(20.dp))
        listOf("تركيب دش", "صيانة دش", "تركيب كاميرات", "صيانة أجهزة", "تركيب تلفزيون").forEach { item ->
            Card(onClick = { onService(item) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = if (service == item) Color(0xFFDCEEFF) else Color.White)) {
                Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = service == item, onClick = { onService(item) })
                    Spacer(Modifier.width(8.dp))
                    Text(item, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) { Text("متابعة الطلب") }
        if (notice.isNotBlank()) { Spacer(Modifier.height(12.dp)); Text(notice, color = Color(0xFF9A5600), fontSize = 13.sp) }
    }
}

@Composable
private fun SimpleDashboard(title: String, items: List<String>, onClick: (String) -> Unit, notice: String) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("حمزاوي سات", color = Blue)
        Spacer(Modifier.height(20.dp))
        items.forEach { item ->
            ActionCard(item, "عرض القسم", "›") { onClick(item) }
            Spacer(Modifier.height(10.dp))
        }
        if (notice.isNotBlank()) { Spacer(Modifier.height(12.dp)); Text(notice, color = Color(0xFF9A5600), fontSize = 13.sp) }
    }
}
