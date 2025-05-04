import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core'; // Thêm OnDestroy
import { Router, RouterModule } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { StateStorageService } from 'app/core/auth/state-storage.service';
import { NotificationService } from 'app/entities/notification/service/notification.service';
import { INotification } from 'app/entities/notification/notification.model';
import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TruncatePipe } from 'app/shared/truncate/truncate.pipe';

import HasAnyAuthorityDirective from 'app/shared/auth/has-any-authority.directive';
import { LANGUAGES } from 'app/config/language.constants';
import { AccountService } from 'app/core/auth/account.service';
import { LoginService } from 'app/login/login.service';
import { EntityNavbarItems } from 'app/entities/entity-navbar-items';
import { environment } from 'environments/environment';
import ActiveMenuDirective from './active-menu.directive';
import NavbarItem from './navbar-item.model';

@Component({
  selector: 'jhi-navbar',
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
  imports: [RouterModule, SharedModule, ActiveMenuDirective, FormatMediumDatetimePipe, TruncatePipe],
})
export default class NavbarComponent implements OnInit, OnDestroy { // Thêm OnDestroy
  inProduction?: boolean;
  isNavbarCollapsed = signal(true);
  languages = LANGUAGES;
  openAPIEnabled?: boolean;
  version = '';
  account = inject(AccountService).trackCurrentAccount();
  entitiesNavbarItems: NavbarItem[] = [];
  notifications = signal<INotification[]>([]);
  unreadNotifications = signal<INotification[]>([]);
  sortState = signal<{ predicate: string; order: 'asc' | 'desc' }>({ predicate: 'createdAt', order: 'desc' });

  private readonly loginService = inject(LoginService);
  private readonly translateService = inject(TranslateService);
  private readonly stateStorageService = inject(StateStorageService);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);

  private pollingInterval: any; // Biến lưu interval

  constructor() {
    const { VERSION } = environment;
    if (VERSION) {
      this.version = VERSION.toLowerCase().startsWith('v') ? VERSION : `v${VERSION}`;
    }
  }

  ngOnInit(): void {
    this.entitiesNavbarItems = EntityNavbarItems;
    this.loadNotifications();
    // Bắt đầu polling: gọi loadNotifications mỗi 30 giây
    this.pollingInterval = setInterval(() => {
      this.loadNotifications();
    }, 3600000); // 1 giờ = 3600000 ms
  }

  ngOnDestroy(): void {
    // Dọn dẹp interval khi component bị hủy
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
    }
  }

  loadNotifications(): void {
    const sort = this.sortState();
    const sortParam = `${sort.predicate},${sort.order}`;
    this.notificationService.query({ sort: sortParam }).subscribe({
      next: (data) => {
        const allNotifications = data.body ?? [];
        this.notifications.set(allNotifications);
        this.unreadNotifications.set(allNotifications.filter(notification => !notification.isRead));
      },
      error: (err) => {
        console.error('Lỗi khi lấy thông báo:', err);
      },
    });
  }

  handleNotificationClick(notification: INotification): void {
    if (!notification.id) {
      console.error('Notification ID is undefined:', notification);
      return;
    }
    if (!notification.isRead) {
      notification.isRead = true;
      this.notificationService.update(notification).subscribe({
        next: () => {
          // Cập nhật danh sách sau khi đánh dấu đã đọc
          this.loadNotifications();
        },
        error: (err) => {
          console.error('Error marking notification as read:', err);
          notification.isRead = false; // Khôi phục trạng thái nếu lỗi
        },
      });
    }
    this.collapseNavbar();
    this.router.navigate(['/notification', notification.id, 'view']);
  }

  changeLanguage(languageKey: string): void {
    this.stateStorageService.storeLocale(languageKey);
    this.translateService.use(languageKey);
  }

  collapseNavbar(): void {
    this.isNavbarCollapsed.set(true);
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  logout(): void {
    this.collapseNavbar();
    this.loginService.logout();
    this.router.navigate(['']);
  }

  toggleNavbar(): void {
    this.isNavbarCollapsed.update((isNavbarCollapsed) => !isNavbarCollapsed);
  }
}