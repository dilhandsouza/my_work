// Initialize data on page load
let students = JSON.parse(localStorage.getItem('students')) || [];
let teachers = JSON.parse(localStorage.getItem('teachers')) || [];
let adminAccounts = JSON.parse(localStorage.getItem('adminAccounts')) || [];
let passwordChangedUsers = JSON.parse(localStorage.getItem('passwordChangedUsers')) || [];
let currentUser = null;
let currentRole = 'admin';
let editingStudentId = null;
let editingTeacherId = null;

// Demo credentials - stored in localStorage to persist password changes
let demoAccounts = {
    admin: { username: 'admin', password: 'admin123', name: 'Administrator' },
    teacher: { username: 'teacher1', password: 'teach123', name: 'John Smith' },
    student: { username: 'student1', password: 'stud123', name: 'Emma Johnson' }
};

console.log('Page loaded. Students from localStorage:', students);
console.log('Students count:', students.length);

document.addEventListener('DOMContentLoaded', function() {
    // Load demo accounts with any password changes from localStorage
    const savedDemoAccounts = localStorage.getItem('demoAccounts');
    if (savedDemoAccounts) {
        try {
            demoAccounts = JSON.parse(savedDemoAccounts);
        } catch (e) {
            console.log('Could not load saved demo accounts, using defaults');
        }
    }
    
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
        const userData = JSON.parse(storedUser);
        login(userData.username, userData.password, userData.role);
    }
    initializeDemoData();
});

function initializeDemoData() {
    if (students.length === 0) {
        students = [
            {
                id: 1,
                name: 'Emma Johnson',
                roll: 'STU001',
                email: 'emma@example.com',
                phone: '9876543210',
                class: '10-A',
                fees: 5000,
                feesPaid: 5000,
                dueDate: '2026-03-15',
                status: 'Active',
                username: 'student1',
                password: 'stud123'
            },
            {
                id: 2,
                name: 'Rahul Sharma',
                roll: 'STU002',
                email: 'rahul@example.com',
                phone: '9876543211',
                class: '10-A',
                fees: 5000,
                feesPaid: 2500,
                dueDate: '2026-02-28',
                status: 'Active',
                username: 'student2',
                password: 'pass123'
            }
        ];
        localStorage.setItem('students', JSON.stringify(students));
    }

    if (teachers.length === 0) {
        teachers = [
            {
                id: 1,
                name: 'John Smith',
                email: 'john@example.com',
                phone: '9876543212',
                subject: 'Mathematics',
                qualification: 'B.Tech, M.Tech',
                experience: 8,
                salary: 50000,
                status: 'Active',
                username: 'teacher1',
                password: 'teach123'
            },
            {
                id: 2,
                name: 'Sarah Williams',
                email: 'sarah@example.com',
                phone: '9876543213',
                subject: 'English',
                qualification: 'M.A, B.Ed',
                experience: 5,
                salary: 45000,
                status: 'Active',
                username: 'teacher2',
                password: 'teach456'
            }
        ];
        localStorage.setItem('teachers', JSON.stringify(teachers));
    }
}

// ==================== AUTHENTICATION ====================
function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    login(username, password);
}

function login(username, password) {
    // Try to find user and auto-detect role
    
    // Check demo admin account
    if (demoAccounts['admin'].username === username && demoAccounts['admin'].password === password) {
        currentUser = {
            username: username,
            password: password,
            role: 'admin',
            name: demoAccounts['admin'].name,
            isCustomAdmin: false
        };
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        checkFirstLogin();
        return;
    }

    // Check custom admin accounts
    const customAdmin = adminAccounts.find(a => a.username === username && a.password === password);
    if (customAdmin) {
        currentUser = {
            username: username,
            password: password,
            role: 'admin',
            name: customAdmin.name,
            isCustomAdmin: true
        };
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        checkFirstLogin();
        return;
    }

    // Check teachers
    const teacher = teachers.find(t => t.username === username && t.password === password);
    if (teacher) {
        currentUser = {
            username: username,
            password: password,
            role: 'teacher',
            name: teacher.name,
            data: teacher
        };
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        checkFirstLogin();
        return;
    }

    // Check students
    const student = students.find(s => s.username === username && s.password === password);
    if (student) {
        currentUser = {
            username: username,
            password: password,
            role: 'student',
            name: student.name,
            data: student
        };
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        checkFirstLogin();
        return;
    }

    alert('❌ Invalid username or password. Please try again.');
}

function checkFirstLogin() {
    const userId = `${currentUser.role}_${currentUser.username}`;
    console.log('Checking first login for:', userId);
    console.log('Password changed users:', passwordChangedUsers);
    console.log('Current user role:', currentUser.role);
    
    if (!passwordChangedUsers.includes(userId)) {
        console.log('First login detected, showing password change modal');
        // First login - show password change modal
        isFirstLoginChange = true;  // Set flag for first login
        document.getElementById('modalTitle').textContent = 'Change Your Password (First Login)';
        document.getElementById('modalSubtitle').textContent = 'This is your first login. Please change your password.';
        document.getElementById('skipPasswordBtn').style.display = 'inline-block';
        document.getElementById('cancelPasswordBtn').style.display = 'none';
        document.getElementById('currentPassword').value = currentUser.password;
        document.getElementById('passwordChangeForm').reset();
        document.getElementById('passwordChangeModal').classList.add('active');
    } else {
        console.log('Password already changed, showing main app');
        // Password already changed - proceed to main app
        showMainApp();
    }
}

function showMainApp() {
    console.log('showMainApp() called');
    console.log('Current user role:', currentUser.role);
    console.log('Students available:', students.length);
    
    document.getElementById('loginPage').classList.remove('active');
    document.getElementById('mainApp').classList.add('active');
    document.getElementById('userInfo').textContent = `${currentUser.role.toUpperCase()} - ${currentUser.name}`;

    document.querySelectorAll('.role-panel').forEach(panel => panel.classList.remove('active'));

    if (currentUser.role === 'admin') {
        console.log('Admin role detected, showing admin panel');
        document.getElementById('adminPanel').classList.add('active');
        
        // Ensure students tab is visible
        const studentsTab = document.getElementById('students-tab');
        const teachersTab = document.getElementById('teachers-tab');
        const backupTab = document.getElementById('backup-tab');
        
        if (studentsTab) studentsTab.classList.add('active');
        if (teachersTab) teachersTab.classList.remove('active');
        if (backupTab) backupTab.classList.remove('active');
        
        console.log('Calling displayStudents()');
        displayStudents();
        console.log('Calling displayTeachers()');
        displayTeachers();
        console.log('Calling displayAdmins()');
        displayAdmins();
    } else if (currentUser.role === 'teacher') {
        document.getElementById('teacherPanel').classList.add('active');
        displayTeacherDashboard();
    } else if (currentUser.role === 'student') {
        document.getElementById('studentPanel').classList.add('active');
        displayStudentDashboard();
    }
}

function logout() {
    if (confirm('Are you sure you want to logout?')) {
        currentUser = null;
        localStorage.removeItem('currentUser');
        
        document.getElementById('loginPage').classList.add('active');
        document.getElementById('mainApp').classList.remove('active');
        document.getElementById('passwordChangeModal').classList.remove('active');

        document.getElementById('studentForm').reset();
        document.getElementById('teacherForm').reset();
        document.getElementById('loginForm').reset();
        document.getElementById('passwordChangeForm').reset();
    }
}

// ==================== PASSWORD CHANGE ====================
let isFirstLoginChange = true;  // Track if this is first login password change

function handlePasswordChange(e) {
    e.preventDefault();
    
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    
    // On first login, auto-fill current password, just validate new password
    if (isFirstLoginChange) {
        // On first login, we don't require entering current password
        if (!currentPassword) {
            alert('Please enter your current password');
            return;
        }
    } else {
        // On post-login change, verify current password
        if (currentPassword !== currentUser.password) {
            alert('Current password is incorrect!');
            return;
        }
    }
    
    // Check if passwords match
    if (newPassword !== confirmPassword) {
        alert('New passwords do not match!');
        return;
    }
    
    // Check password length
    if (newPassword.length < 6) {
        alert('Password must be at least 6 characters long!');
        return;
    }
    
    // Update password in appropriate data structure
    if (currentUser.role === 'admin') {
        if (currentUser.isCustomAdmin) {
            // Update custom admin password
            const customAdmin = adminAccounts.find(a => a.username === currentUser.username);
            if (customAdmin) {
                customAdmin.password = newPassword;
                localStorage.setItem('adminAccounts', JSON.stringify(adminAccounts));
            }
        } else {
            // Update demo admin password
            demoAccounts.admin.password = newPassword;
            localStorage.setItem('demoAccounts', JSON.stringify(demoAccounts));
        }
    } else if (currentUser.role === 'teacher') {
        const teacher = teachers.find(t => t.username === currentUser.username);
        if (teacher) {
            teacher.password = newPassword;
            localStorage.setItem('teachers', JSON.stringify(teachers));
        }
    } else if (currentUser.role === 'student') {
        const student = students.find(s => s.username === currentUser.username);
        if (student) {
            student.password = newPassword;
            localStorage.setItem('students', JSON.stringify(students));
        }
    }
    
    // Update current user password
    currentUser.password = newPassword;
    localStorage.setItem('currentUser', JSON.stringify(currentUser));
    
    // Mark as password changed
    const userId = `${currentUser.role}_${currentUser.username}`;
    if (!passwordChangedUsers.includes(userId)) {
        passwordChangedUsers.push(userId);
        localStorage.setItem('passwordChangedUsers', JSON.stringify(passwordChangedUsers));
    }
    
    // Hide modal and show main app
    document.getElementById('passwordChangeModal').classList.remove('active');
    document.getElementById('passwordChangeForm').reset();
    isFirstLoginChange = true;  // Reset for next user
    showMainApp();
    
    alert('✅ Password changed successfully!');
}

function skipPasswordChange() {
    const userId = `${currentUser.role}_${currentUser.username}`;
    if (!passwordChangedUsers.includes(userId)) {
        passwordChangedUsers.push(userId);
        localStorage.setItem('passwordChangedUsers', JSON.stringify(passwordChangedUsers));
    }
    
    document.getElementById('passwordChangeModal').classList.remove('active');
    document.getElementById('passwordChangeForm').reset();
    showMainApp();
}

function openPasswordChangeDialog() {
    // Set modal for post-login password change
    isFirstLoginChange = false;  // Set flag for post-login change
    document.getElementById('modalTitle').textContent = 'Change Your Password';
    document.getElementById('modalSubtitle').textContent = 'Update your password to something new.';
    document.getElementById('skipPasswordBtn').style.display = 'none';
    document.getElementById('cancelPasswordBtn').style.display = 'inline-block';
    document.getElementById('currentPassword').value = '';  // Don't pre-fill for post-login
    document.getElementById('passwordChangeForm').reset();
    document.getElementById('passwordChangeModal').classList.add('active');
}

function closePasswordChangeDialog() {
    document.getElementById('passwordChangeForm').reset();
    document.getElementById('passwordChangeModal').classList.remove('active');
}

// ==================== TAB SWITCHING ====================
function showTab(tabName) {
    // Hide all tabs
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(tab => tab.classList.remove('active'));
    
    // Remove active class from buttons
    const buttons = document.querySelectorAll('.tab-button');
    buttons.forEach(btn => btn.classList.remove('active'));
    
    // Show selected tab
    document.getElementById(tabName + '-tab').classList.add('active');
    
    // Add active class to clicked button
    event.target.classList.add('active');
}

// ==================== STUDENT MANAGEMENT ====================
function addStudent(e) {
    e.preventDefault();
    
    const student = {
        id: editingStudentId || Date.now(),
        name: document.getElementById('studentName').value,
        roll: document.getElementById('studentRoll').value,
        email: document.getElementById('studentEmail').value,
        phone: document.getElementById('studentPhone').value,
        class: document.getElementById('studentClass').value,
        fees: parseFloat(document.getElementById('studentFees').value),
        feesPaid: parseFloat(document.getElementById('studentFeesPaid').value) || 0,
        dueDate: document.getElementById('studentDueDate').value,
        status: document.getElementById('studentStatus').value,
        username: document.getElementById('studentUsername').value,
        password: document.getElementById('studentPassword').value
    };
    
    console.log('Adding student:', student);
    
    if (editingStudentId) {
        students = students.map(s => s.id === editingStudentId ? student : s);
        editingStudentId = null;
        alert('✅ Student updated successfully!');
    } else {
        students.push(student);
        alert('✅ Student added successfully!');
    }
    
    console.log('All students after add:', students);
    localStorage.setItem('students', JSON.stringify(students));
    document.getElementById('studentForm').reset();
    document.getElementById('studentStatus').value = 'Active';  // Reset status to default
    document.getElementById('studentFeesPaid').value = '0';  // Reset fees paid
    
    // Ensure tab is visible
    const studentsTab = document.getElementById('students-tab');
    if (studentsTab) {
        studentsTab.classList.add('active');
    }
    
    displayStudents();
    
    const submitBtn = document.querySelector('#studentForm button[type="submit"]');
    submitBtn.textContent = 'Add Student';
}

function editStudent(id) {
    const student = students.find(s => s.id === id);
    if (student) {
        editingStudentId = id;
        document.getElementById('studentName').value = student.name;
        document.getElementById('studentRoll').value = student.roll;
        document.getElementById('studentEmail').value = student.email;
        document.getElementById('studentPhone').value = student.phone;
        document.getElementById('studentClass').value = student.class;
        document.getElementById('studentFees').value = student.fees;
        document.getElementById('studentFeesPaid').value = student.feesPaid || 0;
        document.getElementById('studentDueDate').value = student.dueDate || '';
        document.getElementById('studentStatus').value = student.status;
        document.getElementById('studentUsername').value = student.username;
        document.getElementById('studentPassword').value = student.password;
        
        document.querySelector('#studentForm button[type="submit"]').textContent = 'Update Student';
        document.querySelector('.form-section').scrollIntoView({ behavior: 'smooth' });
    }
}

function deleteStudent(id) {
    if (confirm('Are you sure you want to delete this student?')) {
        students = students.filter(s => s.id !== id);
        localStorage.setItem('students', JSON.stringify(students));
        displayStudents();
    }
}

function displayStudents() {
    const tbody = document.getElementById('studentTableBody');
    
    console.log('displayStudents() called');
    console.log('Current students array:', students);
    console.log('Students count:', students.length);
    
    if (!tbody) {
        console.error('studentTableBody element not found');
        return;
    }
    
    if (students.length === 0) {
        console.log('No students to display, showing empty message');
        tbody.innerHTML = '<tr><td colspan="11" class="empty-message">No students added yet.</td></tr>';
        return;
    }
    
    console.log('Rendering students table...');
    tbody.innerHTML = students.map(student => {
        // Ensure feesPaid and dueDate exist
        const feesPaid = student.feesPaid || 0;
        const dueDate = student.dueDate || new Date().toISOString().split('T')[0];
        
        const pending = student.fees - feesPaid;
        const dueDateObj = new Date(dueDate);
        const isOverdue = dueDateObj < new Date() && pending > 0;
        
        return `
        <tr>
            <td>${student.name}</td>
            <td>${student.roll}</td>
            <td>${student.email}</td>
            <td>${student.phone}</td>
            <td>${student.class}</td>
            <td>₹${student.fees.toFixed(2)}</td>
            <td>₹${feesPaid.toFixed(2)}</td>
            <td class="${pending > 0 ? 'text-warning' : 'text-success'}">₹${pending.toFixed(2)}</td>
            <td class="${isOverdue ? 'text-danger' : ''}">${new Date(dueDate).toLocaleDateString()}</td>
            <td><span class="status-${student.status.toLowerCase().replace(' ', '-')}">${student.status}</span></td>
            <td>
                <button class="btn-action btn-edit" onclick="editStudent(${student.id})">Edit</button>
                <button class="btn-action btn-delete" onclick="deleteStudent(${student.id})">Delete</button>
            </td>
        </tr>
    `}).join('');
}

function filterStudents() {
    const searchTerm = document.getElementById('studentSearch').value.toLowerCase();
    const tbody = document.getElementById('studentTableBody');
    
    if (!tbody) {
        console.error('studentTableBody element not found');
        return;
    }
    
    const rows = tbody.querySelectorAll('tr');
    
    rows.forEach(row => {
        const name = row.cells[0].textContent.toLowerCase();
        const roll = row.cells[1].textContent.toLowerCase();
        
        if (name.includes(searchTerm) || roll.includes(searchTerm)) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// ==================== TEACHER MANAGEMENT ====================
function addTeacher(e) {
    e.preventDefault();
    
    const teacher = {
        id: editingTeacherId || Date.now(),
        name: document.getElementById('teacherName').value,
        email: document.getElementById('teacherEmail').value,
        phone: document.getElementById('teacherPhone').value,
        subject: document.getElementById('teacherSubject').value,
        qualification: document.getElementById('teacherQualification').value,
        experience: parseInt(document.getElementById('teacherExperience').value),
        salary: parseFloat(document.getElementById('teacherSalary').value),
        status: document.getElementById('teacherStatus').value,
        username: document.getElementById('teacherUsername').value,
        password: document.getElementById('teacherPassword').value
    };
    
    if (editingTeacherId) {
        teachers = teachers.map(t => t.id === editingTeacherId ? teacher : t);
        editingTeacherId = null;
    } else {
        teachers.push(teacher);
    }
    
    localStorage.setItem('teachers', JSON.stringify(teachers));
    document.getElementById('teacherForm').reset();
    displayTeachers();
    
    const submitBtn = document.querySelector('#teacherForm button[type="submit"]');
    submitBtn.textContent = 'Add Teacher';
}

function editTeacher(id) {
    const teacher = teachers.find(t => t.id === id);
    if (teacher) {
        editingTeacherId = id;
        document.getElementById('teacherName').value = teacher.name;
        document.getElementById('teacherEmail').value = teacher.email;
        document.getElementById('teacherPhone').value = teacher.phone;
        document.getElementById('teacherSubject').value = teacher.subject;
        document.getElementById('teacherQualification').value = teacher.qualification;
        document.getElementById('teacherExperience').value = teacher.experience;
        document.getElementById('teacherSalary').value = teacher.salary;
        document.getElementById('teacherStatus').value = teacher.status;
        document.getElementById('teacherUsername').value = teacher.username;
        document.getElementById('teacherPassword').value = teacher.password;
        
        document.querySelector('#teacherForm button[type="submit"]').textContent = 'Update Teacher';
        document.querySelector('.form-section').scrollIntoView({ behavior: 'smooth' });
    }
}

function deleteTeacher(id) {
    if (confirm('Are you sure you want to delete this teacher?')) {
        teachers = teachers.filter(t => t.id !== id);
        localStorage.setItem('teachers', JSON.stringify(teachers));
        displayTeachers();
    }
}

function displayTeachers() {
    const tbody = document.getElementById('teacherTableBody');
    
    if (teachers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="empty-message">No teachers added yet.</td></tr>';
        return;
    }
    
    tbody.innerHTML = teachers.map(teacher => `
        <tr>
            <td>${teacher.name}</td>
            <td>${teacher.email}</td>
            <td>${teacher.phone}</td>
            <td>${teacher.subject}</td>
            <td>${teacher.qualification}</td>
            <td>${teacher.experience}</td>
            <td>₹${teacher.salary.toFixed(2)}</td>
            <td><span class="status-${teacher.status.toLowerCase().replace(' ', '-')}">${teacher.status}</span></td>
            <td>
                <button class="btn-action btn-edit" onclick="editTeacher(${teacher.id})">Edit</button>
                <button class="btn-action btn-delete" onclick="deleteTeacher(${teacher.id})">Delete</button>
            </td>
        </tr>
    `).join('');
}

function filterTeachers() {
    const searchTerm = document.getElementById('teacherSearch').value.toLowerCase();
    const tbody = document.getElementById('teacherTableBody');
    const rows = tbody.querySelectorAll('tr');
    
    rows.forEach(row => {
        const name = row.cells[0].textContent.toLowerCase();
        const subject = row.cells[3].textContent.toLowerCase();
        
        if (name.includes(searchTerm) || subject.includes(searchTerm)) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// ==================== ADMIN ACCOUNT MANAGEMENT ====================
function addAdmin(e) {
    e.preventDefault();
    
    const adminName = document.getElementById('adminName').value;
    const adminUsername = document.getElementById('adminUsername').value;
    const adminPassword = document.getElementById('adminPassword').value;
    const adminEmail = document.getElementById('adminEmail').value;
    
    // Check if username already exists
    const usernameExists = adminAccounts.some(a => a.username === adminUsername) ||
                          demoAccounts.admin.username === adminUsername;
    
    if (usernameExists) {
        alert('❌ Username already exists. Please choose a different username.');
        return;
    }
    
    const newAdmin = {
        id: Date.now(),
        name: adminName,
        username: adminUsername,
        password: adminPassword,
        email: adminEmail || '',
        createdDate: new Date().toLocaleDateString()
    };
    
    adminAccounts.push(newAdmin);
    localStorage.setItem('adminAccounts', JSON.stringify(adminAccounts));
    
    console.log('New admin created:', newAdmin);
    alert(`✅ Admin account created successfully!\nUsername: ${adminUsername}`);
    
    document.getElementById('adminForm').reset();
    displayAdmins();
}

function deleteAdmin(id) {
    if (confirm('Are you sure you want to delete this admin account?')) {
        adminAccounts = adminAccounts.filter(a => a.id !== id);
        localStorage.setItem('adminAccounts', JSON.stringify(adminAccounts));
        displayAdmins();
        alert('✅ Admin account deleted successfully!');
    }
}

function displayAdmins() {
    const tbody = document.getElementById('adminTableBody');
    
    if (!tbody) {
        console.error('adminTableBody element not found');
        return;
    }
    
    // Combine demo admin and custom admins for display
    const allAdmins = [
        {
            id: 'demo',
            name: demoAccounts.admin.name,
            username: demoAccounts.admin.username,
            email: 'admin@tuition.local',
            type: 'System Admin'
        },
        ...adminAccounts.map(a => ({
            ...a,
            type: 'Custom Admin'
        }))
    ];
    
    if (allAdmins.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-message">No admin accounts found.</td></tr>';
        return;
    }
    
    tbody.innerHTML = allAdmins.map(admin => `
        <tr>
            <td>${admin.name}</td>
            <td>${admin.username}</td>
            <td>${admin.email}</td>
            <td><span class="status-${admin.type === 'System Admin' ? 'active' : 'inactive'}">${admin.type}</span></td>
            <td>
                ${admin.type === 'Custom Admin' ? `<button class="btn-action btn-delete" onclick="deleteAdmin(${admin.id})">Delete</button>` : '<span style="color: #999;">System Account</span>'}
            </td>
        </tr>
    `).join('');
}

// ==================== TEACHER DASHBOARD ====================
function displayTeacherDashboard() {
    const teacher = currentUser.data;
    
    const infoHtml = `
        <p><strong>Name:</strong> ${teacher.name}</p>
        <p><strong>Email:</strong> ${teacher.email}</p>
        <p><strong>Phone:</strong> ${teacher.phone}</p>
        <p><strong>Subject:</strong> ${teacher.subject}</p>
        <p><strong>Qualification:</strong> ${teacher.qualification}</p>
        <p><strong>Experience:</strong> ${teacher.experience} years</p>
        <p><strong>Monthly Salary:</strong> ₹${teacher.salary.toFixed(2)}</p>
        <p><strong>Status:</strong> <span class="status-${teacher.status.toLowerCase()}">${teacher.status}</span></p>
    `;
    document.getElementById('teacherInfo').innerHTML = infoHtml;
    document.getElementById('teacherName').textContent = teacher.name;
    
    const tbody = document.getElementById('teacherStudentTableBody');
    if (students.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-message">No students found.</td></tr>';
        return;
    }
    
    tbody.innerHTML = students.map(student => `
        <tr>
            <td>${student.name}</td>
            <td>${student.roll}</td>
            <td>${student.class}</td>
            <td>${student.email}</td>
            <td>${student.phone}</td>
        </tr>
    `).join('');
}

function filterTeacherStudents() {
    const searchTerm = document.getElementById('teacherStudentSearch').value.toLowerCase();
    const tbody = document.getElementById('teacherStudentTableBody');
    const rows = tbody.querySelectorAll('tr');
    
    rows.forEach(row => {
        const name = row.cells[0].textContent.toLowerCase();
        const roll = row.cells[1].textContent.toLowerCase();
        
        if (name.includes(searchTerm) || roll.includes(searchTerm)) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// ==================== STUDENT DASHBOARD ====================
function displayStudentDashboard() {
    const student = currentUser.data;
    
    const infoHtml = `
        <p><strong>Name:</strong> ${student.name}</p>
        <p><strong>Roll Number:</strong> ${student.roll}</p>
        <p><strong>Email:</strong> ${student.email}</p>
        <p><strong>Phone:</strong> ${student.phone}</p>
        <p><strong>Monthly Fees:</strong> ₹${student.fees.toFixed(2)}</p>
        <p><strong>Status:</strong> <span class="status-${student.status.toLowerCase().replace(' ', '-')}">${student.status}</span></p>
    `;
    document.getElementById('studentInfo').innerHTML = infoHtml;
    document.getElementById('studentName').textContent = student.name;
    
    const classInfoHtml = `
        <p><strong>Class:</strong> ${student.class}</p>
        <p><strong>Total Students in Class:</strong> ${students.filter(s => s.class === student.class).length}</p>
        <p><strong>Total Teachers:</strong> ${teachers.length}</p>
    `;
    document.getElementById('studentClassInfo').innerHTML = classInfoHtml;
    
    // Display fees status
    const feesPaid = student.feesPaid || 0;
    const pending = student.fees - feesPaid;
    const dueDate = student.dueDate || '';
    const dueDateObj = new Date(dueDate);
    const isOverdue = dueDateObj < new Date() && pending > 0;
    
    const feesStatusHtml = `
        <p><strong>Monthly Fees:</strong> ₹${student.fees.toFixed(2)}</p>
        <p><strong>Fees Paid:</strong> <span class="text-success">₹${feesPaid.toFixed(2)}</span></p>
        <p><strong>Pending Amount:</strong> <span class="${pending > 0 ? 'text-warning' : 'text-success'}">₹${pending.toFixed(2)}</span></p>
        <p><strong>Due Date:</strong> <span class="${isOverdue ? 'text-danger' : ''}">${dueDate ? new Date(dueDate).toLocaleDateString() : 'Not Set'}</span></p>
        ${isOverdue ? '<p class="text-danger"><strong>⚠️ Payment Overdue!</strong></p>' : ''}
    `;
    document.getElementById('feesStatus').innerHTML = feesStatusHtml;
    
    // Enable/disable payment button
    const paymentBtn = document.getElementById('paymentBtn');
    if (pending <= 0) {
        paymentBtn.textContent = '✅ Fees Paid';
        paymentBtn.disabled = true;
        paymentBtn.style.opacity = '0.5';
    } else {
        paymentBtn.textContent = '📝 Record Payment';
        paymentBtn.disabled = false;
        paymentBtn.style.opacity = '1';
    }
}

// ==================== STUDENT PAYMENT MANAGEMENT ====================
function openPaymentModal() {
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('paymentDate').value = today;
    document.getElementById('paymentAmount').value = '';
    document.getElementById('paymentMethod').value = '';
    document.getElementById('paymentForm').reset();
    document.getElementById('paymentDate').value = today;
    document.getElementById('paymentModal').classList.add('active');
}

function closePaymentModal() {
    document.getElementById('paymentModal').classList.remove('active');
    document.getElementById('paymentForm').reset();
}

function handlePayment(e) {
    e.preventDefault();
    
    const amount = parseFloat(document.getElementById('paymentAmount').value);
    const date = document.getElementById('paymentDate').value;
    const method = document.getElementById('paymentMethod').value;
    
    if (amount <= 0) {
        alert('Please enter a valid payment amount');
        return;
    }
    
    // Update student's fees paid
    const studentIndex = students.findIndex(s => s.id === currentUser.data.id);
    if (studentIndex !== -1) {
        const oldFeesPaid = students[studentIndex].feesPaid || 0;
        students[studentIndex].feesPaid = oldFeesPaid + amount;
        
        // Update current user data as well
        currentUser.data.feesPaid = students[studentIndex].feesPaid;
        
        localStorage.setItem('students', JSON.stringify(students));
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        
        console.log(`Payment recorded: ₹${amount.toFixed(2)} on ${date} via ${method}`);
        console.log(`New fees paid: ₹${students[studentIndex].feesPaid.toFixed(2)}`);
        
        alert(`✅ Payment of ₹${amount.toFixed(2)} recorded successfully!`);
        
        closePaymentModal();
        displayStudentDashboard();
    }
}

// ==================== GOOGLE DRIVE INTEGRATION ====================
let googleDriveConfig = JSON.parse(localStorage.getItem('googleDriveConfig')) || {};
let googleAuth = null;
let gdriveFileId = null;
let gapiInitialized = false;

// Initialize Google Drive API
function initGoogleDrive() {
    if (!googleDriveConfig.clientId) {
        alert('Please setup your Google Cloud Client ID first in the Setup section below.');
        return;
    }

    const connectBtn = document.getElementById('connectDriveBtn');
    connectBtn.disabled = true;
    connectBtn.textContent = '⏳ Connecting...';

    // Load the Google API
    gapi.load('client:auth2', initializeGoogleAuth);
}

function initializeGoogleAuth() {
    gapi.client.init({
        clientId: googleDriveConfig.clientId,
        discoveryDocs: ['https://www.googleapis.com/discovery/v1/apis/drive/v3/rest'],
        scope: 'https://www.googleapis.com/auth/drive.file'
    }).then(() => {
        gapiInitialized = true;
        googleAuth = gapi.auth2.getAuthInstance();
        
        // Try to sign in
        googleAuth.signIn().then(() => {
            const user = googleAuth.currentUser.get();
            const profile = user.getBasicProfile();
            updateGoogleDriveStatus(true, profile.getEmail());
            loadGoogleDriveFileId();
            
            document.getElementById('connectDriveBtn').disabled = false;
            document.getElementById('connectDriveBtn').textContent = '🔗 Connect Google Drive';
            alert('✅ Successfully connected to Google Drive!');
        }).catch(err => {
            console.error('Google Sign-In error:', err);
            document.getElementById('connectDriveBtn').disabled = false;
            document.getElementById('connectDriveBtn').textContent = '🔗 Connect Google Drive';
            alert('Failed to sign in with Google:\n' + err.error + '\n\nCheck console (F12) for details.');
        });
    }).catch(err => {
        console.error('Google API initialization error:', err);
        document.getElementById('connectDriveBtn').disabled = false;
        document.getElementById('connectDriveBtn').textContent = '🔗 Connect Google Drive';
        alert('Failed to initialize Google API:\n' + JSON.stringify(err) + '\n\nMake sure your Client ID is correct.');
    });
}

function setupGoogleDriveConfig() {
    const clientId = document.getElementById('clientId').value.trim();
    
    if (!clientId) {
        showSetupStatus('❌ Please enter your Client ID', 'error');
        return;
    }
    
    if (clientId.length < 20) {
        showSetupStatus('❌ Client ID seems invalid. Must be at least 20 characters.', 'error');
        return;
    }
    
    // Validate Client ID format (should contain .apps.googleusercontent.com)
    if (!clientId.includes('.apps.googleusercontent.com')) {
        const confirm_save = confirm('⚠️ This doesn\'t look like a valid Client ID.\n\nValid Client IDs should contain ".apps.googleusercontent.com"\n\nDo you want to save anyway?');
        if (!confirm_save) {
            return;
        }
    }
    
    googleDriveConfig.clientId = clientId;
    localStorage.setItem('googleDriveConfig', JSON.stringify(googleDriveConfig));
    showSetupStatus('✅ Client ID saved successfully! You can now click "Connect Google Drive" button above.', 'success');
    document.getElementById('clientId').value = '';
    
    // Reset Google Auth to force re-initialization with new Client ID
    googleAuth = null;
    gapiInitialized = false;
}

function showSetupStatus(message, type) {
    const statusEl = document.getElementById('setupStatus');
    statusEl.textContent = message;
    statusEl.className = type;
}

function disconnectGoogleDrive() {
    if (confirm('Are you sure you want to disconnect Google Drive?')) {
        if (googleAuth) {
            googleAuth.signOut().then(() => {
                updateGoogleDriveStatus(false);
                alert('Disconnected from Google Drive');
            });
        }
    }
}

function updateGoogleDriveStatus(isConnected, email = '') {
    const statusBox = document.getElementById('driveStatus');
    const statusText = document.getElementById('statusText');
    const connectedUser = document.getElementById('connectedUser');
    const userEmail = document.getElementById('userEmail');
    const connectBtn = document.getElementById('connectDriveBtn');
    const disconnectBtn = document.getElementById('disconnectDriveBtn');
    const backupBtn = document.getElementById('backupBtn');
    const restoreBtn = document.getElementById('restoreBtn');
    
    if (isConnected) {
        statusBox.className = 'status-box connected';
        statusText.textContent = '✅ Connected to Google Drive';
        connectedUser.style.display = 'block';
        userEmail.textContent = email;
        connectBtn.style.display = 'none';
        disconnectBtn.style.display = 'inline-block';
        backupBtn.disabled = false;
        restoreBtn.disabled = false;
    } else {
        statusBox.className = 'status-box not-connected';
        statusText.textContent = '❌ Not Connected';
        connectedUser.style.display = 'none';
        connectBtn.style.display = 'inline-block';
        disconnectBtn.style.display = 'none';
        backupBtn.disabled = true;
        restoreBtn.disabled = true;
    }
}

function backupToGoogleDrive() {
    if (!googleAuth || !googleAuth.isSignedIn.get()) {
        alert('Please connect to Google Drive first');
        return;
    }

    const backupBtn = document.getElementById('backupBtn');
    backupBtn.disabled = true;
    backupBtn.textContent = '⏳ Backing up...';

    const backupData = {
        students: students,
        teachers: teachers,
        timestamp: new Date().toISOString(),
        version: '1.0'
    };

    const fileName = 'tuition_admin_backup_' + new Date().toISOString().split('T')[0] + '.json';
    
    // If file already exists, update it instead of creating new
    const query = `name='${fileName}' and trashed=false`;
    
    gapi.client.drive.files.list({
        q: query,
        spaces: 'drive',
        fields: 'files(id)'
    }).then((response) => {
        let fileId = null;
        
        if (response.result.files && response.result.files.length > 0) {
            fileId = response.result.files[0].id;
        }
        
        const request = gapi.client.drive.files.create({
            resource: {
                name: fileName,
                mimeType: 'application/json'
            },
            media: {
                mimeType: 'application/json',
                body: JSON.stringify(backupData, null, 2)
            },
            fields: 'id'
        });
        
        if (fileId) {
            request = gapi.client.drive.files.update({
                fileId: fileId,
                media: {
                    mimeType: 'application/json',
                    body: JSON.stringify(backupData, null, 2)
                },
                fields: 'id'
            });
        }
        
        return request;
    }).then((response) => {
        if (response.result.id) {
            gdriveFileId = response.result.id;
            localStorage.setItem('googleDriveFileId', gdriveFileId);
            updateLastSync();
            
            backupBtn.disabled = false;
            backupBtn.textContent = '💾 Backup to Google Drive';
            alert('✅ Backup saved successfully to Google Drive!');
        }
    }).catch((err) => {
        console.error('Backup error:', err);
        backupBtn.disabled = false;
        backupBtn.textContent = '💾 Backup to Google Drive';
        alert('Failed to backup:\n' + JSON.stringify(err.result?.error?.message || err.message));
    });
}

function restoreFromGoogleDrive() {
    if (!googleAuth || !googleAuth.isSignedIn.get()) {
        alert('Please connect to Google Drive first');
        return;
    }

    const restoreBtn = document.getElementById('restoreBtn');
    restoreBtn.disabled = true;
    restoreBtn.textContent = '⏳ Restoring...';

    if (!gdriveFileId) {
        // Try to find backup file
        gapi.client.drive.files.list({
            q: `name contains 'tuition_admin_backup' and trashed=false`,
            spaces: 'drive',
            fields: 'files(id, name)',
            pageSize: 1,
            orderBy: 'modifiedTime desc'
        }).then((response) => {
            if (response.result.files && response.result.files.length > 0) {
                gdriveFileId = response.result.files[0].id;
                localStorage.setItem('googleDriveFileId', gdriveFileId);
                performRestore(restoreBtn);
            } else {
                restoreBtn.disabled = false;
                restoreBtn.textContent = '📥 Restore from Google Drive';
                alert('No backup file found in Google Drive');
            }
        }).catch((err) => {
            console.error('File search error:', err);
            restoreBtn.disabled = false;
            restoreBtn.textContent = '📥 Restore from Google Drive';
            alert('Failed to find backup file');
        });
    } else {
        performRestore(restoreBtn);
    }
}

function performRestore(restoreBtn) {
    if (!confirm('This will replace all current data. Are you sure?')) {
        restoreBtn.disabled = false;
        restoreBtn.textContent = '📥 Restore from Google Drive';
        return;
    }

    gapi.client.drive.files.get({
        fileId: gdriveFileId,
        alt: 'media'
    }).then((response) => {
        const backupData = response.result;
        
        if (backupData.students) {
            students = backupData.students;
            localStorage.setItem('students', JSON.stringify(students));
        }
        
        if (backupData.teachers) {
            teachers = backupData.teachers;
            localStorage.setItem('teachers', JSON.stringify(teachers));
        }
        
        updateLastSync();
        
        restoreBtn.disabled = false;
        restoreBtn.textContent = '📥 Restore from Google Drive';
        alert('✅ Data restored successfully from Google Drive!');
        
        // Refresh current view
        if (currentUser.role === 'admin') {
            displayStudents();
            displayTeachers();
        }
    }).catch((err) => {
        console.error('Restore error:', err);
        restoreBtn.disabled = false;
        restoreBtn.textContent = '📥 Restore from Google Drive';
        alert('Failed to restore:\n' + JSON.stringify(err.result?.error?.message || err.message));
    });
}

function loadGoogleDriveFileId() {
    gdriveFileId = localStorage.getItem('googleDriveFileId');
    updateLastSync();
}

function updateLastSync() {
    const lastSyncEl = document.getElementById('lastSync');
    const syncTimeEl = document.getElementById('syncTime');
    
    const lastSync = localStorage.getItem('lastGoogleDriveSync');
    if (lastSync) {
        lastSyncEl.style.display = 'block';
        const date = new Date(lastSync);
        syncTimeEl.textContent = date.toLocaleString();
    }
    
    localStorage.setItem('lastGoogleDriveSync', new Date().toISOString());
}

function downloadLocalBackup() {
    const backupData = {
        students: students,
        teachers: teachers,
        timestamp: new Date().toISOString(),
        version: '1.0'
    };
    
    const dataStr = JSON.stringify(backupData, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    
    const link = document.createElement('a');
    link.href = url;
    link.download = 'tuition_admin_backup_' + new Date().toISOString().split('T')[0] + '.json';
    link.click();
    
    URL.revokeObjectURL(url);
    alert('✅ Backup downloaded successfully!');
}
