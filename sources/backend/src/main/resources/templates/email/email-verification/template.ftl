<#import "../base-layout.ftl" as layout>
<@layout.email title="Verify Your Email Address">
    <h1>Verify Your Email Address</h1>
    
    <p>Hello,</p>
    
    <p>Thank you for signing up! To complete your registration, please verify your email address by clicking the button below:</p>
    
    <p class="text-center">
        <a href="${verificationUrl}" class="button primary">Verify Email Address</a>
    </p>
    
    <p>Or copy and paste this link into your browser:</p>
    <div class="url-fallback">${verificationUrl}</div>
    
    <p>If you didn't create an account with us, you can safely ignore this email.</p>
</@layout.email>
