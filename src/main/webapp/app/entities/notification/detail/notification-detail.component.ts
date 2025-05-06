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
  private static readonly EXPENSE_REGEX =
    /Bạn đã chi tiêu vượt quá ngân sách có khối lượng ([\d,.]+)₫ với số tiền vượt là ([\d,.]+)₫ trong khoảng thời gian đã thiết lập từ ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
  private static readonly INCOME_REGEX =
    /Bạn đã hoàn thành mục tiêu ngân sách với khối lượng ([\d,.]+)₫ trong khoảng thời gian đã thiết lập từ ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) vào ngày ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;
  private static readonly WARNING_REGEX =
    /Cảnh báo! Bạn sắp vượt chi tiêu ngân sách có khối lượng ([\d,.]+)₫ trong khoảng thời gian đã thiết lập từ ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) đến ([\d-]{10}T[\d:]{8}Z|\d{2}\/\d{2}\/\d{4}) của danh mục (?:(\S+) )?(.+)/;

  notification = input<INotification | null>(null);

  previousState(): void {
    window.history.back();
  }
  // Tách phần văn bản của thông điệp (không bao gồm icon và categoryName)
  formatNotificationMessageText(message: string): string {
    const parsedMessage = this.parseMessage(message);
    if (!parsedMessage) {
      return message;
    }

    const { type, budgetAmount, exceededAmount, startDate, endDate, transactionDate } = parsedMessage;
    switch (type) {
      case 'expense':
        return `Bạn đã chi tiêu vượt quá ngân sách có khối lượng ${budgetAmount}₫ với số tiền vượt là ${exceededAmount}₫ trong khoảng thời gian đã thiết lập từ ${startDate} đến ${endDate} của danh mục`;
      case 'income':
        return `Bạn đã hoàn thành mục tiêu ngân sách với khối lượng ${budgetAmount}₫ trong khoảng thời gian đã thiết lập từ ${startDate} đến ${endDate} vào ngày ${transactionDate} của danh mục`;
      case 'warning':
        return `Cảnh báo! Bạn sắp vượt chi tiêu ngân sách có khối lượng ${budgetAmount}₫ trong khoảng thời gian đã thiết lập từ ${startDate} đến ${endDate} của danh mục`;
      default:
        return message;
    }
  }

  // Lấy categoryIcon (loại bỏ tiền tố 'fa-')
  getCategoryIcon(message: string): string {
    const parsedMessage = this.parseMessage(message);
    return parsedMessage?.categoryIcon?.replace('fa-', '') ?? 'null';
  }

  // Lấy categoryName
  getCategoryName(message: string): string {
    const parsedMessage = this.parseMessage(message);
    return parsedMessage?.categoryName ?? 'null';
  }

  // Lấy icon dựa trên notificationType
  getIconName(notificationType: string | null): string {
    return notificationType === 'COMPLETE' || notificationType === 'OTHER' ? 'circle-check' : 'triangle-exclamation';
  }

  // Lấy màu icon dựa trên notificationType
  getIconColor(notificationType: string | null): string {
    switch (notificationType) {
      case 'COMPLETE':
        return 'green';
      case 'OTHER':
        return '#007bff';
      case 'WARNING':
        return 'orange';
      default:
        return '#dc3545';
    }
  }

  /**
   * Parses the notification message and extracts relevant fields.
   *
   * @param message The notification message to parse.
   * @returns Parsed message object with type and fields, or null if no match.
   */
  private parseMessage(message: string): ParsedMessage | null {
    const expenseMatch = message.match(NotificationDetailComponent.EXPENSE_REGEX);
    if (expenseMatch) {
      return {
        type: 'expense',
        budgetAmount: expenseMatch[1],
        exceededAmount: expenseMatch[2],
        startDate: expenseMatch[3],
        endDate: expenseMatch[4],
        categoryIcon: expenseMatch[5],
        categoryName: expenseMatch[6],
      };
    }

    const incomeMatch = message.match(NotificationDetailComponent.INCOME_REGEX);
    if (incomeMatch) {
      return {
        type: 'income',
        budgetAmount: incomeMatch[1],
        startDate: incomeMatch[2],
        endDate: incomeMatch[3],
        transactionDate: incomeMatch[4],
        categoryIcon: incomeMatch[5],
        categoryName: incomeMatch[6],
      };
    }

    const warningMatch = message.match(NotificationDetailComponent.WARNING_REGEX);
    if (warningMatch) {
      return {
        type: 'warning',
        budgetAmount: warningMatch[1],
        startDate: warningMatch[2],
        endDate: warningMatch[3],
        categoryIcon: warningMatch[4],
        categoryName: warningMatch[5],
      };
    }

    return null;
  }
}

/**
 * Interface representing a parsed notification message.
 */
interface ParsedMessage {
  type: 'expense' | 'income' | 'warning';
  budgetAmount: string;
  exceededAmount?: string;
  startDate: string;
  endDate: string;
  transactionDate?: string;
  categoryIcon?: string;
  categoryName?: string;
}
