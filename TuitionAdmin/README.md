# Tuition Class Administration System

A complete web-based administration system for managing tuition classes with student and teacher data entry, user authentication, and cloud backup.

## ✨ Features

### 📚 Student Management
- **Add Students**: Enter student name, roll number, email, phone, class, monthly fees, and status
- **View Students**: Display all students in a sortable table
- **Edit Students**: Update student information
- **Delete Students**: Remove student records
- **Search**: Filter students by name or roll number
- **Status Tracking**: Track Active, Inactive, and On Leave statuses

### 👨‍💼 Admin Account Management (NEW!)
- **Create Admin Accounts**: Admins can create additional admin accounts with custom usernames
- **Admin Dashboard**: Dedicated tab to view all admin accounts (system + custom)
- **Account Types**: 
  - System Admin (demo) - Cannot be deleted
  - Custom Admin - Can be created and deleted as needed
- **Password Management**: Each admin account has separate password
- **Unique Usernames**: System prevents duplicate usernames across all accounts
- **Admin Permissions**: All admins (system and custom) have full management access
- **Account Details**: Track admin name, username, email, and account type
- **Add Teachers**: Enter teacher name, email, phone, subject, qualification, experience, salary, and status
- **View Teachers**: Display all teachers in a sortable table
- **Edit Teachers**: Update teacher information
- **Delete Teachers**: Remove teacher records
- **Search**: Filter teachers by name or subject
- **Status Tracking**: Track Active, Inactive, and On Leave statuses

### 🔐 Authentication & Security
- **Auto-Role Detection**: Role automatically detected from username at login (no manual selection)
- **Role-Based Login**: Separate login for Admin, Teachers, and Students
- **Password Management**: Secure password storage with first-login password change
- **Session Management**: Automatic session handling with logout
- **User Credentials**: Each user has unique login credentials
- **Demo Accounts**: Pre-configured demo accounts for testing
- **Custom Admin Accounts**: Create multiple admin profiles with different usernames
- **Admin Account Management**: Create, view, and delete custom admin accounts
- **System Admin Protection**: Demo admin cannot be deleted (system-protected)

### ☁️ Cloud Backup & Sync (NEW!)
- **Google Drive Integration**: Backup all data to personal Google Drive
- **Admin-Only Setup**: Only admin needs to connect Google Drive
- **Automatic Sync**: Option to restore data on any device
- **Local Backup**: Download data as JSON file for extra security
- **One-Time Setup**: Configure once and use forever
- **Zero Impact on Users**: Students & Teachers unaffected

### 💾 Data Persistence
- **Browser Storage**: Data stored in browser's localStorage
- **Google Drive Backup**: Optional cloud backup with Google Drive
- **Local Download**: Export data as JSON backup file
- **Session Persistence**: Data persists across browser sessions

### 💰 Student Fees Management (NEW!)
- **Fees Tracking**: Monitor total fees, paid amount, and pending dues
- **Payment Recording**: Students can record their own payments
- **Payment Methods**: Support Cash, Check, Online, and Demand Draft
- **Payment History**: View all recorded payments with dates
- **Automatic Calculation**: Pending fees calculated automatically
- **Status Indicators**: Visual indicators for paid and pending fees
- **Admin View**: Admins see all student fees in one dashboard

### 📱 Student Dashboard (NEW!)
- **Modern, Responsive Design**: Works on desktop, tablet, and mobile
- **Role-Based Dashboards**: Different views for Admin, Teachers, and Students
- **Tabbed Interface**: Easy navigation between sections
- **Color-Coded Status**: Visual indicators for different statuses
- **Search Functionality**: Quick filtering of records

## 🔑 User Roles

The application automatically detects the user's role based on their username during login. No manual role selection is needed!

**How Auto-Role Detection Works:**
1. Enter username and password
2. System checks username against:
   - Admin accounts (demo admin + any custom admins)
   - Teacher accounts
   - Student accounts
3. Role is automatically assigned based on where username is found
4. Login proceeds with correct dashboard

### Admin
- Full access to manage all students and teachers
- Add, edit, delete records
- Create and manage custom admin accounts
- Configure Google Drive backup
- Access backup & sync features
- **Demo Login**: admin / admin123

### Admin (Custom)
- Created by system admin through Admin Accounts tab
- Full access same as system admin
- Can create additional admins
- Account type shown as "Custom Admin"
- Can be deleted by system admin
- **Example**: manager1 / manager123 (after creation)

### Teacher
- View personal profile and information
- See list of all students
- Cannot modify data
- Personal dashboard
- **Demo Login**: teacher1 / teach123

### Student
- View personal profile and enrollment
- See class information
- View number of classmates
- Record own payments
- View payment history
- Personal dashboard
- **Demo Login**: student1 / stud123

## 🚀 Getting Started

### Installation
1. Clone or download the files
2. Open `index.html` in a web browser
3. Login with demo credentials
4. (Optional) Setup Google Drive backup using `GOOGLE_DRIVE_SETUP.md`

### First Login
- You'll be prompted to change your password on first login
- Create a strong password (minimum 6 characters)
- Or skip to use the default password

### Adding Data
1. **As Admin**: Go to Students or Teachers tab
2. Fill in the form with required information
3. Include login credentials (username/password) for each user
4. Click "Add Student" or "Add Teacher"

### Creating Custom Admin Accounts (NEW!)
1. **Login as Admin**: Use admin / admin123
2. **Go to Admin Accounts Tab**: Navigate to the "Admin Accounts" section
3. **Fill in Admin Details**:
   - Full Name: Name of the new admin
   - Username: Unique username for login
   - Password: Minimum 6 characters (secure password recommended)
   - Email: Optional email address
4. **Click "Create Admin"**: New admin account created and visible in the admin table
5. **Verify Accounts**: All admins (demo + custom) appear in the "Admin Accounts" table
6. **Delete if Needed**: Custom admins can be deleted using the Delete button (demo admin cannot be deleted)

### Recording Student Payments
1. **Login as Student**: Use student / stud123
2. **Go to Student Dashboard**: View personal information and fees
3. **Click "Record Payment"**: Open payment modal
4. **Enter Payment Details**:
   - Amount: Payment amount (up to pending fees)
   - Payment Method: Select Cash, Check, Online, or Demand Draft
5. **Submit Payment**: Payment recorded and fees updated
6. **View History**: All payments visible in payment history section

### Cloud Backup Setup
1. See [GOOGLE_DRIVE_SETUP.md](GOOGLE_DRIVE_SETUP.md) for detailed instructions
2. Takes 10-15 minutes to set up Google Cloud Project
3. One-time configuration required
4. Then easily backup/restore from any device

## Student Fields
- Name
- Roll Number
- Email
- Phone
- Class
- Monthly Fees
- Status (Active/Inactive/On Leave)

## Teacher Fields
- Name
- Email
- Phone
- Subject
- Qualification
- Experience (in years)
- Monthly Salary
- Status (Active/Inactive/On Leave)

## Technical Details

- **HTML5**: Semantic markup
- **CSS3**: Modern styling with gradients and animations
- **JavaScript (ES6+)**: Client-side data management
- **localStorage**: Browser-based data persistence
- **Responsive Design**: Mobile-first approach

## Installation

Simply extract the files and open `index.html` in your web browser. No installation or server setup required.

## Files Included
- `index.html` - Main webpage structure
- `styles.css` - Styling and layout
- `script.js` - Functionality and data management
- `README.md` - This file

## Browser Support
- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Notes
- Data is stored in browser's localStorage
- Clear browser data will delete all stored information
- Export data regularly if you need backup
