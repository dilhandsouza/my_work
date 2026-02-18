# Quick Reference Card - Tuition Admin v3.0

## What's New?

### 🎯 Auto-Role Detection
- Login now automatically detects your role from username
- No need to manually select role anymore
- Faster and simpler login experience

### 👥 Custom Admin Accounts
- Create multiple admin accounts with different usernames
- Each admin can have their own password
- All admins have full access
- Demo admin cannot be deleted

### 💾 Enhanced Security
- Password persistence for all account types
- Unique username validation
- System admin protection

---

## How to Use

### Login
1. Enter username and password
2. Click "Login"
3. **Role automatically detected** - you're in!

### Create Custom Admin (Admin Only)
1. Login as admin
2. Go to "Admin Accounts" tab
3. Fill form:
   - Full Name: `Manager One`
   - Username: `manager1` (must be unique)
   - Password: `manager123` (minimum 6 chars)
   - Email: `manager1@example.com` (optional)
4. Click "Create Admin"
5. New admin appears in Admin Accounts table

### View All Admin Accounts (Admin Only)
1. Go to "Admin Accounts" tab
2. Table shows:
   - System Admin (demo) - marked "System Account" (cannot delete)
   - Custom Admins - marked "Custom Admin" (can delete)

### Delete Custom Admin (Admin Only)
1. Go to "Admin Accounts" tab
2. Find custom admin in table
3. Click "Delete" button
4. Confirm deletion
5. Admin account removed

### Record Student Payment (Student Only)
1. Login as student
2. Click "Record Payment" button
3. Enter:
   - Amount
   - Payment Method (Cash/Check/Online/Demand Draft)
4. Click "Submit Payment"
5. Fees updated automatically

### Change Password (All Users)
1. First login: Modal appears automatically
2. Other times: Look for "Change Password" option
3. Enter old password
4. Enter new password (minimum 6 chars)
5. Confirm new password
6. Click "Change Password"

---

## Demo Accounts

| User | Username | Password | Role |
|------|----------|----------|------|
| Admin | admin | admin123 | System Admin |
| Teacher 1 | teacher1 | teach123 | Teacher |
| Teacher 2 | teacher2 | teach456 | Teacher |
| Student 1 | student1 | stud123 | Student |
| Student 2 | student2 | pass123 | Student |

---

## Common Tasks

### For Admin

**Manage Students:**
1. Click "Students" tab
2. Add: Fill form → Click "Add Student"
3. Edit: Click edit icon → Update → Save
4. Delete: Click delete icon → Confirm

**Manage Teachers:**
1. Click "Teachers" tab
2. Same as students

**Manage Admins:**
1. Click "Admin Accounts" tab
2. Create: Fill form → Click "Create Admin"
3. Delete: Click "Delete" button on custom admin
4. View: Table shows all (demo + custom)

**Backup Data:**
1. Click "Backup & Sync" tab
2. Click "Connect Google Drive" (one time)
3. Click "Backup Now" whenever needed
4. Click "Restore" to get data back

---

### For Teacher

1. View your profile information
2. See all students in your class
3. Logout when done

---

### For Student

1. View your profile
2. Check fees status
3. Record payment
4. View payment history
5. Logout when done

---

## Important Notes

✅ **Data Location:**
- All data stored in browser's localStorage
- Persists even after closing browser
- Lost if browser cache is cleared

✅ **Username Rules:**
- Must be unique across all account types
- Cannot duplicate between admin/teacher/student
- Case-sensitive
- System prevents duplicates on creation

✅ **Password Rules:**
- Minimum 6 characters
- Case-sensitive
- Stored in browser (not encrypted for client-side only app)
- Each user has separate password

✅ **Admin Account Types:**
- **System Admin** (Demo): admin / admin123 - Cannot delete
- **Custom Admin**: Created by admin - Can delete

✅ **Role Auto-Detection:**
- System automatically finds username in:
  1. Admin accounts (demo first, then custom)
  2. Teacher accounts
  3. Student accounts
- Role assigned based on first match
- Faster than manual selection

---

## Troubleshooting

**Can't login?**
- Check username and password are correct (case-sensitive)
- Try with demo account: admin / admin123
- Clear browser cache if using old credentials

**Custom admin not showing in table?**
- Refresh page (F5)
- Check console (F12) for errors
- Verify admin was created successfully (alert appeared)

**Password change not working?**
- Verify you entered old password correctly
- Ensure new password is minimum 6 characters
- Try logout and login with new password

**Data disappeared?**
- Check if you cleared browser cache
- Restore from Google Drive backup if available
- Demo data will reload on next visit

---

## Technical Info

**Technology Stack:**
- HTML5 + CSS3 + JavaScript (no external libraries)
- Browser localStorage for data storage
- Google Drive API (optional) for cloud backup
- Client-side only (no server needed)

**Browser Support:**
- Chrome, Firefox, Safari, Edge (latest versions)
- Mobile responsive design

**File Sizes:**
- index.html: ~17 KB
- styles.css: ~30 KB
- script.js: ~45 KB
- Total: ~92 KB

---

## Need Help?

1. **Getting Started?** Read README.md
2. **Testing?** Use TESTING_GUIDE.md for step-by-step tests
3. **Technical Details?** Check IMPLEMENTATION_SUMMARY.md
4. **Google Drive Setup?** See GOOGLE_DRIVE_SETUP.md

---

**Version:** 3.0
**Last Updated:** 2024
**Status:** Production Ready
