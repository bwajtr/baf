package com.wajtr.baf.core.i18n

import jakarta.annotation.PostConstruct
import org.springframework.context.MessageSource
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.stereotype.Service

/**
 * This unit provides static accessors to the I18N capabilities of spring. The core functionality is handled by global singleton MessageSource bean (wrapped by a MessageSourceAccessor so a session locale is always used)
 * but there are static methods which provide access to this bean. This removes the necessity to inject MessageSource to virtually every UI class.
 *
 * MessageSource implementations are basically thread safe (they use Concurrent hashmaps but what is much more important -> we only read from the MessageSource once it's initialized) so it's OK to use static access to them
 *
 * @author Bretislav Wajtr
 */


fun i18n(code: String): String {
    return StaticMessageSourceAccessor.messageSourceAccessor.getMessage(code)
}

fun i18nWithDefault(code: String, default: String): String {
    return StaticMessageSourceAccessor.messageSourceAccessor.getMessage(code, default)
}

fun i18n(code: String, vararg args: Any): String {
    return StaticMessageSourceAccessor.messageSourceAccessor.getMessage(code, args)
}

object StaticMessageSourceAccessor {
    lateinit var messageSourceAccessor: MessageSourceAccessor
}

@Service
class StaticMessageSourceAccessorInitializer(
    private val messageSource: MessageSource
) {

    @PostConstruct
    fun init() {
        StaticMessageSourceAccessor.messageSourceAccessor = MessageSourceAccessor(messageSource)
    }

}


