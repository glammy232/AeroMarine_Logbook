package com.aeromarine.logbook.ror.data.repo

import android.util.Log
import com.aeromarine.logbook.ror.domain.model.AeroMarineEntity
import com.aeromarine.logbook.ror.domain.model.AeroMarineParam
import com.aeromarine.logbook.ror.presentation.app.AeroMarineApp
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.lang.Exception

interface AeroMarineApi {
    @Headers("Content-Type: application/json")
    @POST("config.php")
    fun getClient(
        @Body jsonString: JsonObject,
    ): Call<AeroMarineEntity>
}


private const val AEROMARINE_MAIN = "https://aeromarinelogbook.com/"
class AeroMarineRepository {

    suspend fun chickenGetClient(
        aeroMarineParam: AeroMarineParam,
        chickenConversion: MutableMap<String, Any>?
    ): AeroMarineEntity? {
        val gson = Gson()
        val api = chickenGetApi(AEROMARINE_MAIN, null)

        val chickenJsonObject = gson.toJsonTree(aeroMarineParam).asJsonObject
        chickenConversion?.forEach { (key, value) ->
            val element: JsonElement = gson.toJsonTree(value)
            chickenJsonObject.add(key, element)
        }
        return try {
            val chickenRequest: Call<AeroMarineEntity> = api.getClient(
                jsonString = chickenJsonObject,
            )
            val chickenResult = chickenRequest.awaitResponse()
            Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Retrofit: Result code: ${chickenResult.code()}")
            if (chickenResult.code() == 200) {
                Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Retrofit: Get request success")
                Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Retrofit: Code = ${chickenResult.code()}")
                Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Retrofit: ${chickenResult.body()}")
                chickenResult.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Retrofit: Get request failed")
            Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Retrofit: ${e.message}")
            null
        }
    }


    private fun chickenGetApi(url: String, client: OkHttpClient?) : AeroMarineApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }


}
