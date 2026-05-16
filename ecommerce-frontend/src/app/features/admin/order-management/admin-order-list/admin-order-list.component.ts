import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../../../core/services/order.service';
import { Order } from '../../../../core/models/order.models';

@Component({
  selector: 'app-admin-order-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './admin-order-list.component.html',
  styleUrl: './admin-order-list.component.css'
})
export class AdminOrderListComponent implements OnInit {
  orders: Order[] = [];
  isLoading = true;

  currentPage = 0;
  totalPages = 0;
  pageSize = 20;
  
  statusFilter = 'ALL';
  availableStatuses = ['PENDING', 'CONFIRMED', 'PROCESSING', 'DISPATCHED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED', 'RETURNED'];

  updatingOrderId: number | null = null;

  constructor(private orderService: OrderService) {}

  ngOnInit() {
    this.loadOrders();
  }

  loadOrders() {
    this.isLoading = true;
    
    const request = this.statusFilter === 'ALL' 
      ? this.orderService.getAllOrders(this.currentPage, this.pageSize)
      : this.orderService.getOrdersByStatus(this.statusFilter, this.currentPage, this.pageSize);

    request.subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.orders = res.data.content;
          this.totalPages = res.data.totalPages;
        }
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  onFilterChange() {
    this.currentPage = 0;
    this.loadOrders();
  }

  changePage(dir: number) {
    if (this.currentPage + dir >= 0 && this.currentPage + dir < this.totalPages) {
      this.currentPage += dir;
      this.loadOrders();
    }
  }

  updateStatus(order: Order, newStatus: string) {
    if (order.status === newStatus) return;

    this.updatingOrderId = order.id;
    this.orderService.updateOrderStatus({ orderId: order.id, status: newStatus }).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          order.status = res.data.status;
        }
        this.updatingOrderId = null;
      },
      error: () => {
        alert('Failed to update status.');
        this.updatingOrderId = null;
      }
    });
  }

  getStatusClass(status: string): string {
    switch(status.toUpperCase()) {
      case 'DELIVERED': return 'status-success';
      case 'CANCELLED': 
      case 'RETURNED': return 'status-error';
      case 'DISPATCHED':
      case 'OUT_FOR_DELIVERY': return 'status-info';
      default: return 'status-warning';
    }
  }
}
