package com.hamzawy.sat
import android.content.Context
import java.io.ByteArrayOutputStream
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import okhttp3.MultipartBody

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

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
import androidx.compose.material3.ExperimentalMaterial3Api

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
    var trackingLoading by remember { mutableStateOf(false) }
    var trackingResult by remember { mutableStateOf<TrackOrderResponse?>(null) }
    var trackingError by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    var service by remember { mutableStateOf("satellite") }
    var services by remember { mutableStateOf<List<ServiceItem>>(emptyList()) }
    var servicesLoading by remember { mutableStateOf(false) }
    var servicesError by remember { mutableStateOf("") }
    var orderSubmitting by remember { mutableStateOf(false) }
    var createdOrderId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(page) {
        if (page == Page.NEW_ORDER) {
            servicesLoading = true
            servicesError = ""
            ApiClient.api.getServices().enqueue(object : Callback<List<ServiceItem>> {
                override fun onResponse(
                    call: Call<List<ServiceItem>>,
                    response: Response<List<ServiceItem>>
                ) {
                    servicesLoading = false
                    if (response.isSuccessful) {
                        services = response.body().orEmpty().filter { it.active }
                        if (services.none { it.id == service }) {
                            service = services.firstOrNull()?.id.orEmpty()
                        }
                        servicesError = if (services.isEmpty()) {
                            "لا توجد خدمات متاحة حاليًا."
                        } else {
                            ""
                        }
                    } else {
                        servicesError = "تعذر تحميل الخدمات (HTTP ${response.code()})."
                    }
                }

                override fun onFailure(call: Call<List<ServiceItem>>, t: Throwable) {
                    servicesLoading = false
                    servicesError = "تعذر الاتصال بالخادم. تأكد أنه يعمل ثم حاول الرجوع وفتح الشاشة مجددًا."
                }
            })
        }
    }

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
                Page.TRACK -> TrackPage(
                    code = code,
                    onCodeChange = { code = it },
                    loading = trackingLoading,
                    result = trackingResult,
                    error = trackingError
                ) {
                    val orderCode = code.trim()
                    if (orderCode.isBlank()) {
                        trackingError = "اكتب كود الطلب أولًا."
                        trackingResult = null
                    } else {
                        trackingLoading = true
                        trackingError = ""
                        trackingResult = null
                        ApiClient.api.trackOrder(orderCode).enqueue(object : Callback<TrackOrderResponse> {
                            override fun onResponse(
                                call: Call<TrackOrderResponse>,
                                response: Response<TrackOrderResponse>
                            ) {
                                trackingLoading = false
                                val body = response.body()
                                if (response.isSuccessful && body != null) {
                                    trackingResult = body
                                } else {
                                    trackingError = if (response.code() == 404) {
                                        "لم يتم العثور على طلب بهذا الكود."
                                    } else {
                                        "تعذر جلب الطلب (HTTP ${response.code()})."
                                    }
                                }
                            }

                            override fun onFailure(call: Call<TrackOrderResponse>, t: Throwable) {
                                trackingLoading = false
                                trackingError = "تعذر الاتصال بالخادم. تحقق من عنوان الخادم واتصال الإنترنت."
                            }
                        })
                    }
                }
                Page.NEW_ORDER -> ServicePage(
                    service = service,
                    services = services,
                    loading = servicesLoading,
                    error = servicesError,
                    onService = { service = it },
                    notice = notice,
                    submitting = orderSubmitting,
                    createdOrderId = createdOrderId
                ) { name, customerPhone, area, address, description, photoPart ->
                    notice = ""
                    createdOrderId = null
                    orderSubmitting = true
                    val plainText = "text/plain".toMediaType()
                    ApiClient.api.createOrder(
                        service.toRequestBody(plainText),
                        name.toRequestBody(plainText),
                        customerPhone.toRequestBody(plainText),
                        area.toRequestBody(plainText),
                        address.toRequestBody(plainText),
                        description.toRequestBody(plainText),
                        photoPart
                    ).enqueue(object : Callback<CreateOrderResponse> {
                        override fun onResponse(
                            call: Call<CreateOrderResponse>,
                            response: Response<CreateOrderResponse>
                        ) {
                            orderSubmitting = false
                            val body = response.body()
                            if (response.isSuccessful && body?.ok == true && !body.orderId.isNullOrBlank()) {
                                createdOrderId = body.orderId
                                code = body.orderId
                                notice = "تم إرسال طلبك بنجاح."
                            } else {
                                notice = body?.error ?: body?.message
                                    ?: "تعذر إرسال الطلب (HTTP ${response.code()}). حاول مرة أخرى."
                            }
                        }

                        override fun onFailure(call: Call<CreateOrderResponse>, t: Throwable) {
                            orderSubmitting = false
                            notice = "فشل الاتصال بالخادم. تأكد أن الخادم يعمل ثم حاول مرة أخرى."
                        }
                    })
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
            lineTo(w * .62f, h * .32f)
            lineTo(w * .35f, h * .73f)
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

@OptIn(ExperimentalMaterial3Api::class)
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
private fun TrackPage(
    code: String,
    onCodeChange: (String) -> Unit,
    loading: Boolean,
    result: TrackOrderResponse?,
    error: String,
    onSubmit: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(28.dp))
        Text("متابعة الطلب", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("اكتب كود الطلب لمعرفة آخر تحديث", color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("كود الطلب") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onSubmit,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (loading) CircularProgressIndicator()
            else Text("بحث عن الطلب")
        }
        if (error.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        if (result != null) {
            Spacer(Modifier.height(20.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text("تفاصيل الطلب", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("الكود: ${result.id}")
                    Text("الخدمة: ${result.serviceName}")
                    val statusText = when (result.status) {
                        "new" -> "تم استلام الطلب، وجارٍ تحديد السعر"
                        "available" -> "تم تحديد السعر والطلب متاح"
                        "opened" -> "تم فتح الطلب بواسطة فني"
                        "completed" -> "مكتمل"
                        else -> result.status
                    }
                    Text("الحالة: $statusText")
                    if (result.agreedPrice != null) {
                        Text("السعر المتفق عليه: ${result.agreedPrice}")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServicePage(
    service: String,
    services: List<ServiceItem>,
    loading: Boolean,
    error: String,
    onService: (String) -> Unit,
    notice: String,
    submitting: Boolean,
    createdOrderId: String?,
    onSubmit: (String, String, String, String, String, MultipartBody.Part?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf("") }
    var selectedPhoto by remember { mutableStateOf<Uri?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> selectedPhoto = uri; validationError = "" }

    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp)
    ) {
        Text("طلب خدمة جديدة", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("اختار نوع الخدمة المطلوبة", color = Color.Gray)
        Spacer(Modifier.height(20.dp))

        if (loading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text("جاري تحميل الخدمات...")
        }

        if (error.isNotBlank()) {
            Text(error, color = Color(0xFF9A5600), fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
        }

        services.forEach { item ->
            Card(
                onClick = { onService(item.id); validationError = "" },
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (service == item.id) Color(0xFFDCEEFF) else Color.White
                )
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(17.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = service == item.id,
                        onClick = { onService(item.id); validationError = "" }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(item.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("بيانات العميل", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = name, onValueChange = { name = it; validationError = "" },
            label = { Text("الاسم بالكامل") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, enabled = !submitting
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = customerPhone, onValueChange = { customerPhone = it; validationError = "" },
            label = { Text("رقم الهاتف") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, enabled = !submitting
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = area, onValueChange = { area = it; validationError = "" },
            label = { Text("المنطقة") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, enabled = !submitting
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = address, onValueChange = { address = it; validationError = "" },
            label = { Text("العنوان بالتفصيل") }, modifier = Modifier.fillMaxWidth(),
            enabled = !submitting
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = description, onValueChange = { description = it; validationError = "" },
            label = { Text("وصف العطل أو الخدمة المطلوبة") },
            modifier = Modifier.fillMaxWidth(), minLines = 3, enabled = !submitting
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            enabled = !submitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (selectedPhoto == null) "اختيار صورة للعطل (اختياري)" else "تم اختيار صورة — اضغط للتغيير")
        }
        if (selectedPhoto != null) {
            Text("تم اختيار صورة. سيتم إرفاقها عند إرسال الطلب.", color = Green, fontSize = 13.sp)
        }
        Spacer(Modifier.height(16.dp))

        if (validationError.isNotBlank()) {
            Text(validationError, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                when {
                    service.isBlank() -> validationError = "اختار نوع الخدمة أولًا."
                    name.isBlank() || customerPhone.isBlank() || area.isBlank() ||
                        address.isBlank() || description.isBlank() ->
                        validationError = "من فضلك املأ كل البيانات المطلوبة."
                    customerPhone.filter { it.isDigit() }.length < 8 ->
                        validationError = "اكتب رقم هاتف صحيحًا."
                    else -> {
                        validationError = ""
                        try { val photoPart = selectedPhoto?.let { createPhotoPart(context, it) }; onSubmit(name.trim(), customerPhone.trim(), area.trim(), address.trim(), description.trim(), photoPart) } catch (e: Exception) { validationError = e.message ?: "تعذر تجهيز الصورة." }
                    }
                }
            },
            enabled = !loading && !submitting && services.any { it.id == service } && createdOrderId == null,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (submitting) CircularProgressIndicator(
                modifier = Modifier.size(22.dp), strokeWidth = 2.dp,
                color = Color.White
            ) else Text("إرسال طلب الخدمة")
        }

        if (notice.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                notice,
                color = if (createdOrderId != null) Green else Color(0xFF9A5600),
                fontSize = 14.sp
            )
        }
        if (createdOrderId != null) {
            Spacer(Modifier.height(8.dp))
            Text("كود طلبك:", fontWeight = FontWeight.Bold)
            Text(createdOrderId, color = Blue, fontSize = 18.sp)
            Text("احتفظ بالكود لاستخدامه في شاشة متابعة الطلب.", fontSize = 13.sp, color = Color.Gray)
        }
    }
}

private fun createPhotoPart(context: Context, uri: Uri): MultipartBody.Part {
    val mime = context.contentResolver.getType(uri)
        ?: throw IllegalArgumentException("تعذر تحديد نوع الصورة.")
    if (mime !in listOf("image/jpeg", "image/png", "image/webp")) {
        throw IllegalArgumentException("صيغة الصورة غير مدعومة. استخدم JPG أو PNG أو WEBP.")
    }
    val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            if (total > 5 * 1024 * 1024) {
                throw IllegalArgumentException("حجم الصورة أكبر من 5 ميجابايت.")
            }
            output.write(buffer, 0, count)
        }
        output.toByteArray()
    } ?: throw IllegalArgumentException("تعذر قراءة الصورة.")
    val ext = when (mime) {
        "image/jpeg" -> "jpg"
        "image/png" -> "png"
        else -> "webp"
    }
    val body = bytes.toRequestBody(mime.toMediaType())
    return MultipartBody.Part.createFormData("photo", "photo." + ext, body)
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
