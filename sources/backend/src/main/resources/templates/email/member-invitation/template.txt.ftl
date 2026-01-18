<#import "../base-layout-text.ftl" as layout>
<@layout.email>
You're Invited!
===============

Hello,

${inviterName} has invited you to join ${organizationName}.

Organization: ${organizationName}
Your Role: ${role}
Invited by: ${inviterName}

Click the link below to accept the invitation and join the organization:

${acceptanceUrl}

If you don't have an account yet, please register or sign in first—you'll be able to accept the invitation once logged in.

Best regards,
${appName}
</@layout.email>