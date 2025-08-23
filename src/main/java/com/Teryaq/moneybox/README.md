# Money Box System - Teryaq Pharmacy Management

## Overview

The Money Box System is a comprehensive cash management solution designed specifically for Syrian pharmacies. It tracks all cash movements throughout the day, providing real-time visibility into cash balances and complete audit trails.

## Features

### 🏦 **Daily Cash Management**
- **Open/Close Money Box**: Start and end each business day with proper cash reconciliation
- **Real-time Balance**: Always know exactly how much cash you have
- **Cash Reconciliation**: End-of-day verification to prevent discrepancies

### 💰 **Transaction Tracking**
- **Cash Sales**: Automatically track cash received from customers
- **Cash Purchases**: Record cash paid to suppliers
- **Cash Refunds**: Handle customer returns with proper cash tracking
- **Cash Withdrawals**: Track money taken from the box (bank deposits, expenses)
- **Cash Deposits**: Record money added to the box (bank withdrawals, etc.)

### 🌍 **Multi-Currency Support**
- **Primary Currency**: SYP (Syrian Pound)
- **Secondary Currencies**: USD, EUR
- **Automatic Conversion**: All amounts stored in SYP for consistency
- **Exchange Rate Management**: Configurable rates for accurate conversions

### 🔒 **Security & Control**
- **Role-based Access**: Only authorized employees can access cash operations
- **Audit Trail**: Complete history of all cash movements
- **Approval Workflows**: Large transactions require manager approval
- **Receipt Tracking**: All cash movements require documentation

## How It Works

### 1. **Morning Opening**
```bash
POST /api/v1/money-box/open
{
  "pharmacyId": 1,
  "openingBalance": 50000,
  "notes": "Opening balance for today"
}
```

### 2. **During Business Hours**
- **Cash Sales**: Automatically recorded when sales are processed
- **Cash Purchases**: Recorded when paying suppliers
- **Manual Transactions**: Withdrawals, deposits, adjustments

### 3. **End of Day**
```bash
POST /api/v1/money-box/{id}/close?actualBalance=60000&notes=End of day reconciliation
```

## API Endpoints

### **Money Box Management**
- `POST /api/v1/money-box/open` - Open new money box
- `POST /api/v1/money-box/{id}/close` - Close and reconcile money box
- `GET /api/v1/money-box/{id}/summary` - Get current status and totals
- `GET /api/v1/money-box/pharmacy/{pharmacyId}/current` - Get current open money box

### **Transaction Management**
- `POST /api/v1/money-box/{id}/transactions` - Add general transaction
- `POST /api/v1/money-box/{id}/withdrawal` - Record cash withdrawal
- `POST /api/v1/money-box/{id}/deposit` - Record cash deposit
- `GET /api/v1/money-box/{id}/transactions` - Get all transactions

## Business Workflow

### **Daily Operations**
1. **Open Money Box** → Set opening balance
2. **Process Transactions** → Sales, purchases, withdrawals, deposits
3. **Monitor Balance** → Real-time cash position
4. **Close Money Box** → Reconcile and verify

### **Cash Flow Example**
```
Opening Balance: 50,000 SYP
+ Cash Sales: +15,000 SYP
- Cash Purchase: -8,000 SYP
- Withdrawal: -5,000 SYP
= Expected Balance: 52,000 SYP
```

## Integration Points

### **Sales System**
- Automatic cash tracking for cash sales
- Reference linking to sale invoices
- Real-time balance updates

### **Purchase System**
- Cash payment tracking to suppliers
- Reference linking to purchase invoices
- Expense categorization

### **Customer Returns**
- Refund processing with cash tracking
- Return reason documentation
- Inventory restoration

## Security Features

### **Access Control**
- JWT authentication required
- Role-based permissions
- Employee tracking for all operations

### **Audit Requirements**
- Complete transaction history
- User identification for all operations
- Timestamp tracking
- Reference documentation

## Configuration

### **Exchange Rates**
- Configurable rates for SYP/USD/EUR
- Automatic conversion to SYP base currency
- Rate update capabilities

### **Approval Thresholds**
- Configurable amounts requiring approval
- Manager authorization workflows
- Transaction limits

## Error Handling

### **Common Scenarios**
- **No Open Money Box**: Cannot process cash transactions
- **Insufficient Cash**: Prevent negative balances
- **Invalid References**: Ensure proper documentation
- **Reconciliation Discrepancies**: Flag for investigation

## Best Practices

### **Daily Operations**
1. **Always open money box** before starting business
2. **Record all cash movements** immediately
3. **Reconcile daily** to catch discrepancies early
4. **Document all transactions** with proper references

### **Cash Management**
1. **Set appropriate opening balances** based on business needs
2. **Monitor cash flow** throughout the day
3. **Investigate discrepancies** immediately
4. **Maintain proper documentation** for all movements

## Troubleshooting

### **Common Issues**
- **Money box won't open**: Check if one is already open for today
- **Transaction fails**: Verify money box is open and has sufficient balance
- **Balance mismatch**: Review all transactions and reconciliation process

### **Support**
- Check application logs for detailed error messages
- Verify database connectivity and table structure
- Ensure proper authentication and permissions

## Future Enhancements

### **Planned Features**
- **Multi-period Aggregation**: Weekly, monthly, yearly summaries
- **Advanced Reporting**: Cash flow analysis and forecasting
- **Mobile Access**: Cash operations on mobile devices
- **Integration APIs**: External system connectivity

### **Business Intelligence**
- **Cash Flow Patterns**: Identify trends and optimize cash management
- **Performance Metrics**: Employee cash handling efficiency
- **Risk Analysis**: Identify potential cash management risks

---

## Quick Start

1. **Start the application** - Money box system will be available
2. **Open money box** for your pharmacy with initial balance
3. **Process transactions** throughout the day
4. **Monitor balance** in real-time
5. **Close money box** at end of day with reconciliation

The system is designed to be simple yet comprehensive, providing Syrian pharmacists with the tools they need to manage cash effectively and maintain proper financial control.
