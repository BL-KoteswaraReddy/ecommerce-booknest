import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WalletService } from '../../core/services/wallet.service';
import { Transaction } from '../../core/models/wallet.models';

@Component({
  selector: 'app-wallet',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './wallet.component.html',
  styleUrl: './wallet.component.css'
})
export class WalletComponent implements OnInit {
  currentBalance = 0;
  transactions: Transaction[] = [];
  
  isLoadingTransactions = true;
  isAddingMoney = false;
  
  addAmount: number | null = null;
  addRemarks: string = 'Added via BookNest Wallet';

  currentPage = 0;
  totalPages = 0;
  pageSize = 10;
  
  activeTab = 'ALL'; // ALL, CREDIT, DEBIT

  constructor(private walletService: WalletService) {}

  ngOnInit() {
    this.walletService.walletBalance$.subscribe(balance => {
      this.currentBalance = balance;
    });
    
    this.loadTransactions();
  }

  loadTransactions() {
    this.isLoadingTransactions = true;
    
    const req = this.activeTab === 'ALL'
      ? this.walletService.getUserTransactions(this.currentPage, this.pageSize)
      : this.walletService.getUserTransactionsByType(this.activeTab, this.currentPage, this.pageSize);
      
    if (req) {
      req.subscribe({
        next: (res) => {
          if (res.success && res.data) {
            this.transactions = res.data.content;
            this.totalPages = res.data.totalPages;
          }
          this.isLoadingTransactions = false;
        },
        error: () => this.isLoadingTransactions = false
      });
    } else {
      this.isLoadingTransactions = false;
    }
  }

  setTab(tab: string) {
    this.activeTab = tab;
    this.currentPage = 0;
    this.loadTransactions();
  }

  changePage(dir: number) {
    if (this.currentPage + dir >= 0 && this.currentPage + dir < this.totalPages) {
      this.currentPage += dir;
      this.loadTransactions();
    }
  }

  onAddMoney() {
    if (!this.addAmount || this.addAmount < 10) {
      alert('Minimum amount to add is $10.');
      return;
    }
    
    this.isAddingMoney = true;
    const req = this.walletService.addMoney(this.addAmount, this.addRemarks);
    
    if (req) {
      req.subscribe({
        next: (res) => {
          if (res.success) {
            alert(`Successfully added $${this.addAmount} to your wallet!`);
            this.addAmount = null; // Reset form
            this.activeTab = 'ALL';
            this.currentPage = 0;
            this.loadTransactions(); // Refresh history
          } else {
            alert(res.message || 'Failed to add money.');
          }
          this.isAddingMoney = false;
        },
        error: (err) => {
          alert(err.error?.message || 'An error occurred while adding money.');
          this.isAddingMoney = false;
        }
      });
    } else {
      this.isAddingMoney = false;
    }
  }

  getStatusClass(status: string): string {
    switch(status.toUpperCase()) {
      case 'SUCCESS': return 'status-success';
      case 'FAILED': return 'status-error';
      default: return 'status-warning';
    }
  }
}
