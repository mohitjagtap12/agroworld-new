package com.example.supabase

/**
 * Utility functions for validating and normalizing Indian mobile numbers
 * and handling role keys in AgroWorld.
 */
object PhoneUtils {

    /**
     * Extracts the core 10-digit Indian phone number from varied inputs.
     * E.g. "9876543210", "+919876543210", "+91 98765 43210", "09876543210", "919876543210".
     * Returns 10 digits or null if not valid.
     */
    fun extractTenDigitPhone(raw: String): String? {
        val digitsOnly = raw.filter { it.isDigit() }
        val tenDigits = when {
            digitsOnly.length == 10 -> digitsOnly
            digitsOnly.length == 11 && digitsOnly.startsWith("0") -> digitsOnly.substring(1)
            digitsOnly.length == 12 && digitsOnly.startsWith("91") -> digitsOnly.substring(2)
            digitsOnly.length == 13 && digitsOnly.startsWith("91") -> digitsOnly.takeLast(10)
            else -> null
        }

        if (tenDigits != null && tenDigits.length == 10 && tenDigits[0] in '6'..'9') {
            return tenDigits
        }
        return null
    }

    /**
     * Normalizes to canonical E.164 phone format for Supabase Phone Auth.
     * E.g. "+919876543210".
     */
    fun normalizeToE164(raw: String): String? {
        val ten = extractTenDigitPhone(raw) ?: return null
        return "+91$ten"
    }

    /**
     * Formats 10-digit phone for user-friendly UI display.
     * E.g. "+91 98765 43210".
     */
    fun formatDisplay(raw: String): String {
        val ten = extractTenDigitPhone(raw) ?: return raw
        return "+91 ${ten.substring(0, 5)} ${ten.substring(5)}"
    }

    /**
     * Validates an Indian mobile number.
     * Returns a user-friendly error message, or null if perfectly valid.
     */
    fun validateIndianMobile(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return "Mobile number cannot be empty"
        }
        val nonAllowedChars = trimmed.filter { !it.isDigit() && it != '+' && it != ' ' && it != '-' }
        if (nonAllowedChars.isNotEmpty()) {
            return "Mobile number contains invalid characters"
        }
        val digits = trimmed.filter { it.isDigit() }
        if (digits.length < 10) {
            return "Mobile number must have at least 10 digits"
        }
        val ten = extractTenDigitPhone(trimmed)
        if (ten == null) {
            return "Please enter a valid 10-digit Indian mobile number (starts with 6, 7, 8, or 9)"
        }
        return null
    }
}

/**
 * Normalizes role identifier to one of the 8 canonical lowercase strings:
 * - farmer
 * - labour
 * - contract_farming
 * - agri_waste
 * - seller
 * - broker
 * - customer
 * - delivery_partner
 */
fun normalizeRoleId(role: String): String {
    return when (role.trim().lowercase()) {
        "farmer" -> "farmer"
        "labour", "farm_squad", "labour_squad", "labour / farm squad" -> "labour"
        "contract_farming", "company", "contract farming" -> "contract_farming"
        "agri_waste", "waste", "waste_buyer", "agri waste" -> "agri_waste"
        "seller" -> "seller"
        "broker" -> "broker"
        "customer" -> "customer"
        "delivery_partner", "delivery", "delivery partner" -> "delivery_partner"
        else -> role.trim().lowercase()
    }
}

/**
 * Returns user-facing professional display name for the role.
 */
fun getRoleDisplayName(role: String): String {
    return when (normalizeRoleId(role)) {
        "farmer" -> "Farmer"
        "labour" -> "Labour / Farm Squad"
        "contract_farming" -> "Contract Farming"
        "agri_waste" -> "Agri Waste"
        "seller" -> "Seller"
        "broker" -> "Broker"
        "customer" -> "Customer"
        "delivery_partner" -> "Delivery Partner"
        else -> role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
