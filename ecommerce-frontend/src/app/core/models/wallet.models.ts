export interface WalletApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface Wallet {
  id: number;
  userId: number;
  currentBalance: number;
  totalCredited: number;
  totalDebited: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Transaction {
  id: number;
  transactionId: string;
  type: string; // CREDIT, DEBIT
  amount: number;
  balanceBefore: number;
  balanceAfter: number;
  status: string; // SUCCESS, FAILED, PENDING
  description: string;
  referenceId: string;
  orderNumber: string;
  transactionDate: string;
  remarks: string;
}

export interface TransactionResponse {
  content: Transaction[];
  pageNo: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface AddMoneyRequest {
  userId: number;
  amount: number;
  remarks?: string;
}

export interface WalletPaymentRequest {
  userId: number;
  amount: number;
  orderNumber: string;
}

export interface WalletPaymentResponse {
  success: boolean;
  message: string;
  balanceAfter: number;
}
