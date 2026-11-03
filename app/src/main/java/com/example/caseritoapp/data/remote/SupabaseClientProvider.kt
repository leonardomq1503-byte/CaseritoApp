package com.example.caseritoapp.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {

    const val SUPABASE_URL = "https://nauxzjgubuyxvzpvyjaw.supabase.co"
    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5hdXh6amd1YnV5eHZ6cHZ5amF3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTM3MzE5MDAsImV4cCI6MjEwOTMwNzkwMH0.LnfxEfAJOSmcBQ9a59fa5JYcDDmOQ2vaMdLK4kGp3x4"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Auth)
        install(Storage)
    }
}
