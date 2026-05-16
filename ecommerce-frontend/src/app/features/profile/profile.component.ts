import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';
import { Users } from '../../core/models/auth.models';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { UserReviewsComponent } from './user-reviews/user-reviews.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, UserReviewsComponent],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  user: Users | null = null;
  changePasswordForm: FormGroup;
  passwordMsg: string = '';
  isPasswordError: boolean = false;
  isChangingPwd: boolean = false;
  
  editProfileForm: FormGroup;
  isEditing: boolean = false;
  isUpdatingProfile: boolean = false;
  profileMsg: string = '';
  isProfileError: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.changePasswordForm = this.fb.group({
      oldPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.editProfileForm = this.fb.group({
      fullName: ['', Validators.required],
      mobile: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]]
    });
  }

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      if (user?.email) {
        this.loadProfile(user.email);
      } else if (user) {
        this.authService.logout();
        this.router.navigate(['/login']);
      }
    });
  }

  loadProfile(email: string) {
    this.authService.getProfile(email).subscribe({
      next: (res) => {
        if (res.data) {
          this.user = res.data;
          this.editProfileForm.patchValue({
            fullName: this.user.fullName,
            mobile: this.user.mobile
          });
        }
      },
      error: (err) => console.error('Error fetching profile', err)
    });
  }

  toggleEdit() {
    this.isEditing = !this.isEditing;
    if (!this.isEditing && this.user) {
      this.editProfileForm.patchValue({
        fullName: this.user.fullName,
        mobile: this.user.mobile
      });
    }
    this.profileMsg = '';
  }

  onUpdateProfile() {
    if (this.editProfileForm.invalid || !this.user) return;

    this.isUpdatingProfile = true;
    this.profileMsg = '';
    const { fullName, mobile } = this.editProfileForm.value;

    this.authService.updateProfile(this.user.email, fullName, mobile).subscribe({
      next: (res) => {
        this.profileMsg = 'Profile updated successfully.';
        this.isProfileError = false;
        this.isUpdatingProfile = false;
        this.isEditing = false;
        if (res.data) {
          this.user = res.data;
        }
      },
      error: (err) => {
        this.profileMsg = err.error?.message || 'Failed to update profile.';
        this.isProfileError = true;
        this.isUpdatingProfile = false;
      }
    });
  }

  onChangePassword() {
    if (this.changePasswordForm.invalid || !this.user) return;
    
    this.isChangingPwd = true;
    this.passwordMsg = '';
    const { oldPassword, newPassword } = this.changePasswordForm.value;

    this.authService.changePassword(this.user.email, oldPassword, newPassword).subscribe({
      next: (res) => {
        this.passwordMsg = res.message || 'Password changed successfully.';
        this.isPasswordError = false;
        this.isChangingPwd = false;
        this.changePasswordForm.reset();
      },
      error: (err) => {
        this.passwordMsg = err.error?.message || 'Failed to change password.';
        this.isPasswordError = true;
        this.isChangingPwd = false;
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
