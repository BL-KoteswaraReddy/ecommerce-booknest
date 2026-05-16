import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Users } from '../../../core/models/auth.models';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-oauth-callback',
  standalone: true,
  imports: [CommonModule],
  template: `<div style="padding: 2rem; text-align: center;">Signing you in...</div>`
})
export class OauthCallbackComponent implements OnInit {
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      const token = params.get('token');
      const role = params.get('role');
      const name = params.get('name');
      const email = params.get('email');

      if (!token || !email) {
        this.router.navigate(['/login'], {
          queryParams: { error: 'oauth_failed' }
        });
        return;
      }

      const oauthUser: Users = {
        userId: 0,
        fullName: name ?? 'Google User',
        email,
        role: role ?? 'CUSTOMER',
        mobile: '',
        createdAt: new Date().toISOString(),
        token
      };

      this.authService.saveSession(oauthUser);
      this.router.navigate(['/profile']);
    });
  }
}
