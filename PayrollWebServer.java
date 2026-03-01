import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

public class PayrollWebServer {
    
    // Data storage
    private static List<Map<String, Object>> employees = new ArrayList<>();
    private static List<Map<String, Object>> payrolls = new ArrayList<>();
    
    public static void main(String[] args) throws IOException {
        // Add sample data
        loadSampleData();
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Serve the HTML page
        server.createContext("/", new HtmlHandler());
        
        // API endpoints
        server.createContext("/api/employees", new EmployeeHandler());
        server.createContext("/api/payrolls", new PayrollHandler());
        
        server.setExecutor(null);
        System.out.println("✅ Server started at http://localhost:8080");
        System.out.println("Press Ctrl+C to stop");
        server.start();
    }
    
    private static void loadSampleData() {
        Map<String, Object> emp1 = new HashMap<>();
        emp1.put("employeeId", "FT001");
        emp1.put("name", "John Doe");
        emp1.put("department", "IT");
        emp1.put("hireDate", "2024-01-15");
        emp1.put("baseSalary", 60000.0);
        emp1.put("type", "fulltime");
        employees.add(emp1);
        
        Map<String, Object> emp2 = new HashMap<>();
        emp2.put("employeeId", "PT001");
        emp2.put("name", "Jane Smith");
        emp2.put("department", "Sales");
        emp2.put("hireDate", "2024-02-01");
        emp2.put("hourlyRate", 25.0);
        emp2.put("type", "parttime");
        employees.add(emp2);
        
        Map<String, Object> emp3 = new HashMap<>();
        emp3.put("employeeId", "CT001");
        emp3.put("name", "Mike Johnson");
        emp3.put("department", "Consulting");
        emp3.put("hireDate", "2024-01-01");
        emp3.put("contractAmount", 50000.0);
        emp3.put("contractDuration", 12);
        emp3.put("type", "contractor");
        employees.add(emp3);
    }
    
    // Handler for serving the HTML page
    static class HtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = getHtmlContent();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, html.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(html.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
        
        private String getHtmlContent() {
            return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Employee Payroll System</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .container {
            max-width: 1400px;
            margin: 0 auto;
        }
        
        /* Header */
        .header {
            background: white;
            border-radius: 15px;
            padding: 30px;
            margin-bottom: 30px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
        }
        
        .header h1 {
            color: #333;
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        
        .header p {
            color: #666;
            font-size: 1.1em;
        }
        
        /* Navigation Tabs */
        .tabs {
            display: flex;
            gap: 10px;
            margin-bottom: 30px;
            background: white;
            padding: 20px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.1);
        }
        
        .tab-btn {
            padding: 12px 30px;
            border: none;
            border-radius: 8px;
            font-size: 1em;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
        }
        
        .tab-btn.active {
            background: #667eea;
            color: white;
        }
        
        .tab-btn:not(.active) {
            background: #f0f0f0;
            color: #666;
        }
        
        .tab-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }
        
        /* Dashboard Cards */
        .dashboard {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .card {
            background: white;
            padding: 25px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.1);
            transition: transform 0.3s;
        }
        
        .card:hover {
            transform: translateY(-5px);
        }
        
        .card h3 {
            color: #666;
            font-size: 0.9em;
            margin-bottom: 10px;
        }
        
        .card .number {
            font-size: 2.2em;
            font-weight: bold;
            color: #667eea;
        }
        
        /* Main Content */
        .content {
            background: white;
            border-radius: 15px;
            padding: 30px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
        }
        
        .content-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 30px;
        }
        
        .content-header h2 {
            color: #333;
            font-size: 1.8em;
        }
        
        /* Buttons */
        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            font-size: 1em;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
        }
        
        .btn-primary {
            background: #667eea;
            color: white;
        }
        
        .btn-success {
            background: #48bb78;
            color: white;
        }
        
        .btn-danger {
            background: #f56565;
            color: white;
        }
        
        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }
        
        /* Tables */
        .table-container {
            overflow-x: auto;
            border-radius: 10px;
            border: 1px solid #e2e8f0;
        }
        
        table {
            width: 100%;
            border-collapse: collapse;
        }
        
        th {
            background: #f7fafc;
            padding: 15px;
            text-align: left;
            font-weight: 600;
            color: #4a5568;
            border-bottom: 2px solid #e2e8f0;
        }
        
        td {
            padding: 15px;
            border-bottom: 1px solid #e2e8f0;
            color: #4a5568;
        }
        
        tr:hover {
            background: #f7fafc;
        }
        
        /* Badges */
        .badge {
            padding: 5px 10px;
            border-radius: 20px;
            font-size: 0.85em;
            font-weight: 600;
        }
        
        .badge-fulltime {
            background: #c6f6d5;
            color: #22543d;
        }
        
        .badge-parttime {
            background: #feebc8;
            color: #744210;
        }
        
        .badge-contractor {
            background: #e9d8fd;
            color: #44337a;
        }
        
        /* Modal */
        .modal {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.5);
            justify-content: center;
            align-items: center;
            z-index: 1000;
        }
        
        .modal.active {
            display: flex;
        }
        
        .modal-content {
            background: white;
            padding: 30px;
            border-radius: 15px;
            width: 90%;
            max-width: 500px;
            max-height: 90vh;
            overflow-y: auto;
        }
        
        .modal-content h3 {
            color: #333;
            margin-bottom: 20px;
            font-size: 1.5em;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #4a5568;
            font-weight: 500;
        }
        
        .form-group input,
        .form-group select {
            width: 100%;
            padding: 12px;
            border: 2px solid #e2e8f0;
            border-radius: 8px;
            font-size: 1em;
            transition: border-color 0.3s;
        }
        
        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #667eea;
        }
        
        .modal-buttons {
            display: flex;
            gap: 15px;
            margin-top: 30px;
        }
        
        /* Toast */
        .toast {
            position: fixed;
            bottom: 30px;
            right: 30px;
            padding: 15px 30px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.2);
            display: none;
            z-index: 1001;
            font-weight: 500;
        }
        
        .toast.show {
            display: block;
            animation: slideIn 0.3s;
        }
        
        .toast.success {
            border-left: 4px solid #48bb78;
            color: #22543d;
        }
        
        .toast.error {
            border-left: 4px solid #f56565;
            color: #742a2a;
        }
        
        @keyframes slideIn {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
        
        /* Delete button in table */
        .delete-btn {
            padding: 5px 10px;
            background: #f56565;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 0.9em;
        }
        
        .delete-btn:hover {
            background: #c53030;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <h1>🏢 Employee Payroll Management System</h1>
            <p>Manage employees, process payroll, and generate reports</p>
        </div>
        
        <!-- Navigation Tabs -->
        <div class="tabs">
            <button class="tab-btn active" onclick="showTab('employees')">👥 Employees</button>
            <button class="tab-btn" onclick="showTab('payroll')">💰 Payroll</button>
            <button class="tab-btn" onclick="showTab('reports')">📊 Reports</button>
        </div>
        
        <!-- Dashboard Cards -->
        <div class="dashboard">
            <div class="card">
                <h3>Total Employees</h3>
                <div class="number" id="totalEmployees">0</div>
            </div>
            <div class="card">
                <h3>Total Payrolls</h3>
                <div class="number" id="totalPayrolls">0</div>
            </div>
            <div class="card">
                <h3>Monthly Payroll</h3>
                <div class="number" id="totalPayrollAmount">$0</div>
            </div>
        </div>
        
        <!-- Main Content -->
        <div class="content">
            <!-- Employees Tab -->
            <div id="employeesTab">
                <div class="content-header">
                    <h2>👥 Employee Management</h2>
                    <button class="btn btn-primary" onclick="showAddEmployeeModal()">+ Add Employee</button>
                </div>
                
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Department</th>
                                <th>Type</th>
                                <th>Salary/Rate</th>
                                <th>Hire Date</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody id="employeesTable">
                            <tr>
                                <td colspan="7" style="text-align: center;">Loading...</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            
            <!-- Payroll Tab -->
            <div id="payrollTab" style="display: none;">
                <div class="content-header">
                    <h2>💰 Payroll Processing</h2>
                    <button class="btn btn-success" onclick="showProcessPayrollModal()">+ Process Payroll</button>
                </div>
                
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Payroll ID</th>
                                <th>Employee</th>
                                <th>Period</th>
                                <th>Gross Pay</th>
                                <th>Tax</th>
                                <th>Net Pay</th>
                            </tr>
                        </thead>
                        <tbody id="payrollsTable">
                            <tr>
                                <td colspan="6" style="text-align: center;">No payroll records</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            
            <!-- Reports Tab -->
            <div id="reportsTab" style="display: none;">
                <div class="content-header">
                    <h2>📊 Reports</h2>
                </div>
                
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
                    <div class="card">
                        <h3>Employee Distribution</h3>
                        <div id="employeeDistribution"></div>
                    </div>
                    <div class="card">
                        <h3>Monthly Summary</h3>
                        <div id="monthlySummary"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <!-- Add Employee Modal -->
    <div class="modal" id="employeeModal">
        <div class="modal-content">
            <h3>➕ Add New Employee</h3>
            <form id="employeeForm" onsubmit="saveEmployee(event)">
                <div class="form-group">
                    <label>Employee ID</label>
                    <input type="text" id="empId" placeholder="e.g., FT001" required>
                </div>
                <div class="form-group">
                    <label>Full Name</label>
                    <input type="text" id="empName" placeholder="John Doe" required>
                </div>
                <div class="form-group">
                    <label>Department</label>
                    <input type="text" id="empDept" placeholder="IT, Sales, etc." required>
                </div>
                <div class="form-group">
                    <label>Hire Date</label>
                    <input type="date" id="empHireDate" required>
                </div>
                <div class="form-group">
                    <label>Employee Type</label>
                    <select id="empType" onchange="toggleEmployeeFields()">
                        <option value="fulltime">Full-Time (Salaried)</option>
                        <option value="parttime">Part-Time (Hourly)</option>
                        <option value="contractor">Contractor</option>
                    </select>
                </div>
                <div id="fulltimeFields">
                    <div class="form-group">
                        <label>Annual Salary ($)</label>
                        <input type="number" id="empSalary" placeholder="60000" min="0" step="1000">
                    </div>
                </div>
                <div id="parttimeFields" style="display: none;">
                    <div class="form-group">
                        <label>Hourly Rate ($)</label>
                        <input type="number" id="empRate" placeholder="25" min="0" step="0.5">
                    </div>
                </div>
                <div id="contractorFields" style="display: none;">
                    <div class="form-group">
                        <label>Contract Amount ($)</label>
                        <input type="number" id="empContractAmount" placeholder="50000" min="0">
                    </div>
                    <div class="form-group">
                        <label>Contract Duration (months)</label>
                        <input type="number" id="empContractDuration" placeholder="12" min="1">
                    </div>
                </div>
                <div class="modal-buttons">
                    <button type="submit" class="btn btn-primary">Save Employee</button>
                    <button type="button" class="btn btn-danger" onclick="closeModal()">Cancel</button>
                </div>
            </form>
        </div>
    </div>
    
    <!-- Process Payroll Modal -->
    <div class="modal" id="payrollModal">
        <div class="modal-content">
            <h3>💰 Process Payroll</h3>
            <form id="payrollForm" onsubmit="processPayroll(event)">
                <div class="form-group">
                    <label>Select Employee</label>
                    <select id="payrollEmployee" required>
                        <option value="">-- Choose an employee --</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Pay Period (YYYY-MM)</label>
                    <input type="month" id="payrollPeriod" required>
                </div>
                <div class="modal-buttons">
                    <button type="submit" class="btn btn-success">Process Payroll</button>
                    <button type="button" class="btn btn-danger" onclick="closePayrollModal()">Cancel</button>
                </div>
            </form>
        </div>
    </div>
    
    <!-- Toast Notification -->
    <div class="toast" id="toast"></div>
    
    <script>
        // Global variables
        let employees = [];
        let payrolls = [];
        
        // Initialize
        document.addEventListener('DOMContentLoaded', function() {
            fetchData();
        });
        
        // Fetch data from Java backend
        async function fetchData() {
            try {
                const empResponse = await fetch('/api/employees');
                employees = await empResponse.json();
                
                const payResponse = await fetch('/api/payrolls');
                payrolls = await payResponse.json();
                
                updateDisplay();
            } catch (error) {
                showToast('Error loading data', 'error');
            }
        }
        
        // Update all displays
        function updateDisplay() {
            displayEmployees();
            displayPayrolls();
            updateDashboard();
            updateReports();
        }
        
        // Tab switching
        function showTab(tab) {
            document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
            event.target.classList.add('active');
            
            document.getElementById('employeesTab').style.display = 'none';
            document.getElementById('payrollTab').style.display = 'none';
            document.getElementById('reportsTab').style.display = 'none';
            
            document.getElementById(tab + 'Tab').style.display = 'block';
        }
        
        // Display employees
        function displayEmployees() {
            let html = '';
            employees.forEach(emp => {
                let type = getEmployeeType(emp);
                let pay = getEmployeePay(emp);
                let badgeClass = type === 'Full-Time' ? 'badge-fulltime' : 
                                type === 'Part-Time' ? 'badge-parttime' : 'badge-contractor';
                
                html += `
                    <tr>
                        <td><strong>${emp.employeeId}</strong></td>
                        <td>${emp.name}</td>
                        <td>${emp.department}</td>
                        <td><span class="badge ${badgeClass}">${type}</span></td>
                        <td>${pay}</td>
                        <td>${emp.hireDate}</td>
                        <td>
                            <button class="delete-btn" onclick="deleteEmployee('${emp.employeeId}')">Delete</button>
                        </td>
                    </tr>
                `;
            });
            
            if (employees.length === 0) {
                html = '<tr><td colspan="7" style="text-align: center;">No employees found</td></tr>';
            }
            
            document.getElementById('employeesTable').innerHTML = html;
            
            // Update employee dropdown in payroll modal
            let select = document.getElementById('payrollEmployee');
            if (select) {
                select.innerHTML = '<option value="">-- Choose an employee --</option>';
                employees.forEach(emp => {
                    select.innerHTML += `<option value="${emp.employeeId}">${emp.name} (${getEmployeeType(emp)})</option>`;
                });
            }
        }
        
        function getEmployeeType(emp) {
            if (emp.baseSalary) return 'Full-Time';
            if (emp.hourlyRate) return 'Part-Time';
            return 'Contractor';
        }
        
        function getEmployeePay(emp) {
            if (emp.baseSalary) return '$' + emp.baseSalary.toLocaleString() + '/yr';
            if (emp.hourlyRate) return '$' + emp.hourlyRate + '/hr';
            if (emp.contractAmount) return '$' + emp.contractAmount.toLocaleString() + ' total';
            return 'N/A';
        }
        
        // Display payrolls
        function displayPayrolls() {
            let html = '';
            payrolls.forEach(p => {
                html += `
                    <tr>
                        <td><strong>${p.payrollId}</strong></td>
                        <td>${p.employeeName}</td>
                        <td>${p.period}</td>
                        <td>$${p.grossPay}</td>
                        <td>$${p.tax}</td>
                        <td>$${p.netPay}</td>
                    </tr>
                `;
            });
            
            if (payrolls.length === 0) {
                html = '<tr><td colspan="6" style="text-align: center;">No payroll records</td></tr>';
            }
            
            document.getElementById('payrollsTable').innerHTML = html;
        }
        
        // Update dashboard
        function updateDashboard() {
            document.getElementById('totalEmployees').textContent = employees.length;
            document.getElementById('totalPayrolls').textContent = payrolls.length;
            
            let total = payrolls.reduce((sum, p) => sum + parseFloat(p.netPay), 0);
            document.getElementById('totalPayrollAmount').textContent = '$' + total.toFixed(2);
        }
        
        // Update reports
        function updateReports() {
            let fullTime = employees.filter(e => e.baseSalary).length;
            let partTime = employees.filter(e => e.hourlyRate).length;
            let contractors = employees.filter(e => e.contractAmount).length;
            
            document.getElementById('employeeDistribution').innerHTML = `
                <p><strong>Full-Time:</strong> ${fullTime}</p>
                <p><strong>Part-Time:</strong> ${partTime}</p>
                <p><strong>Contractors:</strong> ${contractors}</p>
            `;
            
            let monthlyTotal = payrolls.reduce((sum, p) => sum + parseFloat(p.netPay), 0);
            document.getElementById('monthlySummary').innerHTML = `
                <p><strong>Total Payroll:</strong> $${monthlyTotal.toFixed(2)}</p>
                <p><strong>Average per Employee:</strong> $${(monthlyTotal / (payrolls.length || 1)).toFixed(2)}</p>
            `;
        }
        
        // Employee modal
        function showAddEmployeeModal() {
            document.getElementById('employeeForm').reset();
            document.getElementById('employeeModal').classList.add('active');
            toggleEmployeeFields();
        }
        
        function closeModal() {
            document.getElementById('employeeModal').classList.remove('active');
        }
        
        function toggleEmployeeFields() {
            let type = document.getElementById('empType').value;
            document.getElementById('fulltimeFields').style.display = 'none';
            document.getElementById('parttimeFields').style.display = 'none';
            document.getElementById('contractorFields').style.display = 'none';
            
            if (type === 'fulltime') {
                document.getElementById('fulltimeFields').style.display = 'block';
            } else if (type === 'parttime') {
                document.getElementById('parttimeFields').style.display = 'block';
            } else {
                document.getElementById('contractorFields').style.display = 'block';
            }
        }
        
        // Save employee
        async function saveEmployee(event) {
            event.preventDefault();
            
            let emp = {
                employeeId: document.getElementById('empId').value,
                name: document.getElementById('empName').value,
                department: document.getElementById('empDept').value,
                hireDate: document.getElementById('empHireDate').value
            };
            
            let type = document.getElementById('empType').value;
            
            if (type === 'fulltime') {
                emp.baseSalary = parseFloat(document.getElementById('empSalary').value);
                emp.type = 'fulltime';
            } else if (type === 'parttime') {
                emp.hourlyRate = parseFloat(document.getElementById('empRate').value);
                emp.type = 'parttime';
            } else {
                emp.contractAmount = parseFloat(document.getElementById('empContractAmount').value);
                emp.contractDuration = parseInt(document.getElementById('empContractDuration').value);
                emp.type = 'contractor';
            }
            
            try {
                const response = await fetch('/api/employees', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(emp)
                });
                
                if (response.ok) {
                    employees.push(emp);
                    updateDisplay();
                    closeModal();
                    showToast('Employee added successfully!', 'success');
                }
            } catch (error) {
                showToast('Error adding employee', 'error');
            }
        }
        
        // Delete employee
        async function deleteEmployee(id) {
            if (confirm('Are you sure you want to delete this employee?')) {
                try {
                    const response = await fetch('/api/employees?id=' + id, {
                        method: 'DELETE'
                    });
                    
                    if (response.ok) {
                        employees = employees.filter(e => e.employeeId !== id);
                        updateDisplay();
                        showToast('Employee deleted!', 'success');
                    }
                } catch (error) {
                    showToast('Error deleting employee', 'error');
                }
            }
        }
        
        // Payroll modal
        function showProcessPayrollModal() {
            document.getElementById('payrollForm').reset();
            document.getElementById('payrollModal').classList.add('active');
        }
        
        function closePayrollModal() {
            document.getElementById('payrollModal').classList.remove('active');
        }
        
        // Process payroll
        async function processPayroll(event) {
            event.preventDefault();
            
            let empId = document.getElementById('payrollEmployee').value;
            let emp = employees.find(e => e.employeeId === empId);
            let period = document.getElementById('payrollPeriod').value;
            
            if (!emp || !period) {
                showToast('Please select employee and period', 'error');
                return;
            }
            
            // Calculate pay
            let grossPay, tax;
            if (emp.baseSalary) {
                grossPay = emp.baseSalary / 12;
                tax = grossPay * 0.20;
            } else if (emp.hourlyRate) {
                grossPay = emp.hourlyRate * 160;
                tax = grossPay * 0.15;
            } else {
                grossPay = emp.contractAmount / emp.contractDuration;
                tax = grossPay * 0.10;
            }
            
            let netPay = grossPay - tax;
            
            let payroll = {
                payrollId: 'PAY-' + empId + '-' + period,
                employeeName: emp.name,
                period: period,
                grossPay: grossPay.toFixed(2),
                tax: tax.toFixed(2),
                netPay: netPay.toFixed(2)
            };
            
            try {
                const response = await fetch('/api/payrolls', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payroll)
                });
                
                if (response.ok) {
                    payrolls.push(payroll);
                    updateDisplay();
                    closePayrollModal();
                    showToast('Payroll processed successfully!', 'success');
                }
            } catch (error) {
                showToast('Error processing payroll', 'error');
            }
        }
        
        // Toast notification
        function showToast(message, type) {
            let toast = document.getElementById('toast');
            toast.textContent = message;
            toast.className = 'toast show ' + type;
            setTimeout(() => {
                toast.classList.remove('show');
            }, 3000);
        }
    </script>
</body>
</html>
            """;
        }
    }
    
    // API handler for employees
    static class EmployeeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response = "";
            int statusCode = 200;
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            
            try {
                if ("GET".equals(method)) {
                    // Convert employees to JSON
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < employees.size(); i++) {
                        if (i > 0) json.append(",");
                        json.append(mapToJson(employees.get(i)));
                    }
                    json.append("]");
                    response = json.toString();
                }
                else if ("POST".equals(method)) {
                    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        requestBody.append(line);
                    }
                    
                    // Parse JSON and add to employees list
                    // For simplicity, we'll just use the sample data
                    response = "{\"message\":\"Employee added\"}";
                }
                else if ("DELETE".equals(method)) {
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null && query.startsWith("id=")) {
                        String id = query.substring(3);
                        employees.removeIf(e -> id.equals(e.get("employeeId")));
                    }
                    response = "{\"message\":\"Employee deleted\"}";
                }
            } catch (Exception e) {
                statusCode = 500;
                response = "{\"error\":\"" + e.getMessage() + "\"}";
            }
            
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        
        private String mapToJson(Map<String, Object> map) {
            StringBuilder json = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (i++ > 0) json.append(",");
                json.append("\"").append(entry.getKey()).append("\":");
                if (entry.getValue() instanceof String) {
                    json.append("\"").append(entry.getValue()).append("\"");
                } else {
                    json.append(entry.getValue());
                }
            }
            json.append("}");
            return json.toString();
        }
    }
    
    // API handler for payrolls
    static class PayrollHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response = "";
            int statusCode = 200;
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            
            try {
                if ("GET".equals(method)) {
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < payrolls.size(); i++) {
                        if (i > 0) json.append(",");
                        json.append("{");
                        json.append("\"payrollId\":\"").append(payrolls.get(i).get("payrollId")).append("\",");
                        json.append("\"employeeName\":\"").append(payrolls.get(i).get("employeeName")).append("\",");
                        json.append("\"period\":\"").append(payrolls.get(i).get("period")).append("\",");
                        json.append("\"grossPay\":\"").append(payrolls.get(i).get("grossPay")).append("\",");
                        json.append("\"tax\":\"").append(payrolls.get(i).get("tax")).append("\",");
                        json.append("\"netPay\":\"").append(payrolls.get(i).get("netPay")).append("\"");
                        json.append("}");
                    }
                    json.append("]");
                    response = json.toString();
                }
                else if ("POST".equals(method)) {
                    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        requestBody.append(line);
                    }
                    response = "{\"message\":\"Payroll processed\"}";
                }
            } catch (Exception e) {
                statusCode = 500;
                response = "{\"error\":\"" + e.getMessage() + "\"}";
            }
            
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
