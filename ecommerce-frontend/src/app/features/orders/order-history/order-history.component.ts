import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { OrderService } from '../../../core/services/order.service';
import { AuthService } from '../../../core/services/auth.service';
import { Order } from '../../../core/models/order.models';

@Component({
  selector: 'app-order-history',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './order-history.component.html',
  styleUrl: './order-history.component.css'
})
export class OrderHistoryComponent implements OnInit {
  orders: Order[] = [];
  isLoading = true;
  
  currentPage = 0;
  totalPages = 0;
  pageSize = 10;

  constructor(private orderService: OrderService, private authService: AuthService) {}

  ngOnInit() {
    this.loadOrders();
  }

  loadOrders() {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return;

    this.isLoading = true;
    this.orderService.getUserOrders(user.userId, this.currentPage, this.pageSize).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.orders = res.data.content;
          this.totalPages = res.data.totalPages;
        }
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  changePage(dir: number) {
    if (this.currentPage + dir >= 0 && this.currentPage + dir < this.totalPages) {
      this.currentPage += dir;
      this.loadOrders();
    }
  }

  getStatusClass(status: string): string {
    switch(status.toUpperCase()) {
      case 'DELIVERED': return 'status-success';
      case 'CANCELLED': 
      case 'RETURNED': return 'status-error';
      case 'DISPATCHED':
      case 'OUT_FOR_DELIVERY': return 'status-info';
      default: return 'status-warning'; // PENDING, PROCESSING
    }
  }
}
