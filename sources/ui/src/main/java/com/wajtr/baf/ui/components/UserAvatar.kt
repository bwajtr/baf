package com.wajtr.baf.ui.components

import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.init
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.avatar.Avatar
import kotlin.math.abs

class UserAvatar(username: String) : Avatar(username) {

    init {
        colorIndex = getColorIndexForName(username)
    }

    private fun getColorIndexForName(name: String): Int {
        // Uses Kotlin's built-in hashCode() to hash the string
        // Uses modulo 10 to get a value between 0-9
        return abs(name.hashCode()) % 10
    }
}

@VaadinDsl
fun (@VaadinDsl HasComponents).userAvatar(name: String, block: (@VaadinDsl Avatar).() -> Unit = {}): Avatar {
    val avatar = UserAvatar(name)
    return init(avatar, block)
}