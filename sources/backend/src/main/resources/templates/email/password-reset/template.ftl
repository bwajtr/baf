<#import "../base-layout.ftl" as layout>
<@layout.email title="Reset Your Password" preheader="Reset your password to regain access to your account">
    <h1>Reset Your Password</h1>
    
    <p>Hello,</p>
    
    <p>We received a request to reset the password for your account. Click the button below to create a new password:</p>
    
    <p class="text-center">
        <a href="${resetUrl}" class="button primary"><span>Reset Password</span></a>
    </p>
    
    <p>Or copy and paste this link into your browser:</p>
    <div class="url-fallback">${resetUrl}</div>
    
    <p class="note">This link will expire in ${expirationDurationMinutes} minutes for security reasons.</p>
    
    <div class="warning">
        If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.
    </div>
</@layout.email>
