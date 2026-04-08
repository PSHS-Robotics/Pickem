/*
 * This model represents the app's current login/session state.
 * It mainly answers which local user is active right now.
 */
package com.example.pickem.data.model

data class UserSession(val activeUserId: String? = null)
