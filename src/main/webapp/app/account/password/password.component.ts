import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable } from 'rxjs';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { PasswordService } from './password.service';
import PasswordStrengthBarComponent from './password-strength-bar/password-strength-bar.component';

@Component({
  selector: 'jhi-password',
  styleUrl: './password.component.scss',
  imports: [SharedModule, FormsModule, ReactiveFormsModule, PasswordStrengthBarComponent],
  templateUrl: './password.component.html',
})
export default class PasswordComponent implements OnInit {
  doNotMatch = signal(false);
  error = signal(false);
  success = signal(false);
  account$?: Observable<Account | null>;
  passwordForm = new FormGroup({
    currentPassword: new FormControl('', { nonNullable: true, validators: Validators.required }),
    newPassword: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(4), Validators.maxLength(50)],
    }),
    confirmPassword: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(4), Validators.maxLength(50)],
    }),
  });

  private readonly passwordService = inject(PasswordService);
  private readonly accountService = inject(AccountService);

  ngOnInit(): void {
    this.account$ = this.accountService.identity();
  }

  changePassword(): void {
    this.error.set(false);
    this.success.set(false);
    this.doNotMatch.set(false);

    const { newPassword, confirmPassword, currentPassword } = this.passwordForm.getRawValue();
    if (newPassword !== confirmPassword) {
      this.doNotMatch.set(true);
    } else {
      this.passwordService.save(newPassword, currentPassword).subscribe({
        next: () => this.success.set(true),
        error: () => this.error.set(true),
      });
    }
  }
}
