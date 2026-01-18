<#import "../base-layout.ftl" as layout>
<@layout.email title="Your Password Has Been Changed" preheader="Your password was successfully updated on ${changeDateTime}">
    <h1>Your Password Has Been Changed</h1>
    
    <p>Hello,</p>
    
    <p>This is a confirmation that the password for your account associated with <strong>${emailAddress}</strong> has been successfully changed.</p>
    
    <p class="note">This change was made on ${changeDateTime}.</p>
    
    <div class="warning">
        <strong>Wasn't you?</strong> If you did not make this change, your account may have been compromised. Please take immediate action:
        <ul style="margin: 8px 0 0 0; padding-left: 20px;">
            <li>Reset your password immediately</li>
            <li>Review your account activity</li>
            <li>Contact our support team if you need assistance</li>
        </ul>
    </div>
    
    <p>If you made this change, you can safely ignore this email.</p>
</@layout.email>
