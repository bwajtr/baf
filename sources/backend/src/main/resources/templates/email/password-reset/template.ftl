<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reset Your Password</title>
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
            background-color: #007bff;
            color: #ffffff !important;
            text-decoration: none;
            border-radius: 6px;
            font-weight: 600;
            margin: 20px 0;
        }
        .button:hover {
            background-color: #0056b3;
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
    </style>
</head>
<body>
    <div class="container">
        <h1>Reset Your Password</h1>
        
        <p>Hello,</p>
        
        <p>We received a request to reset the password for your account. Click the button below to create a new password:</p>
        
        <p style="text-align: center;">
            <a href="${resetUrl}" class="button">Reset Password</a>
        </p>
        
        <p>Or copy and paste this link into your browser:</p>
        <div class="url-fallback">${resetUrl}</div>
        
        <p class="note">This link will expire in ${expirationDurationMinutes} minutes for security reasons.</p>
        
        <div class="warning">
            If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.
        </div>
        
        <div class="footer">
            <p>This email was sent by ${appName}</p>
        </div>
    </div>
</body>
</html>
