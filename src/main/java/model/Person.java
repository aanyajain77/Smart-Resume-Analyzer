package model;

/**
 * Abstract class representing a general Person.
 * Demonstrates:
 * 1. Abstraction (abstract class and abstract method displayDetails())
 * 2. Encapsulation (private fields, public getters and setters)
 * 3. Constructor Overloading (default and parameterized constructors)
 * 4. Method Overloading (updateContact with 1 parameter vs 2 parameters)
 * 5. Use of 'this' keyword
 */
public abstract class Person {
    private String name;
    private String email;
    private String phone;

    // Default Constructor
    public Person() {
        this.name = "";
        this.email = "";
        this.phone = "";
    }

    // Parameterized Constructor
    public Person(String name, String email, String phone) {
        this.name = name != null ? name.trim() : "";
        this.email = email != null ? email.trim().toLowerCase() : "";
        this.phone = phone != null ? phone.trim() : "";
    }

    // Getters and Setters (Encapsulation)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : "";
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : "";
    }

    // Method Overloading 1: Update email only
    public void updateContact(String newEmail) {
        if (newEmail != null && !newEmail.trim().isEmpty()) {
            this.email = newEmail.trim().toLowerCase();
        }
    }

    // Method Overloading 2: Update email and phone
    public void updateContact(String newEmail, String newPhone) {
        updateContact(newEmail);
        if (newPhone != null && !newPhone.trim().isEmpty()) {
            this.phone = newPhone.trim();
        }
    }

    // Abstract method to be implemented by subclasses (Polymorphism)
    public abstract void displayDetails();

    @Override
    public String toString() {
        return String.format("Person [Name='%s', Email='%s', Phone='%s']", name, email, phone);
    }
}
