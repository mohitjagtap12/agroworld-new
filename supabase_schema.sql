-- ====================================================================
-- AGROWORLD SUPABASE POSTGRESQL DATABASE ARCHITECTURE & RLS POLICIES
-- ====================================================================

-- 1. PROFILES TABLE
-- Primary profile for all authenticated users linked to auth.users(id)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL,
    mobile TEXT NOT NULL UNIQUE,
    role TEXT NOT NULL CHECK (role IN (
        'farmer',
        'labour',
        'contract_farming',
        'agri_waste',
        'seller',
        'broker',
        'customer',
        'delivery_partner'
    )),
    profile_complete BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Index for fast lookup by phone number
CREATE INDEX IF NOT EXISTS idx_profiles_mobile ON public.profiles(mobile);
CREATE INDEX IF NOT EXISTS idx_profiles_role ON public.profiles(role);

-- 2. ROLE-SPECIFIC TABLES

-- Farmer Profiles
CREATE TABLE IF NOT EXISTS public.farmer_profiles (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    village TEXT NOT NULL,
    taluka TEXT NOT NULL,
    district TEXT NOT NULL DEFAULT 'Pune',
    main_crop TEXT NOT NULL,
    land_area TEXT NOT NULL,
    land_area_unit TEXT NOT NULL DEFAULT 'Acres',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Labour / Farm Squad Profiles
CREATE TABLE IF NOT EXISTS public.labour_profiles (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    village TEXT NOT NULL,
    taluka TEXT NOT NULL,
    district TEXT NOT NULL DEFAULT 'Pune',
    work_type TEXT NOT NULL,
    experience TEXT NOT NULL,
    availability TEXT NOT NULL,
    preferred_work_area TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Contract Farming Profiles
CREATE TABLE IF NOT EXISTS public.contract_farming_profiles (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    entity_type TEXT NOT NULL, -- 'Individual' or 'Organization'
    organization_name TEXT,
    address TEXT NOT NULL,
    city TEXT NOT NULL,
    taluka TEXT NOT NULL,
    district TEXT NOT NULL DEFAULT 'Pune',
    business_type TEXT NOT NULL,
    crops_handled TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Agri Waste Profiles
CREATE TABLE IF NOT EXISTS public.agri_waste_profiles (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    entity_type TEXT NOT NULL, -- 'Individual' or 'Business'
    business_name TEXT,
    address TEXT NOT NULL,
    city TEXT NOT NULL,
    taluka TEXT NOT NULL,
    district TEXT NOT NULL DEFAULT 'Pune',
    waste_type TEXT NOT NULL,
    business_type TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Seller Profiles
CREATE TABLE IF NOT EXISTS public.seller_profiles (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    shop_name TEXT NOT NULL,
    owner_name TEXT NOT NULL,
    address TEXT NOT NULL,
    city TEXT NOT NULL,
    taluka TEXT NOT NULL,
    district TEXT NOT NULL DEFAULT 'Pune',
    product_categories TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Broker Profiles
CREATE TABLE IF NOT EXISTS public.broker_profiles (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    business_name TEXT,
    address TEXT NOT NULL,
    city TEXT NOT NULL,
    taluka TEXT NOT NULL,
    district TEXT NOT NULL DEFAULT 'Pune',
    crops_handled TEXT NOT NULL,
    market_area TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Customer Profiles
CREATE TABLE IF NOT EXISTS public.customer_profiles (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    delivery_address TEXT NOT NULL,
    city TEXT NOT NULL,
    taluka TEXT NOT NULL,
    district TEXT NOT NULL DEFAULT 'Pune',
    pin_code TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Delivery Partner Profiles
CREATE TABLE IF NOT EXISTS public.delivery_partner_profiles (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    address TEXT NOT NULL,
    city TEXT NOT NULL,
    taluka TEXT NOT NULL,
    district TEXT NOT NULL DEFAULT 'Pune',
    vehicle_type TEXT NOT NULL,
    vehicle_number TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ====================================================================
-- ROW LEVEL SECURITY (RLS)
-- ====================================================================

-- Enable RLS on all profile tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.farmer_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.labour_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.contract_farming_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.agri_waste_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.seller_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.broker_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.customer_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.delivery_partner_profiles ENABLE ROW LEVEL SECURITY;

-- 1. Profiles Table Policies
-- Users can read their own profile
CREATE POLICY "Users can read own profile"
    ON public.profiles FOR SELECT
    USING (auth.uid() = id);

-- Users can insert their own profile during registration
CREATE POLICY "Users can insert own profile"
    ON public.profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

-- Users can update only their non-role fields
CREATE POLICY "Users can update own non-role profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id)
    WITH CHECK (
        auth.uid() = id AND 
        role = (SELECT role FROM public.profiles WHERE id = auth.uid()) -- Role cannot be altered by client
    );

-- 2. Role Specific Table Policies
-- Farmer
CREATE POLICY "Farmer can view own profile" ON public.farmer_profiles FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Farmer can insert own profile" ON public.farmer_profiles FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Farmer can update own profile" ON public.farmer_profiles FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- Labour
CREATE POLICY "Labour can view own profile" ON public.labour_profiles FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Labour can insert own profile" ON public.labour_profiles FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Labour can update own profile" ON public.labour_profiles FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- Contract Farming
CREATE POLICY "Contract farming can view own profile" ON public.contract_farming_profiles FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Contract farming can insert own profile" ON public.contract_farming_profiles FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Contract farming can update own profile" ON public.contract_farming_profiles FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- Agri Waste
CREATE POLICY "Agri waste can view own profile" ON public.agri_waste_profiles FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Agri waste can insert own profile" ON public.agri_waste_profiles FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Agri waste can update own profile" ON public.agri_waste_profiles FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- Seller
CREATE POLICY "Seller can view own profile" ON public.seller_profiles FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Seller can insert own profile" ON public.seller_profiles FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Seller can update own profile" ON public.seller_profiles FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- Broker
CREATE POLICY "Broker can view own profile" ON public.broker_profiles FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Broker can insert own profile" ON public.broker_profiles FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Broker can update own profile" ON public.broker_profiles FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- Customer
CREATE POLICY "Customer can view own profile" ON public.customer_profiles FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Customer can insert own profile" ON public.customer_profiles FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Customer can update own profile" ON public.customer_profiles FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- Delivery Partner
CREATE POLICY "Delivery partner can view own profile" ON public.delivery_partner_profiles FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Delivery partner can insert own profile" ON public.delivery_partner_profiles FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Delivery partner can update own profile" ON public.delivery_partner_profiles FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
