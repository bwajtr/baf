package com.wajtr.baf.user.emailverification

import com.wajtr.baf.authentication.db.LOGIN_PATH
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * @author Bretislav Wajtr
 */
@Controller
class EmailVerificationController(
    private val emailVerificationService: EmailVerificationService
) {

    /**
     * This is an API endpoint used to confirm the ownership of the email address. The URL of this endpoint
     * is sent to the user (along with the token as a parameter) via email, user clicks on this URL inside the email
     * and that verifies the ownership.
     * 
     * @param key A email verification token
     */
    @GetMapping(CONFIRM_EMAIL_OWNERSHIP_URL)
    fun confirmEmailAddress(@RequestParam key: String): String {
        val result: EmailVerificationConfirmationResult = emailVerificationService.confirmEmailVerificationToken(key)
        return if (result == EmailVerificationConfirmationResult.TOKEN_VALID) {
            "redirect:/$LOGIN_PATH?confirmSuccess"
        } else {
            "redirect:/$LOGIN_PATH"
        }
    }
}
