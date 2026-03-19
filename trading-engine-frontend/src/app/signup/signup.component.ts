import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  username = '';
  password = '';
  email = '';
  fullName = '';
  errorMessage = '';
  isLoading = false;

  constructor(private authService: AuthService, private router: Router) {}

  onSignup() {
    if (this.username && this.password && this.email && this.fullName) {
      this.isLoading = true;
      this.errorMessage = '';
      this.authService.register(this.username, this.password, this.email, this.fullName).subscribe({
        next: (response: any) => {
          this.isLoading = false;
          alert('Registration successful! Please log in.');
          this.router.navigate(['/login']);
        },
        error: (err: any) => {
          this.isLoading = false;
          this.errorMessage = err.error || 'Registration failed. Username may be taken.';
        }
      });
    } else {
      this.errorMessage = "Please enter all required details";
    }
  }
}
