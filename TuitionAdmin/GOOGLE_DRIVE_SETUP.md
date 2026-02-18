# Google Drive Integration Setup Guide

## Overview
This guide will help you set up Google Drive integration for persistent cloud backup of your tuition administration data.

## Step-by-Step Setup

### Step 1: Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Click on the project dropdown at the top
3. Click "NEW PROJECT"
4. Enter project name: `Tuition Admin App`
5. Click "CREATE"
6. Wait for the project to be created (2-3 minutes)

### Step 2: Enable Google Drive API

1. In the Cloud Console, search for "Google Drive API"
2. Click on "Google Drive API"
3. Click "ENABLE"
4. You may see a prompt to configure the OAuth consent screen - click "CONFIGURE CONSENT SCREEN"

### Step 3: Configure OAuth Consent Screen

1. Select "External" for User Type
2. Click "CREATE"
3. Fill in the form:
   - **App name:** Tuition Admin Application
   - **User support email:** Your email
   - **Developer contact:** Your email
4. Click "SAVE AND CONTINUE"
5. Click "ADD OR REMOVE SCOPES"
6. Search for and select these scopes:
   - `https://www.googleapis.com/auth/drive.file`
7. Click "UPDATE"
8. Click "SAVE AND CONTINUE"
9. On the next page, click "SAVE AND CONTINUE" again
10. Click "BACK TO DASHBOARD"

### Step 4: Create OAuth 2.0 Credentials

1. In the Cloud Console, go to "Credentials" (left sidebar)
2. Click "CREATE CREDENTIALS"
3. Select "OAuth client ID"
4. Choose "Desktop application" as Application type
5. Enter name: `Tuition Admin Desktop`
6. Click "CREATE"
7. A dialog box will appear with your Client ID - **COPY THIS**

### Step 5: Add Client ID to the App

1. Open the Tuition Admin webpage
2. Make sure you're logged in as **Admin**
3. Go to the "Backup & Sync" tab
4. In the "Setup Google Drive" section, paste your Client ID
5. Click "Save Client ID"
6. You should see a success message

### Step 6: Connect to Google Drive

1. Click "🔗 Connect Google Drive"
2. A Google Sign-In window will open
3. Sign in with your Google account
4. Grant the required permissions
5. You should see "✅ Connected to Google Drive" message

## Using Backup & Sync

### Backup Your Data
- Click "💾 Backup to Google Drive"
- Your data will be saved to your Google Drive
- You'll see the backup completion message

### Restore Your Data
- Login from a different device
- Go to "Backup & Sync" tab
- Click "📥 Restore from Google Drive"
- Your data will be restored

### Download Local Backup
- Click "⬇️ Download Local Backup"
- A JSON file will download to your computer
- Keep this safe as a backup

## Important Notes

### ✅ What Students & Teachers See
- Nothing! They only login with their credentials
- No changes to their experience
- All backup happens transparently

### ⚠️ Important Reminders
- **Only Admin** needs to connect Google Drive
- Connection is one-time setup
- Data is backed up to your personal Google Drive
- Students and Teachers are unaffected
- Backup happens when you click the button (not automatic yet)

### 🔒 Security
- Uses OAuth 2.0 (industry standard)
- Your credentials are not stored in the app
- Only admin can backup/restore
- Backups are stored in your Google Drive

## Troubleshooting

### "Please setup your Google Cloud Client ID first"
- You need to create a Google Cloud Project first
- Follow Steps 1-5 above

### "Failed to sign in with Google"
- Check that you enabled Google Drive API
- Make sure you configured OAuth consent screen
- Check your Client ID is correct
- Clear browser cache and try again

### "No backup file found"
- You haven't backed up yet
- Click "Backup to Google Drive" first

### Still having issues?
- Check browser console (F12) for error messages
- Make sure Google Drive API is enabled in Cloud Console
- Verify your Client ID is correct (should be very long)

## Testing

1. **First Time Setup:**
   - Add some students/teachers
   - Go to Backup & Sync tab
   - Click Setup and paste Client ID
   - Click Connect Google Drive
   - Click Backup to Google Drive

2. **Test Restore:**
   - Open the app in a new browser window (private/incognito)
   - Login as admin
   - Go to Backup & Sync
   - Click Setup again with same Client ID
   - Connect to Google Drive
   - Click Restore - your data should appear!

3. **Local Backup:**
   - Click "Download Local Backup"
   - Keep the JSON file safe
   - Can be used to restore manually if needed

## What's Backed Up?
- ✅ All student records (name, email, phone, fees, status, etc.)
- ✅ All teacher records (name, email, subject, salary, etc.)
- ✅ All user credentials (passwords)
- ✅ Settings and configurations
- ❌ Not: Login history or audit logs (for now)

## Next Steps

After setup, you can:
1. Add more students/teachers
2. Regularly backup to Google Drive
3. Restore from any device with internet
4. Download local backups for extra safety
