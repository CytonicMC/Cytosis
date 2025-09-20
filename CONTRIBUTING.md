# Java Code Style Guide

*Version 1.0*

## Table of Contents
1. [General Principles](#general-principles)
2. [Formatting](#formatting)
3. [Naming Conventions](#naming-conventions)
4. [Code Organization](#code-organization)
5. [Comments and Documentation](#comments-and-documentation)
6. [Best Practices](#best-practices)
7. [IntelliJ IDEA Configuration](#intellij-idea-configuration)
8. [Automated Enforcement](#automated-enforcement)

## General Principles

- **Consistency is key** - Follow the established patterns in the codebase
- **Readability first** - Code is read far more often than it's written
- **Keep it simple** - Prefer clear, straightforward solutions over clever ones
- **Be explicit** - Make your intentions clear through naming and structure

## Formatting

### Indentation and Spacing
- Use **4 spaces** for indentation (never tabs)
- Maximum line length: **120 characters**
- Use single spaces around operators: `int result = a + b;`
- No trailing whitespace on any line

### Braces
Use **K&R style** (opening brace on same line):
```java
// Good
if (condition) {
    doSomething();
} else {
    doSomethingElse();
}

// Bad
if (condition) 
{
    doSomething();
} 
else 
{
    doSomethingElse();
}
```

### Line Breaks
- Break long method calls and chains logically:
```java
// Good
String result = someObject
    .method1()
    .method2(parameter1, parameter2)
    .method3();

// Good - parameters on new lines when many
public void longMethodName(String firstParameter,
                          String secondParameter,
                          int thirdParameter) {
    // method body
}
```

## Naming Conventions

### Classes and Interfaces
- Use **PascalCase**: `UserService`, `PaymentProcessor`
- Classes: Nouns that describe what they represent
- Interfaces: Either nouns or adjectives ending in "-able": `Runnable`, `UserRepository`

### Methods
- Use **camelCase**: `getUserById()`, `calculateTotal()`
- Start with verbs that describe what they do
- Boolean methods should ask questions: `isEmpty()`, `hasPermission()`, `canProcess()`

### Variables
- Use **camelCase**: `userName`, `totalAmount`
- Be descriptive but concise: `user` not `u`, but `userId` not `userIdentificationNumber`
- Avoid abbreviations unless they're widely understood: `id`, `url`, `html`

### Constants
- Use **UPPER_SNAKE_CASE**: `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT`
- Must be `static final`

### Packages
- Use **lowercase** with dots: `net.cytonic.project.service`
- Use plural nouns: `utils` not `util`, `models` not `model`

## Code Organization

### Package Structure
Organize by feature, not by layer:
```
net.cytonic.project/
├── users/
│   ├── User.java
│   ├── UserService.java
│   ├── UserController.java
│   └── UserRepository.java
├── payments/
│   ├── Payment.java
│   └── PaymentService.java
└── commons/
    ├── exceptions/
    └── utils/
```

### Class Member Order
1. Static fields (constants first)
2. Instance fields
3. Constructors
4. Methods (public before private)
5. Static methods
6. Inner classes

### Import Organization
- Avoid wildcard imports where it makes sense (`import java.util.*;`)
- Group imports: Java standard library, then third-party, then our own
- Remove unused imports

## Comments and Documentation

### When to Comment
- **Why**, not what: Explain the reasoning behind non-obvious code
- Complex logic or algorithms
- Workarounds or temporary solutions (with TODO/FIXME)

### Javadoc
Use for all public methods and classes:
```java
/**
 * Calculates the total price including tax for the given items.
 * 
 * @param items the items to calculate total for
 * @param taxRate the tax rate to apply (e.g., 0.08 for 8%)
 * @return the total price including tax
 * @throws IllegalArgumentException if taxRate is negative
 */
public BigDecimal calculateTotalWithTax(List<Item> items, double taxRate) {
    // implementation
}
```

### Inline Comments
```java
// Good - explains why
// Using HashMap for O(1) lookup performance with large datasets
Map<String, User> userCache = new HashMap<>();

// Bad - explains what (obvious from code)
// Create a new HashMap
Map<String, User> userCache = new HashMap<>();
```

## Best Practices

### Method Length
- Keep methods short and focused (generally under 20 lines)
- If longer, consider breaking into smaller methods

### Variable Declaration
- Declare variables close to where they're used
- Initialize variables when declaring when possible
- Use `final` for variables that won't change

### Error Handling
- Don't catch and ignore exceptions
- Use specific exception types, not generic `Exception`
- Log errors with context

### Modern Java Features
- Do not use `var` for variables even when type is obvious: `var users = getUserList();`
- Use enhanced for-loops when possible: `for (User user : users)`
- Consider streams for data processing, but don't overuse

## IntelliJ IDEA Configuration

To configure IntelliJ IDEA to match this style:

1. **File → Settings → Editor → Code Style → Java**
2. Set **Tab size** and **Indent** to 4
3. Set **Right margin** to 120
4. Under **Wrapping and Braces**, set brace placement to "End of line"
5. **File → Settings → Editor → General → Auto Import**:
   - Uncheck "Use single class import"
   - Set "Class count to use import with '*'" to 99

## Automated Enforcement

We'll implement Checkstyle via GitHub Actions. The configuration will enforce:
- Indentation and spacing rules
- Line length limits
- Naming conventions
- Import organization
- Basic code structure rules

*Note: Detailed GitHub Actions configuration and Checkstyle rules will be added in the next iteration.*

---

## Quick Reference Checklist

When writing code, ask yourself:
- [ ] Are my variable and method names descriptive?
- [ ] Is my formatting consistent with the rest of the file?
- [ ] Are my methods focused on a single responsibility?
- [ ] Have I documented non-obvious behavior?
- [ ] Are imports organized and unused ones removed?
- [ ] Does this code follow the existing patterns in the project?

---

*This document is a living guide. As our team grows and learns, we'll update it together.*
