package com.example

import com.example.supabase.PhoneUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `verify role display names for all 8 required roles`() {
    assertEquals("Farmer", getRoleDisplayName("farmer"))
    assertEquals("Labour / Farm Squad", getRoleDisplayName("labour"))
    assertEquals("Contract Farming", getRoleDisplayName("company"))
    assertEquals("Agri Waste", getRoleDisplayName("waste"))
    assertEquals("Seller", getRoleDisplayName("seller"))
    assertEquals("Broker", getRoleDisplayName("broker"))
    assertEquals("Customer", getRoleDisplayName("customer"))
    assertEquals("Delivery Partner", getRoleDisplayName("delivery"))
  }

  @Test
  fun `verify mobile number normalization and validation`() {
    assertEquals("+919876543210", PhoneUtils.normalizeToE164("9876543210"))
    assertEquals("+919876543210", PhoneUtils.normalizeToE164("+91 98765 43210"))
    assertEquals("+919876543210", PhoneUtils.normalizeToE164("09876543210"))

    assertNull(PhoneUtils.validateIndianMobile("9876543210"))
    assertNotNull(PhoneUtils.validateIndianMobile(""))
    assertNotNull(PhoneUtils.validateIndianMobile("12345"))
    assertNotNull(PhoneUtils.validateIndianMobile("1234567890")) // Doesn't start with 6-9
  }

  @Test
  fun `verify canonical role mapping`() {
    assertEquals("farmer", com.example.supabase.normalizeRoleId("farmer"))
    assertEquals("labour", com.example.supabase.normalizeRoleId("labour_squad"))
    assertEquals("contract_farming", com.example.supabase.normalizeRoleId("company"))
    assertEquals("agri_waste", com.example.supabase.normalizeRoleId("waste"))
    assertEquals("delivery_partner", com.example.supabase.normalizeRoleId("delivery"))
  }
}

