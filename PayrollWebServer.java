import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PayrollServer {
    
    // Data storage
    private static List<Map<String, Object>> employees = new ArrayList<>();
    private static List<Map<String, Object>> payrolls = new ArrayList<>();
    
    public static void main(String[] args) throws IOException {
        // Load sample data
        loadSampleData();
        
        // Create server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Serve static files (HTML, CSS, JS)
        server.createContext("/", new StaticFileHandler());
        
        // API endpoints
        server.createContext("/api/employees", new EmployeeHandler());
        server.createContext("/api/payrolls", new PayrollHandler());
        
        server.setExecutor(null);
        System.out.println("=".repeat(60));
        System.out.println("✅ EMPLOYEE PAYROLL SERVER STARTED");
        System.out.println("=".repeat(60));
        System.out.println("📁 Frontend: http://localhost:8080/index.html");
        System.out.println("📊 API: http://localhost:8080/api/employees");
        System.out.println("💰 API: http://localhost:8080/api/payrolls");
        System.out.println("=".repeat(60));
        System.out.println("Press Ctrl+C to stop the server");
        
        server.start();
    }
    
    private static void loadSampleData() {
        // Full-time employee
        Map<String, Object> emp1 = new HashMap<>();
        emp1.put("id", "FT001");
        emp1.put("name", "John Doe");
        emp1.put("department", "IT");
        emp1.put("type", "fulltime");
        emp1.put("salary", 60000.0);
        emp1.put("hireDate", "2024-01-15");
        employees.add(emp1);
        
        // Part-time employee
        Map<String, Object> emp2 = new HashMap<>();
        emp2.put("id", "PT001");
        emp2.put("name", "Jane Smith");
        emp2.put("department", "Sales");
        emp2.put("type", "parttime");
        emp2.put("salary", 25.0);
        emp2.put("hireDate", "2024-02-01");
        employees.add(emp2);
        
        // Contractor
        Map<String, Object> emp3 = new HashMap<>();
        emp3.put("id", "CT001");
        emp3.put("name", "Mike Johnson");
        emp3.put("department", "Consulting");
        emp3.put("type", "contractor");
        emp3.put("salary", 50000.0);
        emp3.put("duration", 12);
        emp3.put("hireDate", "2024-01-01");
        employees.add(emp3);
    }
    
    // Handler for static files (HTML, CSS, JS)
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Default to index.html
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            // Serve file from resources
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is == null) {
                    String response = "404 - File not found";
                    exchange.sendResponseHeaders(404, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    return;
                }
                
                // Set content type based on file extension
                String contentType = getContentType(path);
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, 0);
                
                // Copy file to response
                OutputStream os = exchange.getResponseBody();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.close();
            }
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg")) return "image/jpeg";
            return "text/plain";
        }
    }
    
    // Handler for employee API
    static class EmployeeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response = "";
            int statusCode = 200;
            
            // Set CORS headers
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            
            // Handle preflight requests
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            
            try {
                if ("GET".equals(method)) {
                    // Return all employees as JSON
                    response = employeesToJson();
                }
                else if ("POST".equals(method)) {
                    // Add new employee
                    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        requestBody.append(line);
                    }
                    
                    // Parse JSON and add to list
                    Map<String, Object> newEmp = parseEmployeeJson(requestBody.toString());
                    employees.add(newEmp);
                    
                    response = "{\"status\":\"success\",\"message\":\"Employee added\"}";
                }
                else if ("DELETE".equals(method)) {
                    // Delete employee
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null && query.startsWith("id=")) {
                        String id = query.substring(3);
                        employees.removeIf(emp -> id.equals(emp.get("id")));
                    }
                    response = "{\"status\":\"success\",\"message\":\"Employee deleted\"}";
                }
            } catch (Exception e) {
                statusCode = 500;
                response = "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
            }
            
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        
        private String employeesToJson() {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < employees.size(); i++) {
                if (i > 0) json.append(",");
                json.append("{");
                json.append("\"id\":\"").append(employees.get(i).get("id")).append("\",");
                json.append("\"name\":\"").append(employees.get(i).get("name")).append("\",");
                json.append("\"department\":\"").append(employees.get(i).get("department")).append("\",");
                json.append("\"type\":\"").append(employees.get(i).get("type")).append("\",");
                json.append("\"salary\":").append(employees.get(i).get("salary")).append(",");
                json.append("\"hireDate\":\"").append(employees.get(i).get("hireDate")).append("\"");
                json.append("}");
            }
            json.append("]");
            return json.toString();
        }
        
        private Map<String, Object> parseEmployeeJson(String json) {
            Map<String, Object> emp = new HashMap<>();
            // Simple parsing - in real app use a JSON library
            json = json.replace("{", "").replace("}", "").replace("\"", "");
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    emp.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
            return emp;
        }
    }
    
    // Handler for payroll API
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
                        json.append("\"id\":\"").append(payrolls.get(i).get("id")).append("\",");
                        json.append("\"employee\":\"").append(payrolls.get(i).get("employee")).append("\",");
                        json.append("\"month\":\"").append(payrolls.get(i).get("month")).append("\",");
                        json.append("\"gross\":").append(payrolls.get(i).get("gross")).append(",");
                        json.append("\"tax\":").append(payrolls.get(i).get("tax")).append(",");
                        json.append("\"net\":").append(payrolls.get(i).get("net"));
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
                    
                    // Simple response
                    response = "{\"status\":\"success\",\"message\":\"Payroll processed\"}";
                }
            } catch (Exception e) {
                statusCode = 500;
                response = "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
            }
            
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
