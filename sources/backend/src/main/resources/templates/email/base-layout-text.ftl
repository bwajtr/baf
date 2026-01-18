<#--
  Base layout macro for plain text email templates.
  
  Usage:
    <#import "../base-layout-text.ftl" as layout>
    <@layout.email>
        Your plain text content here...
    </@layout.email>
  
  This macro provides a consistent footer for all plain text emails with:
  - Explanation of why the email was received
  - Link to manage email preferences
  - Company information for legal compliance
-->
<#macro email>
<#nested>

---

You received this email because you have an account with ${appName}. 
This is a transactional email required for your account security and functionality.
Manage your email preferences: [link placeholder]

${appName} is operated by ${companyName}, ${companyAddress}
</#macro>
