<#--
  Base layout macro for all email templates.
  
  Usage:
    <#import "../base-layout.ftl" as layout>
    <@layout.email title="Email Title">
        <h1>Your Heading</h1>
        <p>Your content here...</p>
        <a href="..." class="button primary">Click Me</a>
    </@layout.email>
  
  Parameters:
    - title: The HTML document title (shown in browser tab if email is opened in browser)
    - extraStyles: Optional. Additional CSS to include in the <style> block
  
  Button classes:
    - .button.primary - Blue button (default action)
    - .button.success - Green button (positive action)
    - .button.warning - Orange button (caution action)
-->
<#macro email title="Email" extraStyles="">
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title}</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            line-height: 1.6;
            color: #333333;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background-color: #ffffff;
            border-radius: 8px;
            padding: 40px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }
        h1 {
            color: #1a1a1a;
            font-size: 24px;
            margin-bottom: 20px;
        }
        p {
            margin-bottom: 16px;
            color: #4a4a4a;
        }
        .button {
            display: inline-block;
            padding: 9px 28px;
            color: #ffffff !important;
            text-decoration: none;
            border-radius: 6px;
            font-weight: 600;
            margin: 20px 0;
        }
        .button.primary {
            background-color: #007bff;
        }
        .button.primary:hover {
            background-color: #0056b3;
        }
        .button.success {
            background-color: #28a745;
        }
        .button.success:hover {
            background-color: #218838;
        }
        .button.warning {
            background-color: #ffc107;
            color: #212529 !important;
        }
        .button.warning:hover {
            background-color: #e0a800;
        }
        .url-fallback {
            word-break: break-all;
            color: #666666;
            font-size: 14px;
            background-color: #f8f9fa;
            padding: 12px;
            border-radius: 4px;
            margin: 16px 0;
        }
        .footer {
            margin-top: 32px;
            padding-top: 20px;
            border-top: 1px solid #eeeeee;
            font-size: 12px;
            color: #888888;
        }
        .note {
            font-size: 14px;
            color: #666666;
            font-style: italic;
        }
        .warning {
            background-color: #fff3cd;
            border: 1px solid #ffc107;
            border-radius: 4px;
            padding: 12px;
            margin: 16px 0;
            font-size: 14px;
            color: #856404;
        }
        .highlight-box {
            background-color: #f8f9fa;
            border-left: 4px solid #007bff;
            padding: 16px;
            border-radius: 0 4px 4px 0;
            margin: 20px 0;
        }
        .highlight-box p {
            margin: 8px 0;
        }
        .label {
            font-weight: 600;
            color: #333333;
        }
        .text-primary {
            color: #007bff;
            font-weight: 600;
        }
        .text-success {
            color: #28a745;
            font-weight: 600;
        }
        .text-center {
            text-align: center;
        }
        ${extraStyles}
    </style>
</head>
<body>
    <div class="container">
        <#nested>
        
        <div class="footer">
            <p>This email was sent by ${appName}</p>
        </div>
    </div>
</body>
</html>
</#macro>
