# VaultPay - Digital Wallet & Payment Backend API

A secure and scalable REST API backend for a digital wallet and payment platform built with Spring Boot. VaultPay enables peer-to-peer money transfers, Stripe-powered deposits, and comprehensive wallet management with enterprise-grade security.

##  Features

### Authentication & Security
- **JWT Authentication**: Secure access and refresh token implementation
- **Email Verification**: OTP-based email verification during registration
- **Password Reset**: Secure password reset flow with OTP verification
- **PIN Protection**: Transaction authorization with secure wallet PIN
- **Spring Security**: Custom security configuration with JWT filters

### Wallet Management
- **Digital Wallet**: Automatic wallet creation for each user
- **Balance Tracking**: Real-time wallet balance management
- **PIN Setup**: Secure PIN configuration with OTP verification
- **Balance Inquiry**: Retrieve current wallet balance

### Payment & Transactions
- **P2P Transfers**: Send money to other users by username
- **Transaction History**: Paginated transaction history with details
- **Transaction Remarks**: Add notes/descriptions to transfers
- **Stripe Deposits**: Add funds to wallet via Stripe Payment Intents
- **Deposit History**: Track all deposit transactions with pagination

### Stripe Integration
- **Customer Management**: Automatic Stripe customer creation
- **Payment Intents**: Secure deposit processing
- **Webhook Handling**: Real-time payment confirmation via webhooks
- **Automatic Updates**: Wallet balance updates on successful payments

### Email Notifications
- **Registration OTP**: Email verification codes
- **PIN Setup OTP**: Secure PIN configuration verification
- **Password Reset OTP**: Password recovery codes
- **Async Processing**: Non-blocking email delivery

##  Tech Stack

- **Java 21**
- **Spring Boot 4.0.3**
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database operations
- **PostgreSQL** - Primary database
- **JWT (JJWT 0.12.7)** - Token-based authentication
- **Stripe API (v32.0.0)** - Payment processing
- **Spring Mail** - Email notifications
- **Lombok** - Code simplification
- **Maven** - Dependency management

##  Prerequisites

- Java 21 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher
- Stripe Account (for payment features)
- Gmail Account (for email notifications)

## ⚙️ Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd VaultPay
```

### 2. Configure PostgreSQL
Create a PostgreSQL database:
```sql
CREATE DATABASE vaultpay;
```

### 3. Configure Application Properties
Update `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/vaultpay
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

# Email Configuration
spring.mail.username=YOUR_EMAIL@gmail.com
spring.mail.password=YOUR_APP_PASSWORD
app.email.from=YOUR_EMAIL@gmail.com

# JWT Configuration
jwt.secret=YOUR_SECRET_KEY_HERE
jwt.expiration=900000
jwt.refresh.expiration=604800000

# Stripe Configuration
stripe.api.key=YOUR_STRIPE_SECRET_KEY
stripe.webhook.secret=YOUR_STRIPE_WEBHOOK_SECRET
```

### 4. Build the Project
```bash
mvn clean install
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

##  API Documentation

### Authentication Endpoints

#### Register User
```http
POST /register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Verify Email
```http
POST /verify
Content-Type: application/json

{
  "email": "john@example.com",
  "otp": "123456"
}
```

#### Login
```http
POST /login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "SecurePass123"
}
```

#### Refresh Token
```http
POST /refresh
Authorization: Bearer <refresh_token>
```

#### Logout
```http
POST /logout
Authorization: Bearer <access_token>
```

#### Resend OTP
```http
POST /resend-otp
Content-Type: application/json

{
  "email": "john@example.com"
}
```

#### Request Password Reset
```http
POST /resetPassword-Request
Content-Type: application/json

{
  "email": "john@example.com"
}
```

#### Reset Password
```http
POST /resetPassword
Content-Type: application/json

{
  "email": "john@example.com",
  "otp": "123456",
  "newPassword": "NewSecurePass123"
}
```

### Wallet Endpoints

#### Get Wallet Balance
```http
GET /get-wallet
Authorization: Bearer <access_token>
```

#### Request PIN Setup OTP
```http
POST /set-pin-request
Authorization: Bearer <access_token>
```

#### Set Wallet PIN
```http
POST /set-pin
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "otp": "123456",
  "pin": "1234"
}
```

### Transaction Endpoints

#### Transfer Money
```http
POST /transfer
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "toUserName": "janedoe",
  "amount": 100.00,
  "remarks": "Payment for lunch",
  "pin": "1234"
}
```

#### Get Transaction History
```http
GET /transaction-history?page=0&size=10
Authorization: Bearer <access_token>
```

### Deposit Endpoints (Stripe)

#### Create Deposit Intent
```http
POST /create-intent
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "amount": 500.00,
  "currency": "inr"
}
```

#### Get Deposit History
```http
GET /history?page=0&size=10
Authorization: Bearer <access_token>
```

### Stripe Webhook
```http
POST /webhook/stripe
Stripe-Signature: <stripe_signature>
Content-Type: application/json
```

##  Security Features

- **Password Encryption**: BCrypt password hashing
- **JWT Tokens**: Stateless authentication with access and refresh tokens
- **OTP Verification**: Time-limited OTPs (5 minutes expiration)
- **PIN Protection**: Encrypted wallet PIN for transaction authorization
- **Webhook Verification**: Stripe signature verification for webhooks

##  Database Schema

The application uses the following main entities:
- **User**: User account information
- **Wallet**: User wallet with balance and PIN
- **Transaction**: P2P transfer records
- **Deposit**: Stripe deposit records
- **RefreshToken**: JWT refresh tokens
- **OtpVerification**: Email verification OTPs
- **PasswordReset**: Password reset OTPs
- **SetPin**: PIN setup OTPs
- **StripeCustomer**: Stripe customer mapping
- **StripeWebhookEvent**: Webhook event tracking

##  Workflow Examples

### User Registration Flow
1. User registers with email and password
2. System sends OTP to email
3. User verifies email with OTP
4. Wallet is automatically created
5. User can now login

### Money Transfer Flow
1. User sets up wallet PIN (one-time)
2. User initiates transfer with recipient username, amount, and PIN
3. System validates PIN and sufficient balance
4. Amount is debited from sender and credited to recipient
5. Transaction record is created

### Deposit Flow
1. User requests deposit intent with amount
2. System creates Stripe Payment Intent
3. Frontend processes payment with Stripe
4. Stripe sends webhook confirmation
5. System updates wallet balance automatically

##  Testing

Run tests with:
```bash
mvn test
```

##  Configuration Notes

### Email Setup (Gmail)
1. Enable 2-Factor Authentication on your Gmail account
2. Generate an App Password: Google Account → Security → App Passwords
3. Use the generated password in `application.properties`

### Stripe Setup
1. Create a Stripe account at https://stripe.com
2. Get your API keys from Dashboard → Developers → API Keys
3. Set up webhook endpoint: Dashboard → Developers → Webhooks
4. Add webhook URL: `https://your-domain.com/webhook/stripe`
5. Subscribe to event: `payment_intent.succeeded`

### JWT Secret Generation
Generate a secure secret key:
```bash
openssl rand -base64 64
```

##  Future Enhancements

- [ ] Multi-currency support
- [ ] Transaction analytics dashboard
- [ ] Scheduled payments
- [ ] Transaction limits and fraud detection
- [ ] QR code payments
- [ ] Payment request feature
- [ ] Transaction categories and tags
- [ ] Export transaction history (PDF/CSV)
- [ ] Two-factor authentication (2FA)
- [ ] Biometric authentication support

##  Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

##  Author

- GitHub: @ZahiqIbrahim(https://github.com/ZahiqIbrahim)
- LinkedIn: https://www.linkedin.com/in/zahiq-ibrahim-414a23280/


---

**Note**: This is a learning/portfolio project. For production use, additional security measures, testing, and compliance considerations are required.
