package com.example.supabase

import retrofit2.Call
import retrofit2.http.*

/**
 * Retrofit interface communicating with Supabase GoTrue Auth and PostgREST REST APIs.
 */
interface SupabaseApiService {

    // ---------------- AUTH (GoTrue) ----------------
    @POST("auth/v1/token?grant_type=password")
    fun signInWithPassword(
        @Header("apikey") apiKey: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body body: SupabaseAuthRequest
    ): Call<SupabaseAuthResponse>

    @POST("auth/v1/signup")
    fun signUp(
        @Header("apikey") apiKey: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body body: SupabaseAuthRequest
    ): Call<SupabaseAuthResponse>

    @POST("auth/v1/logout")
    fun signOut(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String
    ): Call<Void>

    @POST("auth/v1/recover")
    fun recoverPassword(
        @Header("apikey") apiKey: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body body: SupabaseRecoverRequest
    ): Call<Void>

    @GET("auth/v1/user")
    fun getUser(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String
    ): Call<SupabaseUser>

    // ---------------- POSTGREST DATABASE (Profiles) ----------------
    @GET("rest/v1/profiles")
    fun getProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("id") idFilter: String, // e.g. "eq.usr-123"
        @Query("select") select: String = "*"
    ): Call<List<SupabaseProfile>>

    @GET("rest/v1/profiles")
    fun getProfileByMobile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("mobile") mobileFilter: String, // e.g. "eq.+919876543210"
        @Query("select") select: String = "*"
    ): Call<List<SupabaseProfile>>

    @POST("rest/v1/profiles")
    fun upsertProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body profile: SupabaseProfile
    ): Call<Void>

    // ---------------- ROLE SPECIFIC PROFILE POSTGREST TABLES ----------------
    @POST("rest/v1/farmer_profiles")
    fun insertFarmerProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body record: FarmerProfileRecord,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates"
    ): Call<Void>

    @POST("rest/v1/labour_profiles")
    fun insertLabourProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body record: LabourProfileRecord,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates"
    ): Call<Void>

    @POST("rest/v1/contract_farming_profiles")
    fun insertContractFarmingProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body record: ContractFarmingProfileRecord,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates"
    ): Call<Void>

    @POST("rest/v1/agri_waste_profiles")
    fun insertAgriWasteProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body record: AgriWasteProfileRecord,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates"
    ): Call<Void>

    @POST("rest/v1/seller_profiles")
    fun insertSellerProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body record: SellerProfileRecord,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates"
    ): Call<Void>

    @POST("rest/v1/broker_profiles")
    fun insertBrokerProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body record: BrokerProfileRecord,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates"
    ): Call<Void>

    @POST("rest/v1/customer_profiles")
    fun insertCustomerProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body record: CustomerProfileRecord,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates"
    ): Call<Void>

    @POST("rest/v1/delivery_partner_profiles")
    fun insertDeliveryPartnerProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body record: DeliveryPartnerProfileRecord,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates"
    ): Call<Void>
}
