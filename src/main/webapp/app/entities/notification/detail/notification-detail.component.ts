
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
    const expenseRegex = /Bạn đã chi tiêu vượt quá ngân sách có khối lượng ([\d\.]+)₫ với số tiền vượt là ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
    const incomeRegex = /Bạn đã hoàn thành mục tiêu ngân sách với khối lượng ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) vào ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
  
    const expenseMatch = message.match(expenseRegex);
    const incomeMatch = message.match(incomeRegex);
  
    if (expenseMatch) {
      const budgetAmount = expenseMatch[1]; 
      const exceededAmount = expenseMatch[2]; 
      let startDateIso = expenseMatch[3]; 
      let endDateIso = expenseMatch[4]; 
  
      return `Bạn đã chi tiêu vượt quá ngân sách có khối lượng ${budgetAmount}₫ với số tiền vượt là ${exceededAmount}₫ trong khoảng thời gian đã thiết lập từ ${startDateIso} đến ${endDateIso} của danh mục`;
    } else if (incomeMatch) {
      const budgetAmount = incomeMatch[1]; 
      let startDateIso = incomeMatch[2]; 
      let endDateIso = incomeMatch[3]; 
      let transactionDateIso = incomeMatch[4]; 
  
      return `Bạn đã hoàn thành mục tiêu ngân sách với khối lượng ${budgetAmount}₫ trong khoảng thời gian đã thiết lập từ ${startDateIso} đến ${endDateIso} vào ngày ${transactionDateIso} của danh mục`;
    }
    console.log('No match for message:', message);
    return message;
  }

  // Lấy categoryIcon (loại bỏ tiền tố 'fa-')
  getCategoryIcon(message: string): string {
    const expenseRegex = /Bạn đã chi tiêu vượt quá ngân sách có khối lượng ([\d\.]+)₫ với số tiền vượt là ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
    const incomeRegex = /Bạn đã hoàn thành mục tiêu ngân sách với khối lượng ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) vào ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
  
    const expenseMatch = message.match(expenseRegex);
    const incomeMatch = message.match(incomeRegex);
  
    if (incomeMatch && incomeMatch[5]) {
      return incomeMatch[5].replace('fa-', '');
    } else if (expenseMatch && expenseMatch[5]) {
      return expenseMatch[5].replace('fa-', '');
    }
    return 'null';
  }

  // Lấy categoryName
  getCategoryName(message: string): string {
    const expenseRegex = /Bạn đã chi tiêu vượt quá ngân sách có khối lượng ([\d\.]+)₫ với số tiền vượt là ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
    const incomeRegex = /Bạn đã hoàn thành mục tiêu ngân sách với khối lượng ([\d\.]+)₫ trong khoảng thời gian đã thiết lập từ ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) vào ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
  
    const expenseMatch = message.match(expenseRegex);
    const incomeMatch = message.match(incomeRegex);
  
    if (incomeMatch && incomeMatch[6]) {
      return incomeMatch[6];
    } else if (expenseMatch && expenseMatch[6]) {
      return expenseMatch[6];
    }
    return 'null';
  }

  // Lấy icon dựa trên notificationType
  getIconName(notificationType: string | null): string {
    return notificationType === 'OTHER' ? 'circle-check' : 'triangle-exclamation';
  }

  // Lấy màu icon dựa trên notificationType
  getIconColor(notificationType: string | null): string {
    return notificationType === 'OTHER' ? '#007bff' : '#dc3545'; // Replace $primary with a valid color code
  }
}