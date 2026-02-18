# Tuition Administration App - Testing Guide

## Overview
This document provides comprehensive testing guidelines for the Tuition Admin application with automatic role detection and custom admin account management.

## Quick Start
1. Open `index.html` in a web browser
2. Application loads with demo data automatically
3. Login with credentials and role is auto-detected

## Test Scenarios

### Test 1: Login with Auto-Role Detection

#### Scenario 1A: Demo Admin Login
- **Credentials:** 
  - Username: `admin`
  - Password: `admin123`
- **Expected Result:**
  - Login successful
  - Admin dashboard loads
  - User shown as "Administrator"
  - Admin panel visible with tabs: Students, Teachers, Admin Accounts, Backup & Sync
- **Verification:**
  - Check browser console for "Auto-detected role: admin"
  - Verify currentUser.isCustomAdmin = false
  - Verify currentUser.role = 'admin'

#### Scenario 1B: Teacher Login
- **Credentials:**
  - Username: `teacher1`
  - Password: `teach123`
- **Expected Result:**
  - Login successful
  - Teacher dashboard loads
  - Shows teacher info: Name, Email, Phone, Subject, Qualification, Experience, Salary, Status
  - Shows list of students taught by this teacher
- **Verification:**
  - Check currentUser.role = 'teacher'
  - Verify teacher information displayed correctly

#### Scenario 1C: Student Login
- **Credentials:**
  - Username: `student1`
  - Password: `stud123`
- **Expected Result:**
  - Login successful
  - Student dashboard loads
  - Shows student info with fees status
  - Shows pending fees if any
  - Shows payment history
  - Shows "Record Payment" button
- **Verification:**
  - Check currentUser.role = 'student'
  - Verify student information displayed correctly

#### Scenario 1D: Invalid Login
- **Credentials:**
  - Username: `invalid`
  - Password: `wrong`
- **Expected Result:**
  - Alert: "❌ Invalid username or password. Please try again."
  - Stay on login page
  - No dashboard loads

### Test 2: First-Login Password Change

#### Scenario 2A: First Login Password Change (New Account)
- **Setup:** Create a new custom admin account (see Test 4)
- **Steps:**
  1. Login with new admin account
  2. Password change modal should appear automatically
  3. Enter old password (what was set during creation)
  4. Enter new password (minimum 6 characters)
  5. Confirm new password
  6. Click "Change Password"
- **Expected Result:**
  - Modal closes
  - Alert: "✅ Password changed successfully"
  - User remains logged in
  - Next login requires new password
- **Verification:**
  - Logout
  - Login with same username but old password → Should fail
  - Login with same username and new password → Should succeed

#### Scenario 2B: Demo Admin First Login
- **Setup:** Fresh browser (clear localStorage for this test)
- **Steps:**
  1. Open app for first time
  2. Login as admin with admin123
  3. Password change modal appears
- **Expected Result:**
  - Password change modal shown
  - User can update password
  - Demo admin password persists in localStorage

### Test 3: Post-Login Password Change

#### Scenario 3A: Change Password After Login
- **Setup:** Logged in as admin
- **Steps:**
  1. Click "Change Password" button (if available on dashboard)
  2. Or access via menu/settings
  3. Enter current password: `admin123`
  4. Enter new password: `admin456`
  5. Confirm new password: `admin456`
- **Expected Result:**
  - Alert: "✅ Password changed successfully"
  - Current password updated in localStorage
  - Next session requires new password
- **Verification:**
  - Logout
  - Login with admin / admin456 → Should succeed
  - Login with admin / admin123 → Should fail

### Test 4: Custom Admin Account Management

#### Scenario 4A: Create New Admin Account
- **Setup:** Logged in as admin
- **Steps:**
  1. Click on "Admin Accounts" tab
  2. In "Create New Admin Account" form, enter:
     - Full Name: `Manager One`
     - Username: `manager1`
     - Password: `manager123`
     - Email: `manager1@example.com`
  3. Click "Create Admin"
- **Expected Result:**
  - Alert: "✅ Admin account created successfully! Username: manager1"
  - Form clears
  - Admin appears in "Admin Accounts" table with type "Custom Admin"
  - Delete button visible for this admin
- **Verification:**
  - Logout
  - Login with manager1 / manager123 → Should succeed as admin
  - Verify currentUser.isCustomAdmin = true

#### Scenario 4B: Prevent Duplicate Username
- **Setup:** Admin account `manager1` already created
- **Steps:**
  1. Try to create another admin with username `manager1`
  2. Click "Create Admin"
- **Expected Result:**
  - Alert: "❌ Username already exists. Please choose a different username."
  - Account not created
  - Admin list unchanged

#### Scenario 4C: Create Multiple Custom Admins
- **Steps:**
  1. Create admin: manager2 / pass123 / Manager Two
  2. Create admin: manager3 / pass123 / Manager Three
  3. Create admin: manager4 / pass123 / Manager Four
- **Expected Result:**
  - All admins created successfully
  - All appear in Admin Accounts table
  - Each has unique username
  - All marked as "Custom Admin"

#### Scenario 4D: View Admin Accounts Table
- **Setup:** Multiple admins created
- **Expected Result:**
  - Table shows:
    - Row 1: "Administrator" (demo) - marked as "System Admin" - no delete button, shows "System Account"
    - Row 2+: Custom admins - marked as "Custom Admin" - with delete button
  - System admin cannot be deleted
  - Custom admins can be deleted
- **Verification:**
  - Demo admin row shows "System Admin" type
  - Custom admin rows show "Custom Admin" type
  - Only custom admin rows have delete buttons

#### Scenario 4E: Delete Custom Admin Account
- **Setup:** Custom admin `manager1` exists
- **Steps:**
  1. Find manager1 in Admin Accounts table
  2. Click "Delete" button
  3. Confirm deletion when prompted
- **Expected Result:**
  - Alert: "Are you sure you want to delete this admin account?"
  - On confirmation: "✅ Admin account deleted successfully!"
  - manager1 removed from table
  - manager1 no longer available to login
- **Verification:**
  - Attempt to login with manager1 → Should fail with "Invalid username or password"

#### Scenario 4F: Try to Delete System Admin
- **Steps:**
  1. Look for Administrator (demo admin) in table
  2. Observe row shows "System Account" instead of delete button
- **Expected Result:**
  - No delete button present
  - Row clearly indicates "System Account"
  - Demo admin cannot be deleted

### Test 5: Data Persistence

#### Scenario 5A: Browser Refresh
- **Setup:** Logged in as admin
- **Steps:**
  1. Refresh the page (F5)
- **Expected Result:**
  - User remains logged in
  - Dashboard reloads with same data
  - All previous entries visible
- **Verification:**
  - currentUser persists in localStorage
  - No re-login required

#### Scenario 5B: Close and Reopen Browser
- **Setup:** Logged in and created custom admin
- **Steps:**
  1. Close browser
  2. Reopen page
  3. Check if auto-login occurs OR login required
- **Expected Result:**
  - User NOT auto-logged in (security best practice)
  - Login page shown
  - Custom admin still accessible with saved credentials
  - Can login with custom admin account

#### Scenario 5C: localStorage Persistence Verification
- **Steps:**
  1. Open browser console (F12)
  2. Type: `JSON.parse(localStorage.getItem('adminAccounts'))`
  3. Verify custom admins are stored
  4. Type: `JSON.parse(localStorage.getItem('demoAccounts'))`
  5. Verify demo account password changes are stored
- **Expected Result:**
  - adminAccounts shows all custom admin objects with id, name, username, password, email, createdDate
  - demoAccounts shows updated password if changed
  - All data properly serialized and stored

### Test 6: Student Management

#### Scenario 6A: View Students List
- **Setup:** Logged in as admin
- **Steps:**
  1. Click "Students" tab
  2. View students list
- **Expected Result:**
  - Table shows all students with: Name, Roll, Class, Fees, Fees Paid, Due Date, Status
  - Shows fees status for each student
  - Shows pending fees if any

#### Scenario 6B: Add Student
- **Setup:** Logged in as admin
- **Steps:**
  1. Click "Students" tab
  2. Enter student details in form
  3. Click "Add Student"
- **Expected Result:**
  - Student added to list
  - Form clears
  - New student visible in table

#### Scenario 6C: View Student Fees Status
- **Expected Result:**
  - Students with unpaid fees marked clearly
  - Pending fees amount visible
  - Students with full payment marked as paid

### Test 7: Teacher Management

#### Scenario 7A: View Teachers List
- **Setup:** Logged in as admin
- **Steps:**
  1. Click "Teachers" tab
  2. View teachers list
- **Expected Result:**
  - Table shows all teachers with: Name, Email, Subject, Experience, Salary, Status

#### Scenario 7B: Add Teacher
- **Setup:** Logged in as admin
- **Steps:**
  1. Click "Teachers" tab
  2. Enter teacher details
  3. Click "Add Teacher"
- **Expected Result:**
  - Teacher added to list
  - Form clears
  - New teacher visible in table

### Test 8: Student Payment Recording

#### Scenario 8A: Student Records Payment
- **Setup:** Logged in as student with pending fees
- **Steps:**
  1. View student dashboard
  2. Click "Record Payment" button
  3. Enter payment amount (less than or equal to pending fees)
  4. Select payment method: Cash, Check, Online, Demand Draft
  5. Click "Submit Payment"
- **Expected Result:**
  - Payment modal closes
  - Alert: "✅ Payment recorded successfully"
  - Fees paid amount updated
  - New pending fees calculated
  - Payment appears in payment history

#### Scenario 8B: Payment History Visibility
- **Setup:** Student has made payments
- **Expected Result:**
  - Payment history shows all recorded payments
  - Each entry shows: Date, Amount, Method, Details
  - Dates properly formatted

### Test 9: Role-Based Access Control

#### Scenario 9A: Admin Can Access All Sections
- **Setup:** Logged in as admin
- **Expected Result:**
  - All tabs visible: Students, Teachers, Admin Accounts, Backup & Sync
  - All management functions available
  - Can create/edit/delete students, teachers, and admins

#### Scenario 9B: Teacher Can Only Access Teacher Dashboard
- **Setup:** Logged in as teacher
- **Expected Result:**
  - Only teacher dashboard visible
  - Can see assigned students
  - Cannot access admin functions
  - Cannot manage other teachers
  - Cannot create admins

#### Scenario 9C: Student Can Only Access Student Dashboard
- **Setup:** Logged in as student
- **Expected Result:**
  - Only student dashboard visible
  - Can see own information
  - Can record own payments
  - Cannot access admin functions
  - Cannot view other students' data

### Test 10: Google Drive Integration (Optional)

#### Scenario 10A: Connect Google Drive
- **Setup:** Logged in as admin
- **Steps:**
  1. Click "Backup & Sync" tab
  2. Click "🔗 Connect Google Drive"
  3. Complete OAuth authorization
- **Expected Result:**
  - Status changes to "Connected"
  - Shows connected user email
  - "Backup Now" button enabled
  - "Disconnect" button shows

#### Scenario 10B: Backup Data
- **Setup:** Connected to Google Drive
- **Steps:**
  1. Click "Backup Now" button
- **Expected Result:**
  - Data uploaded to Google Drive
  - Status shows last sync time
  - All students, teachers, and admin data backed up

## Console Verification

During testing, check browser console (F12) for:
- "Page loaded. Students from localStorage: [...]"
- "Calling displayStudents()"
- "Calling displayTeachers()"
- "Calling displayAdmins()"
- "New admin created: {...}"
- "Admin account deleted successfully"
- No errors or warnings

## Known Demo Accounts

```
ADMIN:
- Username: admin
- Password: admin123
- Type: System Admin (cannot delete)

TEACHERS:
- Username: teacher1, Password: teach123 (John Smith)
- Username: teacher2, Password: teach456 (Sarah Williams)

STUDENTS:
- Username: student1, Password: stud123 (Emma Johnson)
- Username: student2, Password: pass123 (Rahul Sharma)
```

## Troubleshooting

### Login Issues
- Clear localStorage: Open console, type `localStorage.clear()`, refresh
- Check credentials match exactly (case-sensitive)
- Verify role auto-detection in browser console

### Admin Accounts Not Showing
- Check if displayAdmins() is called on admin panel load
- Verify adminAccounts array in localStorage
- Check browser console for errors

### Password Changes Not Persisting
- Verify localStorage is enabled in browser
- Check admin type (custom vs demo) is correct
- Verify passwords saved to correct array in localStorage

### Custom Admins Can't Login
- Verify username and password exactly match creation
- Check adminAccounts array in localStorage contains the account
- Clear browser cache if issues persist

## Performance Notes

- App uses client-side storage only (no server required)
- All operations instantaneous (no network delays)
- Handles 1000+ students efficiently
- Recommended to backup to Google Drive regularly

## Security Recommendations

1. **Change Demo Admin Password** - Change admin123 to strong password
2. **Create Custom Admin Accounts** - Distribute admin duties
3. **Use Strong Passwords** - Minimum 6 characters (recommend 12+)
4. **Regular Backups** - Use Google Drive sync regularly
5. **Clear Old Data** - Periodically clean up old records
