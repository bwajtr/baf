<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Verify Your Email Address</title>
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
    </style>
</head>
<body>
    <div class="container">
        <h1>Verify Your Email Address</h1>
        
        <p>Hello,</p>
        
        <p>Thank you for signing up! To complete your registration, please verify your email address by clicking the button below:</p>
        
        <p style="text-align: center;">
            <a href="${verificationUrl}" class="button">Verify Email Address</a>
        </p>
        
        <p>Or copy and paste this link into your browser:</p>
        <div class="url-fallback">${verificationUrl}</div>
        
        <p>If you didn't create an account with us, you can safely ignore this email.</p>
        
        <div class="footer">
            <p>This email was sent by ${appName}</p>
        </div>
    </div>
</body>
</html>
