package com.hamzawy.sat

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.POST

data class ServiceItem(
val id: String,
val name: String,
val active: Boolean
)

data class CreateOrderResponse(
val ok: Boolean = false,
val orderId: String? = null,
val message: String? = null,
val error: String? = null
)

data class TrackOrderResponse(
val id: String,
val serviceName: String,
val status: String,
val agreedPrice: Int? = null,
val error: String? = null
)


data class TechnicianRegisterRequest(
    val name: String,
    val phone: String,
    val password: String,
    val area: String
)

data class TechnicianLoginRequest(
    val phone: String,
    val password: String
)

data class TechnicianAuthResponse(
    val ok: Boolean = false,
    val token: String? = null,
    val message: String? = null,
    val error: String? = null,
    val technician: TechnicianInfo? = null
)

data class TechnicianInfo(
    val id: String,
    val name: String,
    val phone: String,
    val balance: Int = 0
)

interface HamzawyApi {
@GET("api/services")
fun getServices(): Call<List<ServiceItem>>

@Multipart
@POST("api/orders")
fun createOrder(
    @Part("serviceId") serviceId: RequestBody,
    @Part("name") name: RequestBody,
    @Part("phone") phone: RequestBody,
    @Part("area") area: RequestBody,
    @Part("address") address: RequestBody,
    @Part("description") description: RequestBody,
    @Part photo: MultipartBody.Part? = null
): Call<CreateOrderResponse>


@POST("api/technicians/register")
fun registerTechnician(
    @Body request: TechnicianRegisterRequest
): Call<TechnicianAuthResponse>

@POST("api/technicians/login")
fun loginTechnician(
    @Body request: TechnicianLoginRequest
): Call<TechnicianAuthResponse>

@GET("api/orders/{id}")
fun trackOrder(@Path("id") id: String): Call<TrackOrderResponse>

}

object ApiClient {
private val retrofit: Retrofit by lazy {
Retrofit.Builder()
.baseUrl("http://127.0.0.1:3000/")
.addConverterFactory(GsonConverterFactory.create())
.build()
}

val api: HamzawyApi by lazy {
    retrofit.create(HamzawyApi::class.java)
}

}
