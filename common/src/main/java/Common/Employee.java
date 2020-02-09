package Common;

import org.bson.Document;

import java.util.Date;
/**
 * author Ciobanu Eduard David
 */
public class Employee {

    public enum EmployeeJob {
        CHELNER,
        BUCATAR,
        BARMAN,
        PIZZETAR,
        AJUTOR_BUCATAR,
        LIVRARI;

        public static EmployeeJob map(String job_s){
            switch (job_s){
                case "Chelner": return CHELNER;
                case "Bucatar": return BUCATAR;
                case "Barman": return BARMAN;
                case "Pizzetar": return PIZZETAR;
                case "Ajutor_Bucatar": return AJUTOR_BUCATAR;
                default: return LIVRARI;
            }
        }
    }

    private int emp_id;
    private String name;
    private EmployeeJob job;
    private Date hire_date;
    private String account_name;
    private String password;

    public Employee(int emp_id, String name, EmployeeJob job, Date hire_date, String account_name, String password) {
        this.emp_id = emp_id;
        this.name = name;
        this.job = job;
        this.hire_date = hire_date;
        this.account_name = account_name;
        this.password = password;
    }

    public Employee(Document docEmp) throws Exception {
        if(docEmp == null)
            throw new Exception("'docEmp' is null!");

        if (docEmp.getInteger("emp_id") == null){
            throw new Exception("'emp_id' does not exist in document.");
        }
        this.emp_id = docEmp.getInteger("emp_id");

        if (docEmp.getString("name") == null){
            throw new Exception("'name' does not exist in document.");
        }
        this.name = docEmp.getString("name");

        if (docEmp.getString("job") == null){
            throw new Exception("'job' does not exist in document.");
        }
        this.job = EmployeeJob.map(docEmp.getString("job"));

        if (docEmp.getDate("hire_date") == null){
            throw new Exception("'hire_date' does not exist in document.");
        }
        this.hire_date = docEmp.getDate("hire_date");

        if (docEmp.getString("account_name") == null){
            throw new Exception("'account_name' does not exist in document.");
        }
        this.account_name = docEmp.getString("account_name");

        if (docEmp.getString("password") == null){
            throw new Exception("'password' does not exist in document.");
        }
        this.password = docEmp.getString("password");
    }

    public int getEmp_id() {
        return emp_id;
    }

    public String getName() {
        return name;
    }

    public EmployeeJob getJob() {
        return job;
    }

    public Date getHire_date() {
        return hire_date;
    }

    public String getAccount_name() {
        return account_name;
    }

    public String getPassword() {
        return password;
    }
}
