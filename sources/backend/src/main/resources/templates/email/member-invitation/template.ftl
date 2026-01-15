<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>You're Invited!</title>
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
            background-color: #28a745;
            color: #ffffff !important;
            text-decoration: none;
            border-radius: 6px;
            font-weight: 600;
            margin: 20px 0;
        }
        .button:hover {
            background-color: #218838;
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
        .inviter-name {
            color: #007bff;
            font-weight: 600;
        }
        .org-name {
            color: #28a745;
            font-weight: 600;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>You're Invited!</h1>
        
        <p>Hello,</p>
        
        <p><span class="inviter-name">${inviterName}</span> has invited you to join <span class="org-name">${organizationName}</span>.</p>
        
        <div class="highlight-box">
            <p><span class="label">Organization:</span> ${organizationName}</p>
            <p><span class="label">Your Role:</span> ${role}</p>
            <p><span class="label">Invited by:</span> ${inviterName}</p>
        </div>
        
        <p>Click the button below to accept the invitation and join the organization:</p>
        
        <p style="text-align: center;">
            <a href="${acceptanceUrl}" class="button">Accept Invitation</a>
        </p>
        
        <p>Or copy and paste this link into your browser:</p>
        <div class="url-fallback">${acceptanceUrl}</div>
        
        <p>If you don't have an account yet, please register or sign in first—you'll be able to accept the invitation once logged in.</p>
        
        <div class="footer">
            <p>This email was sent by ${appName}</p>
        </div>
    </div>
</body>
</html>
