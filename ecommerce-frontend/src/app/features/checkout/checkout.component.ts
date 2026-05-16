import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { OrderService } from '../../core/services/order.service';
import { AuthService } from '../../core/services/auth.service';
import { WalletService } from '../../core/services/wallet.service';
import { Cart } from '../../core/models/cart.models';
import { PlaceOrderRequest } from '../../core/models/order.models';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css'
})
export class CheckoutComponent implements OnInit {
  checkoutForm: FormGroup;
  cart: Cart | null = null;
  isLoading = true;
  isPlacingOrder = false;
  errorMessage = '';
  walletBalance = 0;

  constructor(
    private fb: FormBuilder,
    private cartService: CartService,
    private orderService: OrderService,
    private authService: AuthService,
    private walletService: WalletService,
    private router: Router
  ) {
    this.checkoutForm = this.fb.group({
      fullName: ['', Validators.required],
      mobile: ['', [Validators.required, Validators.pattern('^[0-9]{10,15}$')]],
      alternateMobile: [''],
      address: ['', Validators.required],
      city: ['', Validators.required],
      state: ['', Validators.required],
      postalCode: ['', Validators.required],
      country: ['USA', Validators.required], // Defaulting to USA for this demo
      paymentMode: ['CASH_ON_DELIVERY', Validators.required],
      orderNotes: ['']
    });
  }

  ngOnInit() {
    this.loadCartSummary();
    this.walletService.walletBalance$.subscribe(bal => {
      this.walletBalance = bal;
    });
  }

  loadCartSummary() {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) {
      this.router.navigate(['/login']);
      return;
    }

    this.cartService.getCart(user.userId).subscribe({
      next: (res) => {
        if (res.success && res.data && res.data.items && res.data.items.length > 0) {
          this.cart = res.data;
        } else {
          // If cart is empty, redirect back to catalog
          this.router.navigate(['/catalog']);
        }
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Cart is taking too long to load. Please check that cart-service and book-service are running, then try again.';
        this.isLoading = false;
      }
    });
  }

  onSubmit() {
    if (this.checkoutForm.invalid) {
      this.checkoutForm.markAllAsTouched();
      return;
    }

    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return;

    this.isPlacingOrder = true;
    this.errorMessage = '';

    const formValues = this.checkoutForm.value;
    
    const request: PlaceOrderRequest = {
      userId: user.userId,
      paymentMode: formValues.paymentMode,
      orderNotes: formValues.orderNotes,
      shippingAddress: {
        fullName: formValues.fullName,
        mobile: formValues.mobile,
        alternateMobile: formValues.alternateMobile,
        address: formValues.address,
        city: formValues.city,
        state: formValues.state,
        postalCode: formValues.postalCode,
        country: formValues.country,
        addressType: 'Home' // Default for now
      }
    };

    // Wallet Balance Check
    if (formValues.paymentMode === 'WALLET') {
      if (!this.cart || this.walletBalance < this.cart.totalPrice) {
        this.errorMessage = 'Insufficient Wallet Balance. Please top-up or choose Cash on Delivery.';
        this.isPlacingOrder = false;
        return;
      }
    }

    this.orderService.placeOrder(request).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          
          const orderNumber = res.data.orderNumber;
          const orderId = res.data.id;

          // If wallet, process payment now
          if (formValues.paymentMode === 'WALLET') {
            this.walletService.processPayment({
              userId: user.userId,
              amount: this.cart!.totalPrice,
              orderNumber: orderNumber
            }).subscribe({
              next: (paymentRes) => {
                if (paymentRes.success) {
                  this.cartService.getCart(user.userId).subscribe(); // clear cart state
                  alert('Order placed successfully! Order Number: ' + orderNumber);
                  this.router.navigate(['/orders', orderId]);
                } else {
                  this.errorMessage = paymentRes.message || 'Payment failed.';
                  this.isPlacingOrder = false;
                }
              },
              error: (err) => {
                this.errorMessage = err.error?.message || 'Payment processing error.';
                this.isPlacingOrder = false;
              }
            });
          } else {
            // Cash on delivery
            this.cartService.getCart(user.userId).subscribe(); // clear cart state
            alert('Order placed successfully! Order Number: ' + orderNumber);
            this.router.navigate(['/orders', orderId]);
          }
          
        } else {
          this.errorMessage = res.message || 'Failed to place order.';
          this.isPlacingOrder = false;
        }
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'An error occurred while placing the order.';
        this.isPlacingOrder = false;
      }
    });
  }
}
