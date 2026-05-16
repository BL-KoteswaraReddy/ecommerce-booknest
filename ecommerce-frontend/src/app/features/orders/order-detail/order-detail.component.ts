import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { OrderService } from '../../../core/services/order.service';
import { Order } from '../../../core/models/order.models';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './order-detail.component.html',
  styleUrl: './order-detail.component.css'
})
export class OrderDetailComponent implements OnInit {
  order: Order | null = null;
  isLoading = true;
  isCancelling = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      const id = +params['id'];
      if (id) {
        this.loadOrder(id);
      }
    });
  }

  loadOrder(id: number) {
    this.isLoading = true;
    this.orderService.getOrderById(id).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.order = res.data;
        } else {
          this.errorMessage = res.message || 'Failed to load order.';
        }
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'An error occurred while loading the order.';
        this.isLoading = false;
      }
    });
  }

  canCancel(): boolean {
    if (!this.order) return false;
    const s = this.order.status.toUpperCase();
    return s === 'PENDING' || s === 'CONFIRMED' || s === 'PROCESSING';
  }

  cancelOrder() {
    if (!this.order || !this.canCancel()) return;

    if (confirm('Are you sure you want to cancel this order? This action cannot be undone.')) {
      this.isCancelling = true;
      this.orderService.cancelOrder(this.order.id).subscribe({
        next: (res) => {
          if (res.success) {
            alert('Order cancelled successfully.');
            this.loadOrder(this.order!.id); // Reload to get updated status
          }
          this.isCancelling = false;
        },
        error: () => {
          alert('Failed to cancel order. Please try again later.');
          this.isCancelling = false;
        }
      });
    }
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
