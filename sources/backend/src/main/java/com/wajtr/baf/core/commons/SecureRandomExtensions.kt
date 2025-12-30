package com.wajtr.baf.core.commons

import java.security.SecureRandom


/**
 *
 * @author Bretislav Wajtr
 */
fun SecureRandom.generateRandomAlphanumeric(length: Long): String {
    val source = "ABCDEFGHIJKLMNOPRSTUVWXYZ123456789"
    return ints(length, 0, source.length)
        .toArray()
        .map(source::get)
        .joinToString("")
}
