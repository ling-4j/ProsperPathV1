import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { INotification } from '../notification.model';

@Component({
  selector: 'jhi-notification-detail',
  styleUrls: ['./notification-detail.component.scss'],
  templateUrl: './notification-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe],
})
export class NotificationDetailComponent {
  notification = input<INotification | null>(null);

  previousState(): void {
    window.history.back();
  }

  // Tách phần văn bản của thông điệp (không bao gồm icon và categoryName)
  formatNotificationMessageText(message: string): string {
    const regex = /Bạn đã chi tiêu vượt quá ngân sách có khối lượng ([\d\.]+)₫ với số tiền vượt là ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
    const match = message.match(regex);

    if (match) {
      const budgetAmount = match[1]; // Ví dụ: "1.000"
      const exceededAmount = match[2]; // Ví dụ: "1.222"
      let startDateIso = match[3]; // Ví dụ: "2025-05-03T07:45:00Z" hoặc "03/05/2025"
      let endDateIso = match[4]; // Ví dụ: "2025-05-12T07:45:00Z" hoặc "12/05/2025"

      // Định dạng ngày từ ISO sang dd/MM/yyyy nếu cần
      const startDateFormatted = startDateIso.includes('T')
        ? new Date(startDateIso).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
        : startDateIso;
      const endDateFormatted = endDateIso.includes('T')
        ? new Date(endDateIso).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
        : endDateIso;

      return `Bạn đã chi tiêu vượt quá ngân sách có khối lượng ${budgetAmount}₫ với số tiền vượt là ${exceededAmount}₫ trong khoảng thời gian đã thiết lập từ ngày ${startDateFormatted} đến ngày ${endDateFormatted} của danh mục`;
    }
    console.log('No match for message:', message); // Debug
    return message;
  }

  // Lấy categoryIcon (loại bỏ tiền tố 'fa-')
  getCategoryIcon(message: string): string {
    const regex = /Bạn đã chi tiêu vượt quá ngân sách có khối lượng ([\d\.]+)₫ với số tiền vượt là ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
    const match = message.match(regex);
    return match && match[5] ? match[5].replace('fa-', '') : '';
  }

  // Lấy categoryName
  getCategoryName(message: string): string {
    const regex = /Bạn đã chi tiêu vượt quá ngân sách có khối lượng ([\d\.]+)₫ với số tiền vượt là ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
    const match = message.match(regex);
    return match ? match[6] : '';
  }
}