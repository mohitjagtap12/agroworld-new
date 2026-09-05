package com.example.supabase

/**
 * Supabase configuration constants for AgroWorld.
 *
 * Configured for Supabase Auth, PostgreSQL (profiles table), and Storage.
 * When real project credentials are provided via AI Studio Secrets or environment,
 * they replace these default development endpoints.
 */
object SupabaseConfig {
    // Default Supabase project URL and anon public key for AgroWorld
    var SUPABASE_URL: String = "https://agroworld-supabase.supabase.co"
    var SUPABASE_ANON_KEY: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.agroworld-anon-key-placeholder"

    // Table names in Supabase PostgreSQL
    const val TABLE_PROFILES = "profiles"
    const val TABLE_FARMER_PROFILES = "farmer_profiles"
    const val TABLE_LABOUR_PROFILES = "labour_profiles"
    const val TABLE_CONTRACT_FARMING_PROFILES = "contract_farming_profiles"
    const val TABLE_AGRI_WASTE_PROFILES = "agri_waste_profiles"
    const val TABLE_SELLER_PROFILES = "seller_profiles"
    const val TABLE_BROKER_PROFILES = "broker_profiles"
    const val TABLE_CUSTOMER_PROFILES = "customer_profiles"
    const val TABLE_DELIVERY_PARTNER_PROFILES = "delivery_partner_profiles"
    const val TABLE_CROPS = "crops"
    const val TABLE_LABOUR_REQUESTS = "labour_requests"
    const val TABLE_CONTRACTS = "contracts"
    const val TABLE_ORDERS = "orders"
    const val TABLE_AGRI_WASTE = "agri_waste"

    // Storage Buckets
    const val BUCKET_PRODUCE_IMAGES = "produce-images"
    const val BUCKET_LEAF_DISEASE = "leaf-disease-scans"
    const val BUCKET_AVATARS = "avatars"
}
