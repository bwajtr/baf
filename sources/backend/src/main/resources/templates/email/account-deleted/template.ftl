<#import "../base-layout.ftl" as layout>
<@layout.email title="Your Account Has Been Deleted">
    <h1>Your Account Has Been Deleted</h1>
    
    <p>Hello,</p>
    
    <p>This email confirms that your account associated with <strong>${emailAddress}</strong> has been permanently deleted from ${applicationName}.</p>
    
    <p class="note">This action was completed on ${deletionDateTime}.</p>
    
    <p>All your personal data has been removed from our systems. This action cannot be undone.</p>
    
    <div class="warning">
        <strong>Wasn't you?</strong> If you did not request this deletion, please contact our support team immediately as your account may have been compromised.
    </div>
    
    <p>We're sorry to see you go. If you ever decide to come back, you're always welcome to create a new account.</p>
    
    <p>Thank you for being part of ${applicationName}.</p>
</@layout.email>
