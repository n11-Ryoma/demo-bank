export type LoginRequest = { username?: string; password?: string };
export type AuthResponse = { token?: string; status?: string };

export type RegisterRequest = { username?: string; password?: string };
export type RegisterResponse = { username?: string; accountNumber?: string; message?: string };

export type CashOperationRequest = { amount?: number; description?: string };
export type BalanceResponse = { accountNumber?: string; balance?: number };

export type TransferRequest = { toAccountNumber?: string; amount?: number; description?: string };
export type TransferResponse = { fromAccountNumber?: string; toAccountNumber?: string; amount?: number; newBalance?: number };

export type TransactionHistoryItem = {
  accountNumber?: string;
  type?: "DEPOSIT" | "WITHDRAW" | "TRANSFER_IN" | "TRANSFER_OUT";
  amount?: number;
  balanceAfter?: number;
  relatedAccountNumber?: string;
  description?: string;
  createdAt?: string; // date-time
};

export type CurrentAddressResponse = {
  postalCode?: string; prefecture?: string; city?: string; addressLine1?: string; addressLine2?: string;
};

export type AddressChangeCommitRequest = {
  postalCode?: string; prefecture?: string; city?: string; addressLine1?: string; addressLine2?: string;
  fileName?: string; fileBase64?: string;
};

export type AddressChangeResponse = { status?: string };
