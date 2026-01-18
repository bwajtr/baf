<#import "../base-layout.ftl" as layout>
<@layout.email title="You're Invited!" preheader="${inviterName} invited you to join ${organizationName}">
    <h1>You're Invited!</h1>
    
    <p>Hello,</p>
    
    <p><span class="text-primary">${inviterName}</span> has invited you to join <span class="text-success">${organizationName}</span>.</p>
    
    <div class="highlight-box">
        <p><span class="label">Organization:</span> ${organizationName}</p>
        <p><span class="label">Your Role:</span> ${role}</p>
        <p><span class="label">Invited by:</span> ${inviterName}</p>
    </div>
    
    <p>Click the button below to accept the invitation and join the organization:</p>
    
    <p class="text-center">
        <a href="${acceptanceUrl}" class="button success">Accept Invitation</a>
    </p>
    
    <p>Or copy and paste this link into your browser:</p>
    <div class="url-fallback">${acceptanceUrl}</div>
    
    <p>If you don't have an account yet, please register or sign in first—you'll be able to accept the invitation once logged in.</p>
</@layout.email>
