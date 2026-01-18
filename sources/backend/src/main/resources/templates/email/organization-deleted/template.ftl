<#import "../base-layout.ftl" as layout>
<@layout.email title="Your Organization Has Been Deleted" preheader="${organizationName} has been permanently deleted from ${applicationName}">
    <h1>Organization Has Been Deleted</h1>
    
    <p>Hello,</p>
    
    <p>This email confirms that organization <strong>${organizationName}</strong> has been permanently deleted from ${applicationName}.</p>
    
    <p class="note">This action was completed on ${deletionDateTime}.</p>
    
    <p>All organization data, including members, settings, and associated records, has been removed from our systems. This action cannot be undone.</p>

    <p>Thank you for being part of ${applicationName}.</p>
</@layout.email>
